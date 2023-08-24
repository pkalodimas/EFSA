package pending_request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;

import javax.xml.soap.SOAPException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import config.Environment;
import dcf_log.DcfLogParserMock;
import soap.UploadCatalogueFileImpl.ReserveLevel;
import soap.UploadCatalogueFilePersistentImpl;
import user.DcfUser;

/**
 * Check that the pending requests are correctly stored
 * 
 * @author avonva
 * @author shahaal
 */
public class UploadCatalogueFilePersistentTest {

	private Environment env;
	private DcfUser user;

	@BeforeEach
	public void init() {
		this.user = new DcfUser();
		this.user.login("avonva", "Ab123456");
		this.env = Environment.TEST;
	}

	@Test
	public void insertAndRemovePendingRequestAutomaticallyWithPersistentClass()
			throws SQLException, IOException, SOAPException {

		PendingRequestDaoMock<IPendingRequest> dao = new PendingRequestDaoMock<>();

		UploadCatalogueFilePersistentImpl ucf = new UploadCatalogueFilePersistentImpl(dao);

		IPendingRequest req = ucf.reserve(this.user, this.env, ReserveLevel.MINOR, "ACTION",
				"Test of reserve minor with db");
		assertNotNull(req);

		// here the request should be stored in the db
		// let's check
		IDcfPendingRequestsList<IPendingRequest> output = new DcfPendingRequestsList();
		ucf.getUserPendingRequests(this.user, output);

		// get the stored request
		IPendingRequest storedReq = output.iterator().next();

		// must be not null and equal to the one received
		assertNotNull(storedReq);
		assertEquals(req, storedReq);
		assertEquals(output.size(), 1);

		// start the request
		storedReq.start(new DcfLogParserMock());

		// at the end of the process the request must be automatically
		// removed from the database
		output = new DcfPendingRequestsList();
		dao.getUserPendingRequests(this.user, output);
		assertEquals(output.size(), 0);
	}

	@Test
	public void insertAndRemovePendingRequestAutomaticallyWithPersistentClass2()
			throws SQLException, IOException, SOAPException {

		PendingRequestDaoMock<IPendingRequest> dao = new PendingRequestDaoMock<>();

		UploadCatalogueFilePersistentImpl ucf = new UploadCatalogueFilePersistentImpl(dao);

		IPendingRequest req = ucf.reserve(this.user, this.env, ReserveLevel.MINOR, "ACTION",
				"Test of reserve minor with db");
		assertNotNull(req);

		// here the request should be stored in the db
		// let's check
		IDcfPendingRequestsList<IPendingRequest> output = new DcfPendingRequestsList();
		ucf.getUserPendingRequests(this.user, output);

		// get the stored request
		IPendingRequest storedReq = output.iterator().next();

		// must be not null and equal to the one received
		assertNotNull(storedReq);
		assertEquals(req, storedReq);
		assertEquals(output.size(), 1);

		// start the request from the one retrieved from reserve
		req.start(new DcfLogParserMock());

		// at the end of the process the request must be automatically
		// removed from the database
		output = new DcfPendingRequestsList();
		dao.getUserPendingRequests(this.user, output);
		assertEquals(output.size(), 0);
	}
}
