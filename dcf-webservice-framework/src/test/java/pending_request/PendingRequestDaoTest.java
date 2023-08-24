package pending_request;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import config.Environment;
import soap.DetailedSOAPException;
import soap.UploadCatalogueFileImpl.ReserveLevel;
import user.DcfUser;

public class PendingRequestDaoTest {
	
	private String dbUrl;
	
	@BeforeEach
	public void init() {
		this.dbUrl = "jdbc:derby:mock-database;create=true";
	}
	
	@Test
	public void test() throws DetailedSOAPException, IOException, SQLException {
		
		DcfUser user = new DcfUser();
		user.login("avonva", "Ab123456");
		
		Environment env = Environment.TEST;
		
		PendingRequestDao<IPendingRequest> dao = new PendingRequestDao<>(this.dbUrl);
		
		UploadCatalogueFilePersistentImplMock upc = new UploadCatalogueFilePersistentImplMock(dao);
		
		IPendingRequest request = upc.reserve(user, env, ReserveLevel.MINOR, "ACTION", "JUnit test for pr dao");
		
		// remove all
		boolean ok1 = dao.removeAll();
		assertEquals(ok1, true);
		
		// insert
		dao.insert(request);
		
		// select
		IDcfPendingRequestsList<IPendingRequest> output = new DcfPendingRequestsList();
		dao.getUserPendingRequests(user, output);
		assertEquals(output.size(), 1);
		assertEquals(output.iterator().next().getPriority(), PendingRequestPriority.HIGH);
		
		// delete
		boolean ok2 = dao.remove(request.getLogCode());
		assertEquals(ok2, true);
		
		// select
		output = new DcfPendingRequestsList();
		dao.getUserPendingRequests(user, output);
		assertEquals(output.isEmpty(), true);
	}
}
