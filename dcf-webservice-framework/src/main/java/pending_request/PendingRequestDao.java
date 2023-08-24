package pending_request;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import config.Environment;
import sql.SQLExecutor;
import user.IDcfUser;

/**
 * Insert/remove/get pending request from the db The pending request table is
 * created if not present after calling one dao operation.
 * 
 * @author avonva
 * @author shahaal
 * 
 * @param <T> custom type of pending request if needed, otherwise set
 *            {@link IPrioritizablePendingRequest}
 */
public class PendingRequestDao<T extends IPendingRequest> implements IPendingRequestDao<T> {

	private String dbUrl;
	private boolean created;

	public PendingRequestDao(String dbUrl) {
		this.dbUrl = dbUrl;
		this.created = false;
	}

	private Connection getConnection() throws SQLException {
		Connection con = DriverManager.getConnection(this.dbUrl);
		return con;
	}

	/**
	 * Add all the tables which were not release with the first version of the
	 * browser (if they are not present)
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public boolean isCreated() throws SQLException, IOException {

		// if already checked, avoid connection
		if (this.created)
			return true;

		boolean createdVar = true;

		try (Connection con = getConnection()) {
			DatabaseMetaData dbm = getConnection().getMetaData();
			try (ResultSet rs = dbm.getTables(null, null, "PENDING_REQUEST", null);) {
				createdVar = rs.next();
				rs.close();
			}

			con.close();
		}

		// save cache
		this.created = createdVar;

		return createdVar;
	}

	private void create() throws SQLException, IOException {

		if (isCreated())
			return;

		// solve memory leak
		try (Connection con = getConnection();
				SQLExecutor executor = new SQLExecutor(con);
				InputStream stream = PendingRequest.class.getClassLoader().getResourceAsStream("PendingRequest")) {
			executor.exec(stream);

			con.close();
		}
	}

	@Override
	public int insert(T object) throws SQLException, IOException {

		create();

		int id = -1;
		String query = "insert into APP.PENDING_REQUEST(REQ_LOG_CODE, "
				+ "REQ_TYPE, REQUESTOR, REQ_ENVIRONMENT, REQ_DATA) values (?,?,?,?,?)";

		try (Connection con = getConnection(); PreparedStatement stmt = con.prepareStatement(query);) {

			// set the parameters
			stmt.setString(1, object.getLogCode());
			stmt.setString(2, object.getType());
			stmt.setString(3, object.getRequestor().getUsername());
			stmt.setString(4, object.getEnvironmentUsed().getKey());

			if (object.getData() != null)
				stmt.setString(5, CompoundFieldManager.mapToString(object.getData()));
			else
				stmt.setNull(5, Types.VARCHAR);

			// insert the pending reserve object
			stmt.executeUpdate();
			stmt.close();
			con.close();
		}

		return id;
	}

	@Override
	public boolean remove(String requestLogCode) throws SQLException, IOException {

		create();

		boolean ok = false;
		String query = "delete from APP.PENDING_REQUEST where REQ_LOG_CODE = ?";

		try (Connection con = getConnection(); PreparedStatement stmt = con.prepareStatement(query);) {

			stmt.setString(1, requestLogCode);

			stmt.executeUpdate();

			stmt.close();
			con.close();

			ok = true;

		}

		return ok;
	}

	public boolean removeAll() throws SQLException, IOException {

		create();

		boolean ok = false;
		String query = "delete from APP.PENDING_REQUEST";

		try (Connection con = getConnection(); PreparedStatement stmt = con.prepareStatement(query);) {

			stmt.executeUpdate();

			stmt.close();
			con.close();

			ok = true;

		}

		return ok;
	}

	/**
	 * Get a pending request from the Sql data
	 * 
	 * @param user   the user who is related to the pending request
	 * @param rs
	 * @param output object where the db information will be put
	 * @return
	 * @throws SQLException
	 */
	@Override
	public T getByResultSet(IDcfUser user, ResultSet rs, T output) throws SQLException {

		String requestor = rs.getString("REQUESTOR");

		// if the pending request is not of the correct user
		if (!requestor.equalsIgnoreCase(user.getUsername()))
			return null;

		String logCode = rs.getString("REQ_LOG_CODE");
		String type = rs.getString("REQ_TYPE");

		Environment env = Environment.fromString(rs.getString("REQ_ENVIRONMENT"));
		String data = rs.getString("REQ_DATA");

		output.setLogCode(logCode);
		output.setType(type);

		output.setRequestor(user);
		output.setEnvironmentUsed(env);

		if (data != null)
			output.setData(CompoundFieldManager.stringToMap(data));

		return output;
	}

	@Override
	public IDcfPendingRequestsList<T> getUserPendingRequests(IDcfUser user, IDcfPendingRequestsList<T> output)
			throws SQLException, IOException {

		create();

		String query = "select * from APP.PENDING_REQUEST where requestor = ?";

		try (Connection con = getConnection(); PreparedStatement stmt = con.prepareStatement(query);) {

			stmt.setString(1, user.getUsername());

			try (ResultSet rs = stmt.executeQuery()) {
				// get all the pending reserve obj
				while (rs.next()) {
					T request = output.create();
					output.add(getByResultSet(user, rs, request));
				}

				rs.close();
			}

			stmt.close();
			con.close();

		}

		return output;
	}
}
