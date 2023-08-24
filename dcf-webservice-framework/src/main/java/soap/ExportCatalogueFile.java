package soap;

import java.io.File;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import config.Environment;
import soap_interface.IExportCatalogueFile;
import user.IDcfUser;

/**
 * Export catalogue file web service. See GDE2 and DCF manuals to
 * see all the available options. Here we can download a file from
 * the dcf, as an internal version of a catalogue or a log.
 * The class is also used for allowing the login to openapi portal by downloading
 * the catusers dump catalogue
 * @author avonva
 * @author shahaal
 *
 */
public class ExportCatalogueFile extends SOAPRequest implements IExportCatalogueFile {
	
	private static final String NAMESPACE = "http://ws.catalog.dc.efsa.europa.eu/";
	private static final String URL = "https://dcf-cms.efsa.europa.eu/catalogues/?wsdl";
	private static final String OPENAPI_URL = "https://openapi.efsa.europa.eu/api/catalogues.soap";
	private static final String TEST_URL = "https://dcf-01.efsa.test/dc-catalog-public-ws/catalogues/?wsdl";
	
	// export types used in the request
	private static final String EXPORT_TYPE_CATALOGUE = "catalogFullDefinition";
	private static final String EXPORT_TYPE_LOG = "log";
	private static final String EXPORT_TYPE_INTERNAL_VERSION = "catalogInternalVersion";
	
	private static final String XML_FILE_TYPE = "XML";
	
	private String catalogueCode;
	private String exportType;
	private String fileType;
	
	public ExportCatalogueFile() {}
	
	@Override
	public File exportCatalogue(Environment env, IDcfUser user, String catalogueCode1) throws DetailedSOAPException {
		
		SOAPConsole.log("ExportCatalogueFile: export last published version of catalogue=" + catalogueCode1, user);
		
		Object log = exportXml(env, user, catalogueCode1, EXPORT_TYPE_CATALOGUE, XML_FILE_TYPE);
		
		if (log != null)
			return (File) log;
		
		return null;
	}

	/**
	 * Download a log file related to an upload catalogue file operation.
	 * @param code the code of the log we want to download
	 * @param filename the file where we want to store the log
	 * @return a File object which points to the log file
	 * @throws SOAPException 
	 */
	@Override
	public File exportLog(Environment env, IDcfUser user, String code) throws DetailedSOAPException {
		
		SOAPConsole.log("ExportCatalogueFile: export log=" + code, user);
		
		Object log = exportXml(env, user, code, EXPORT_TYPE_LOG, XML_FILE_TYPE);
		
		if (log != null)
			return (File) log;
		
		return null;
	}
	
	/**
	 * Export the last internal version of the catalogue.
	 * @param catalogueCode1 the code of the catalogue we want to consider
	 * @param filename the file where we want to store the downloaded catalogue
	 * @return a File object which points to the downloaded catalogue .xml file
	 * @throws SOAPException
	 */
	@Override
	public File exportLastInternalVersion(Environment env, IDcfUser user, String catalogueCode1) 
			throws SOAPException {
		
		SOAPConsole.log("ExportCatalogueFile: export last internal version of catalogue=" 
				+ catalogueCode1, user);
		
		Object lastVersion = exportXml(env, user, catalogueCode1, 
				EXPORT_TYPE_INTERNAL_VERSION, 
				XML_FILE_TYPE);
		
		if (lastVersion != null)
			return (File) lastVersion;
		
		return null;
	}
	
	/**
	 * Export an xml file given the code and export type fields
	 * to be inserted in the request.
	 * @param code code of the object we are considering as the
	 * catalogue code or the log code
	 * @param exportType1 the export type (see GDE2)
	 * @param fileType1 the type of the attachment we want
	 * @param flag if loggin using openapi
	 * @return an object containing the xml structure
	 * @throws SOAPException 
	 */
	public Object exportXml(Environment env, IDcfUser user, String code, String exportType1, 
			String fileType1) throws DetailedSOAPException {
		
		this.catalogueCode = code;
		this.exportType = exportType1;
		this.fileType = fileType1;

		Object obj = export(env, user);
		return obj;
	}
	
	/**
	 * Make the export request.
	 * @return
	 * @throws SOAPException 
	 */
	private Object export(Environment env, IDcfUser user) throws DetailedSOAPException {
		
		boolean opeanpiUser = user.isOpeanapi();
		
		String url_request = (opeanpiUser ? OPENAPI_URL : URL);
		
		String url = env == Environment.PRODUCTION ? url_request : TEST_URL;
		
		Object obj = makeRequest(env, user, NAMESPACE, url);

		return obj;
	}

	@Override
	public SOAPMessage createRequest(IDcfUser user, String namespace, SOAPConnection con) throws SOAPException {

		boolean opeanpiUser = user.isOpeanapi();
		
		// create the standard structure and get the message
		SOAPMessage request;
		
		if(opeanpiUser)
			request = createOpenapiTemplateSOAPMessage(user, namespace, "ws");
		else
			request = createTemplateSOAPMessage(user, namespace, "ws");
		
		// get the body of the message
		SOAPBody soapBody = request.getSOAPPart().getEnvelope().getBody();

		// export catalogue file node
		SOAPElement export = soapBody.addChildElement("ExportCatalogueFile", "ws");

		// add the catalogue code to the xml if there is one
		if (this.catalogueCode != null) {
			SOAPElement catCodeNode = export.addChildElement("catalogueCode");
			catCodeNode.setValue(this.catalogueCode);
		}
		
		// add the catalogue code to the xml if there is one
		if (this.exportType != null) {
			SOAPElement exportTypeNode = export.addChildElement("exportType");
			exportTypeNode.setValue(this.exportType);
		}
		
		// add the file type if required
		if (this.fileType != null) {
			SOAPElement fileTypeNode = export.addChildElement("fileType");
			fileTypeNode.setValue(this.fileType);
		}
		
		// save the changes
		request.saveChanges();
		
		return request;
	}

	@Override
	public Object processResponse(SOAPMessage soapResponse) throws SOAPException {
		
		File response = null;
		
		// process the response based
		// on the export type field
		switch (this.exportType) {
		case EXPORT_TYPE_LOG:
			response = writeXmlIntoFile(soapResponse, true);
			break;
		case EXPORT_TYPE_INTERNAL_VERSION:
		case EXPORT_TYPE_CATALOGUE:
			response = writeXmlIntoFile(soapResponse, false);
			break;
		default:
			break;
		}

		return response;
	}
}
