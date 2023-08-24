package pending_request;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import user.IDcfUser;

public class PendingRequestDaoMock<T extends IPendingRequest> implements IPendingRequestDao<T> {

	private Collection<T> database;
	
	public PendingRequestDaoMock() {
		this.database = new ArrayList<>();
	}
	
	@Override
	public int insert(T object) throws SQLException, IOException {
		this.database.add(object);
		return 0;
	}

	@Override
	public boolean remove(String requestLogCode) throws SQLException, IOException {
		
		boolean removed = false;
		
		Iterator<T> iterator = this.database.iterator();
		
		while(iterator.hasNext()){
			
			T request = iterator.next();
			
			if (request.getLogCode().equals(requestLogCode)) {
				iterator.remove();
				removed = true;
			}
		}

		return removed;
	}

	@Override
	public T getByResultSet(IDcfUser user, ResultSet rs, T output) throws SQLException {
		return null;
	}

	@Override
	public IDcfPendingRequestsList<T> getUserPendingRequests(IDcfUser user, IDcfPendingRequestsList<T> output)
			throws SQLException, IOException {
		
		for (T request: this.database) {
			output.add(request);
		}
		
		return output;
	}
}
