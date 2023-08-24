package soap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import config.Environment;
import response_parser.UploadCatalogueFileParser;
import soap_interface.IUploadCatalogueFile;
import user.IDcfUser;

/**
 * Model the upload catalogue file web service. Use the method
 * {@link #getAttachment()} to define the xml attachment which needs
 * to be uploaded to the dcf. Use {@link #processResponse(String)} to
 * process the log code related to the upload catalogue file request.
 * @author avonva
 * @author shahaal
 *
 */
public class UploadCatalogueFile extends SOAPRequest implements IUploadCatalogueFile {

	private static final Logger LOGGER = LogManager.getLogger(UploadCatalogueFile.class);
	
	private static final String NAMESPACE = "http://ws.catalog.dc.efsa.europa.eu/";
	private static final String URL = "https://dcf-cms.efsa.europa.eu/catalogues/?wsdl";
	private static final String TEST_URL = "https://dcf-01.efsa.test/dc-catalog-public-ws/catalogues/?wsdl";
	
	private String attachment;
	
	/**
	 * Upload a file to DCF
	 * @return the code of the log which tracks the request
	 * @throws SOAPException 
	 * @throws IOException 
	 */
	public String send(Environment env, IDcfUser user, File file) throws DetailedSOAPException, IOException {
		
		// read the file into a string
		String data = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));

		return this.send(env, user, data);
	}
	
	/**
	 * Upload an .xml file contained in a string to dcf
	 * @param attachment1
	 * @return
	 * @throws DetailedSOAPException
	 */
	@Override
	public String send(Environment env, IDcfUser user, String attachment1) throws DetailedSOAPException {
		
		SOAPConsole.log("UploadCatalogueFile: attachment=" + attachment1, user);
		
		this.attachment = attachment1;
		
		String url = env == Environment.PRODUCTION ? URL : TEST_URL;
		
		// return the log code got from dcf
		Object response = makeRequest(env, user, NAMESPACE, url);
		
		SOAPConsole.log("UploadCatalogueFile:", response);
		
		if (response == null)
			return null;
		
		return (String) response;
	}

	/**
	 * Create the reserve request message
	 */
	@Override
	public SOAPMessage createRequest(IDcfUser user, String namespace, SOAPConnection con) throws SOAPException {

		// create the standard structure and get the message
		SOAPMessage soapMsg = createTemplateSOAPMessage(user, namespace, "ws");
		
		// get the body of the message
		SOAPBody soapBody = soapMsg.getSOAPPart().getEnvelope().getBody();

		// upload catalogue file node
		SOAPElement upload = soapBody.addChildElement("UploadCatalogueFile", "ws");
		
		// file data node (child of upload cf)
		SOAPElement fileData = upload.addChildElement("fileData");
		
		// get the attachment in base64 format
		String encodedAttachment = encodeAttachment(this.attachment);

		// row data node (child of file data)
		SOAPElement rowData = fileData.addChildElement("rowData");
		rowData.setValue(encodedAttachment);

		// save the changes in the message and return it
		soapMsg.saveChanges();

		return soapMsg;
	}
	
	private static String encodeAttachment(String attachment) {
		byte[] data = attachment.getBytes();
		return new String(Base64.getEncoder().encode(data));
	}
	
	@Override
	public Object processResponse(SOAPMessage soapResponse) throws SOAPException {
		
		// get the log code of the response
		String logCode = UploadCatalogueFileParser.parse(soapResponse.getSOAPBody());
		
		// if errors the log code is not returned
		if (logCode == null)
			LOGGER.error("UploadCatalogueFile: cannot find the log code in the soap response");

		return logCode;
	}
}
