package pending_request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import javax.xml.soap.SOAPException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import config.Environment;
import dcf_log.DcfLogParser;
import dcf_log.DcfResponse;
import soap.UploadCatalogueFileImpl;
import soap.UploadCatalogueFileImpl.PublishLevel;
import soap.UploadCatalogueFileImpl.ReserveLevel;
import user.DcfUser;

/**
 * Test that the pending requests generated by the upload catalogue file
 * calls are correct in the contents
 * @author avonva
 * @author shahaal
 */
public class PendingRequestTest {
	
	private Environment env;
	private DcfUser user;
	
	@BeforeEach
	public void init() {
		this.user = new DcfUser();
		this.user.login("avonva", "Ab123456");
		this.env = Environment.TEST;
	}
	
	@Test
	public void publishMinor() throws SOAPException, IOException {
		
		UploadCatalogueFileImpl ucf = new UploadCatalogueFileImpl();
		
		IPendingRequest request = ucf.publish(this.user, this.env, PublishLevel.MINOR, "ACTION");

		DcfResponse response = request.start(new DcfLogParser());
		
		assertNotNull(response);
	}
	
	
	@Test
	public void publishMajor() throws SOAPException, IOException {
		
		UploadCatalogueFileImpl ucf = new UploadCatalogueFileImpl();
		
		IPendingRequest request = ucf.publish(this.user, this.env, PublishLevel.MAJOR, "ACTION");

		DcfResponse response = request.start(new DcfLogParser());
		
		assertNotNull(response);
	}
	
	@Test
	public void reserveMinor() throws SOAPException, IOException {
		
		UploadCatalogueFileImpl ucf = new UploadCatalogueFileImpl();
		
		IPendingRequest request = ucf.reserve(this.user, this.env, ReserveLevel.MINOR, "ACTION", "Test upload catalogue file");

		DcfResponse response = request.start(new DcfLogParser());

		assertNotNull(response);
	}
	
	@Test
	public void reserveMajor() throws SOAPException, IOException {
		
		UploadCatalogueFileImpl ucf = new UploadCatalogueFileImpl();
		
		IPendingRequest request = ucf.reserve(this.user, this.env, ReserveLevel.MAJOR, "ACTION", "Test upload catalogue file");

		DcfResponse response = request.start(new DcfLogParser());
		
		assertNotNull(response);
	}

	@Test
	public void unreserveMinor() throws SOAPException, IOException {
		
		UploadCatalogueFileImpl ucf = new UploadCatalogueFileImpl();
		
		IPendingRequest request = ucf.unreserve(this.user, this.env, "ACTION", "Test upload catalogue file");

		DcfResponse response = request.start(new DcfLogParser());
		
		assertNotNull(response);
	}
	
	
	@Test
	public void pendingReserveMinor() throws SOAPException {
		
		UploadCatalogueFileImplMock ucf = new UploadCatalogueFileImplMock();
		
		IPendingRequest request = ucf.reserve(this.user, this.env, ReserveLevel.MINOR, "ACTION", "Test upload catalogue file");
		
		assertEquals(request.getType(), IPendingRequest.TYPE_RESERVE_MINOR);
		assertEquals(request.getRequestor(), this.user);
		assertEquals(request.getEnvironmentUsed(), this.env);
	}
	
	@Test
	public void pendingReserveMajor() throws SOAPException {
		
		UploadCatalogueFileImplMock ucf = new UploadCatalogueFileImplMock();
		
		IPendingRequest request = ucf.reserve(this.user, this.env, ReserveLevel.MAJOR, "ACTION", "Test upload catalogue file");
		
		assertEquals(request.getType(), IPendingRequest.TYPE_RESERVE_MAJOR);
		assertEquals(request.getRequestor(), this.user);
		assertEquals(request.getEnvironmentUsed(), this.env);
	}
	
	@Test
	public void pendingUnreserve() throws SOAPException {

		UploadCatalogueFileImplMock ucf = new UploadCatalogueFileImplMock();
		
		IPendingRequest request = ucf.unreserve(this.user, this.env, "ACTION", "Test upload catalogue file");
		
		assertEquals(request.getType(), IPendingRequest.TYPE_UNRESERVE);
		assertEquals(request.getRequestor(), this.user);
		assertEquals(request.getEnvironmentUsed(), this.env);
	}	
	
	@Test
	public void pendingPublishMinor() throws SOAPException {

		UploadCatalogueFileImplMock ucf = new UploadCatalogueFileImplMock();
		
		IPendingRequest request = ucf.publish(this.user, this.env, PublishLevel.MINOR, "ACTION");
		
		assertEquals(request.getType(), IPendingRequest.TYPE_PUBLISH_MINOR);
		assertEquals(request.getRequestor(), this.user);
		assertEquals(request.getEnvironmentUsed(), this.env);
	}
	
	@Test
	public void pendingPublishMajor() throws SOAPException {
		
		UploadCatalogueFileImplMock ucf = new UploadCatalogueFileImplMock();
		
		IPendingRequest request = ucf.publish(this.user, this.env, PublishLevel.MAJOR, "ACTION");
		
		assertEquals(request.getType(), IPendingRequest.TYPE_PUBLISH_MAJOR);
		assertEquals(request.getRequestor(), this.user);
		assertEquals(request.getEnvironmentUsed(), this.env);
	}
}
