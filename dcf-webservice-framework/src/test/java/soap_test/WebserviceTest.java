package soap_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ack.DcfAck;
import catalogue.DcfCataloguesList;
import catalogue.IDcfCatalogue;
import config.Environment;
import data_collection.DcfDCTable;
import data_collection.DcfDCTablesList;
import data_collection.DcfDataCollectionsList;
import data_collection.IDcfDCTableLists;
import data_collection.IDcfDataCollection;
import dataset.DcfDatasetsList;
import dataset.IDcfDataset;
import dcf_log.DcfLogDownloader;
import dcf_log.DcfLogParser;
import dcf_log.DcfResponse;
import pending_request.IPendingRequest;
import resource.DcfResourcesList;
import resource.IDcfResourceReference;
import soap.DetailedSOAPException;
import soap.ExportCatalogueFile;
import soap.GetAck;
import soap.GetCataloguesList;
import soap.GetDataCollectionTables;
import soap.GetDataCollectionsList;
import soap.GetDataset;
import soap.GetDatasetsList;
import soap.GetResourcesList;
import soap.GetXsdFile;
import soap.Ping;
import soap.SendMessage;
import soap.UploadCatalogueFile;
import soap.UploadCatalogueFileImpl;
import soap.UploadCatalogueFileImpl.PublishLevel;
import soap.UploadCatalogueFileImpl.ReserveLevel;
import user.DcfUser;

public class WebserviceTest {

	// categories markers
	public interface PingTests {
	}

	public interface DownloadLogTests {
	}

	public interface SendMessageTests {
	}

	public interface GetAckTests {
	}

	public interface GetCataloguesListTests {
	}

	public interface GetDataCollectionListTests {
	}

	public interface GetDatasetsListTests {
	}

	public interface GetResourcesListTests {
	}

	public interface GetDatasetTests {
	}

	public interface GetXsdFileTests {
	}

	public interface ExportCatalogueFileTests {
	}

	public interface UploadCatalogueFileTests {
	}

	public interface GetDataCollectionConfigTests {
	}

	public Environment env;
	public DcfUser user;

	@BeforeEach
	public void init() {
		this.user = new DcfUser();
		this.user.login("avonva", "Ab123456");
		this.env = Environment.TEST;
	}

	@Tag("PingTests")
	public void wrongCredentials() throws DetailedSOAPException {

		DcfUser user1 = new DcfUser();
		user1.login("wrong-username", "wrong-pwd");
		Ping ping = new Ping();
		assertThrows(DetailedSOAPException.class, () -> ping.ping(this.env, user1));
	}

	@Tag("PingTests")
	public void wrongUsername() throws DetailedSOAPException {

		DcfUser user1 = new DcfUser();
		user1.login("wrong-username", "Ab123456");
		Ping ping = new Ping();
		assertThrows(DetailedSOAPException.class, () -> ping.ping(this.env, user1));
	}

	/*
	 * careful use, it might block dcf account
	 * 
	 * @Tag("PingTests")
	 * 
	 * @Test(expected = DetailedSOAPException.class) public void wrongPassword()
	 * throws DetailedSOAPException {
	 * 
	 * DcfUser user = new DcfUser(); user.login("avonva", "wrong-pwd"); Ping ping =
	 * new Ping(user, env); ping.ping(); }
	 */

	@Tag("PingTests")
	@Test
	public void ping() throws DetailedSOAPException {
		Ping ping = new Ping();
		boolean ok = ping.ping(this.env, this.user);
		assertEquals(ok, true);
	}

	@Tag("DownloadLogTests")
	@Test
	public void downloadLog() throws SOAPException {

		DcfLogDownloader downloader = new DcfLogDownloader();
		File file = downloader.getLog(this.user, this.env, "20180130_001_WS", 2000, 10);

		assertNotNull(file);
		assertEquals(file.exists(), true);
	}

	@Tag("DownloadLogTests")
	public void downloadNotExistingLog() throws SOAPException {
		DcfLogDownloader downloader = new DcfLogDownloader();
		File file = downloader.getLog(this.user, this.env, "not-existing-code", 2000, 10);
		assertEquals(file, null);
	}

	@Tag("SendMessageTests")
	public void sendNotExistingMessage() throws DetailedSOAPException, IOException {
		SendMessage request = new SendMessage();
		assertThrows(IOException.class, () -> request.send(this.env, this.user, new File("not-existing-file.xml")));
	}

	@Tag("SendMessageTests")
	@Test
	public void sendExistingMessage() throws DetailedSOAPException, IOException {
		SendMessage request = new SendMessage();
		File file = new File("test-files" + System.getProperty("file.separator") + "submit.xml");
		request.send(this.env, this.user, file);
	}

	@Tag("GetAckTests")
	public void getNotExistingAck() throws DetailedSOAPException {
		GetAck request = new GetAck();
		DcfAck ack = request.getAck(this.env, this.user, "1920392302");
		assertEquals(ack, null);
	}

	@Tag("GetAckTests")
	@Test
	public void getExistingAck() throws DetailedSOAPException {

		GetAck request = new GetAck();
		DcfAck ack = request.getAck(this.env, this.user, "12632");

		assertNotNull(ack);
		assertEquals(ack.isReady(), true);
		assertNotNull(ack.getLog());
		assertNotNull(ack.getLog().getDatasetId());
	}

	@Tag("GetCataloguesListTests")
	@Test
	public void getCataloguesList() throws DetailedSOAPException {

		DcfCataloguesList output = new DcfCataloguesList();
		GetCataloguesList<IDcfCatalogue> request = new GetCataloguesList<>();
		request.getList(this.env, this.user, output);

		assertNotNull(output);
		assertEquals(output.isEmpty(), false);
	}

	@Tag("GetCataloguesListTests")
	@Test
	public void getDCList() throws DetailedSOAPException {

		DcfDataCollectionsList output = new DcfDataCollectionsList();
		GetDataCollectionsList<IDcfDataCollection> request = new GetDataCollectionsList<>();
		request.getList(this.env, this.user, output);

		assertNotNull(output);
	}

	@Tag("GetDatasetsListTests")
	public void getNotExistingDCDatasetList() throws DetailedSOAPException {

		DcfDatasetsList output = new DcfDatasetsList();
		GetDatasetsList<IDcfDataset> request = new GetDatasetsList<>();
		request.getList(this.env, this.user, "not-existing-code", output);

		assertEquals(output.isEmpty(), true);
	}

	@Tag("GetDatasetsListTests")
	@Test
	public void getTSEDatasetList() throws DetailedSOAPException {

		DcfDatasetsList output = new DcfDatasetsList();
		GetDatasetsList<IDcfDataset> request = new GetDatasetsList<>();
		request.getList(this.env, this.user, "TSE.TEST", output);

		assertNotNull(output);
	}

	@Tag("GetResourcesListTests")
	public void getNotExistingDCResourceList() throws DetailedSOAPException {

		DcfResourcesList output = new DcfResourcesList();
		GetResourcesList<IDcfResourceReference> request = new GetResourcesList<>();
		request.getList(this.env, this.user, "not-existing-code", output);

		assertEquals(output.isEmpty(), true);
	}

	@Tag("GetResourcesListTests")
	@Test
	public void getTSEResourceList() throws DetailedSOAPException {

		DcfResourcesList output = new DcfResourcesList();
		GetResourcesList<IDcfResourceReference> request = new GetResourcesList<>();
		request.getList(this.env, this.user, "TSE.TEST", output);

		assertNotNull(output);
	}

	@Tag("GetDatasetTests")
	public void getNotExistingDataset() throws DetailedSOAPException {

		GetDataset request = new GetDataset();
		File file = request.getDatasetFile(this.env, this.user, "not-existing-code");
		assertNull(file);
	}

	@Tag("GetDatasetTests")
	@Test
	public void getExistingDataset() throws DetailedSOAPException {

		GetDataset request = new GetDataset();
		File file = request.getDatasetFile(this.env, this.user, "7216");

		assertNotNull(file);
		assertEquals(file.exists(), true);
	}

	@Tag("GetXsdFileTests")
	public void getNotExistingXsdFile() throws SOAPException, SAXException, IOException, ParserConfigurationException {

		GetXsdFile request = new GetXsdFile();
		Document document = request.getXsdFile(this.env, this.user, "not-existing-code");

		assertNull(document);
	}

	@Tag("GetXsdFileTests")
	@Test
	public void getExistingXsdFile() throws SOAPException, SAXException, IOException, ParserConfigurationException {

		GetXsdFile request = new GetXsdFile();
		Document file = request.getXsdFile(this.env, this.user, "38864");

		assertNotNull(file);
	}

	@Tag("ExportCatalogueFileTests")
	public void exportNotExistingCatalogueLastPublishedVersion() throws SOAPException {
		ExportCatalogueFile request = new ExportCatalogueFile();
		File file = request.exportCatalogue(this.env, this.user, "not-existing-code");
		assertEquals(file, null);
	}

	@Tag("ExportCatalogueFileTests")
	@Test
	public void exportCatalogueLastPublishedVersion() throws SOAPException {

		ExportCatalogueFile request = new ExportCatalogueFile();
		File file = request.exportCatalogue(this.env, this.user, "ABUNDANCE");

		System.err.println(file.length());

		assertNotNull(file);
		assertEquals(file.exists(), true);
	}

	@Tag("ExportCatalogueFileTests")
	public void exportNotExistingCatalogueLastInternalVersion() throws SOAPException {
		ExportCatalogueFile request = new ExportCatalogueFile();
		File file = request.exportLastInternalVersion(this.env, this.user, "not-existing-code");
		assertEquals(file, null);
	}

	@Tag("ExportCatalogueFileTests")
	@Test
	public void exportCatalogueLastInternalVersion() throws SOAPException {

		ExportCatalogueFile request = new ExportCatalogueFile();
		File file = request.exportLastInternalVersion(this.env, this.user, "ACTION");

		assertNotNull(file);
		assertEquals(file.exists(), true);
	}

	@Tag("ExportCatalogueFileTests")
	public void exportNotExistingLog() throws DetailedSOAPException {
		ExportCatalogueFile request = new ExportCatalogueFile();
		File file = request.exportLog(this.env, this.user, "not-existing-code");
		assertEquals(file, null);
	}

	@Tag("ExportCatalogueFileTests")
	@Test
	public void exportExistingLog() throws SOAPException {
		ExportCatalogueFile request = new ExportCatalogueFile();
		File file = request.exportLog(this.env, this.user, "20180130_001_WS");
		assertNotNull(file);
		assertEquals(file.exists(), true);
	}

	@Tag("UploadCatalogueFileTests")
	public void uploadNotExistingCatalogueFile() throws SOAPException, IOException {
		UploadCatalogueFile request = new UploadCatalogueFile();
		assertThrows(IOException.class, () -> request.send(this.env, this.user, new File("not-existing-file.xml")));
	}

	@Tag("UploadCatalogueFileTests")
	@Test
	public void uploadExistingCatalogueFile() throws SOAPException, IOException {

		File file = new File("test-files" + System.getProperty("file.separator") + "unreserve.xml");
		UploadCatalogueFile request = new UploadCatalogueFile();
		String logCode = request.send(this.env, this.user, file);
		assertNotNull(logCode);
	}

	@Tag("UploadCatalogueFileTests")
	@Test
	public void publishMinorNotExistingCatalogue() throws SOAPException, IOException {

		UploadCatalogueFileImpl ucf = new UploadCatalogueFileImpl();

		IPendingRequest request = ucf.publish(this.user, this.env, PublishLevel.MINOR, "not-existing-code");

		DcfResponse response = request.start(new DcfLogParser());

		assertNotNull(response);
	}

	@Tag("UploadCatalogueFileTests")
	@Test
	public void publishMinorExistingCatalogue() throws SOAPException, IOException {

		UploadCatalogueFileImpl ucf = new UploadCatalogueFileImpl();

		IPendingRequest request = ucf.publish(this.user, this.env, PublishLevel.MINOR, "ACTION");

		DcfResponse response = request.start(new DcfLogParser());

		assertNotNull(response);
	}

	@Tag("UploadCatalogueFileTests")
	@Test
	public void publishMajorNotExistingCatalogue() throws SOAPException, IOException {

		UploadCatalogueFileImpl ucf = new UploadCatalogueFileImpl();

		IPendingRequest request = ucf.publish(this.user, this.env, PublishLevel.MAJOR, "not-existing-code");

		DcfResponse response = request.start(new DcfLogParser());

		assertNotNull(response);
	}

	@Tag("UploadCatalogueFileTests")
	@Test
	public void publishMajorExistingCatalogue() throws SOAPException, IOException {

		UploadCatalogueFileImpl ucf = new UploadCatalogueFileImpl();

		IPendingRequest request = ucf.publish(this.user, this.env, PublishLevel.MAJOR, "ACTION");

		DcfResponse response = request.start(new DcfLogParser());

		assertNotNull(response);
	}

	@Tag("UploadCatalogueFileTests")
	@Test
	public void reserveMinorNotExistingCatalogue() throws SOAPException, IOException {

		UploadCatalogueFileImpl ucf = new UploadCatalogueFileImpl();

		IPendingRequest request = ucf.reserve(this.user, this.env, ReserveLevel.MINOR, "not-existing-code",
				"JUnit test case DCF webservices");

		DcfResponse response = request.start(new DcfLogParser());

		assertNotNull(response);
	}

	@Tag("UploadCatalogueFileTests")
	@Test
	public void reserveMinorExistingCatalogue() throws SOAPException, IOException {

		UploadCatalogueFileImpl ucf = new UploadCatalogueFileImpl();

		IPendingRequest request = ucf.reserve(this.user, this.env, ReserveLevel.MINOR, "ACTION",
				"JUnit test case DCF webservices");

		DcfResponse response = request.start(new DcfLogParser());

		assertNotNull(response);
	}

	@Tag("UploadCatalogueFileTests")
	@Test
	public void reserveMajorNotExistingCatalogue() throws SOAPException, IOException {

		UploadCatalogueFileImpl ucf = new UploadCatalogueFileImpl();

		IPendingRequest request = ucf.reserve(this.user, this.env, ReserveLevel.MAJOR, "not-existing-code",
				"JUnit test case DCF webservices");

		DcfResponse response = request.start(new DcfLogParser());

		assertNotNull(response);
	}

	@Tag("UploadCatalogueFileTests")
	@Test
	public void reserveMajorExistingCatalogue() throws SOAPException, IOException {

		UploadCatalogueFileImpl ucf = new UploadCatalogueFileImpl();

		IPendingRequest request = ucf.reserve(this.user, this.env, ReserveLevel.MAJOR, "ACTION",
				"JUnit test case DCF webservices");

		DcfResponse response = request.start(new DcfLogParser());

		assertNotNull(response);
	}

	@Tag("UploadCatalogueFileTests")
	@Test
	public void unreserveMinorNotExistingCatalogue() throws SOAPException, IOException {

		UploadCatalogueFileImpl ucf = new UploadCatalogueFileImpl();

		IPendingRequest request = ucf.unreserve(this.user, this.env, "not-existing-code",
				"JUnit test case DCF webservices");

		DcfResponse response = request.start(new DcfLogParser());

		assertNotNull(response);
	}

	@Tag("UploadCatalogueFileTests")
	@Test
	public void unreserveMinorExistingCatalogue() throws SOAPException, IOException {

		UploadCatalogueFileImpl ucf = new UploadCatalogueFileImpl();

		IPendingRequest request = ucf.unreserve(this.user, this.env, "ACTION", "JUnit test case DCF webservices");

		DcfResponse response = request.start(new DcfLogParser());

		assertNotNull(response);
	}

	@Tag("GetDataCollectionConfigTests")
	@Test
	public void getNotExistingDCConfig() throws SOAPException, IOException, XMLStreamException {

		IDcfDCTableLists<DcfDCTable> output = new DcfDCTablesList();
		GetDataCollectionTables<DcfDCTable> req = new GetDataCollectionTables<>();
		req.getTables(this.env, this.user, "not-existing-code", output);

		assertNull(output);
	}

	@Tag("GetDataCollectionConfigTests")
	@Test
	public void getExistingDCConfig() throws SOAPException, IOException, XMLStreamException {

		IDcfDCTableLists<DcfDCTable> output = new DcfDCTablesList();
		GetDataCollectionTables<DcfDCTable> req = new GetDataCollectionTables<>();
		req.getTables(this.env, this.user, "05_220", output);

		assertNotNull(output);
	}
}
