package pending_request;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import user.IDcfUser;

public interface IPendingRequestDao<T extends IPendingRequest> {
	
	/**
	 * Insert a {@link IPendingRequest}
	 * @param pendingRequest
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public int insert(T pendingRequest) throws SQLException, IOException;
	
	/**
	 * Remove a {@link IPendingRequest} by its log code (primary key)
	 * @param requestLogCode
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public boolean remove(String requestLogCode) throws SQLException, IOException;
	
	public T getByResultSet(IDcfUser user, ResultSet rs, T output) throws SQLException;
	
	/**
	 * Get all the {@link IPendingRequest} related to an user
	 * @param user
	 * @param output
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public IDcfPendingRequestsList<T> getUserPendingRequests(IDcfUser user, 
			IDcfPendingRequestsList<T> output) throws SQLException, IOException;
}
