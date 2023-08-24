package soap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import message.MessageResponse;
import response_parser.SendMessageParser;
import soap_interface.ISendMessage;
import user.IDcfUser;

/**
 * SendMessage request to the dcf
 * @author avonva
 * @author shahaal
 *
 */
public class SendMessage extends SOAPRequest implements ISendMessage {
	
	private static final Logger LOGGER = LogManager.getLogger(SendMessage.class);

	private static final String URL = "https://dcf-elect.efsa.europa.eu/elect2/";
	private static final String TEST_URL = "https://dcf-01.efsa.test/dcf-dp-ws/elect2/?wsdl";
	private static final String NAMESPACE = "http://dcf-elect.efsa.europa.eu/";
	
	private String messageName;
	private String message;
	
	/**
	 * Send a dataset to the dcf
	 * @param filename
	 * @throws IOException 
	 */
	@Override
	public MessageResponse send(Environment env, IDcfUser user, File file) throws DetailedSOAPException, IOException {
		
		SOAPConsole.log("SendMessage: file=" + file, user);

		if (!file.exists())
			throw new IOException("The file=" + file + " does not exist");
		
		this.messageName = file.getName();
		this.message = prepareMessage(file);
		
		LOGGER.info("File to be processed: " + file);
		
		String url = env == Environment.PRODUCTION ? URL : TEST_URL;
	
		Object response = makeRequest(env, user, NAMESPACE, url);
		
		LOGGER.info("Request has been created successfully");
		
		SOAPConsole.log("SendMessage:", response);
		
		if (response == null)
			return null;
		
		return (MessageResponse) response;
	}
	
	/**
	 * Create the message from the file
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static String prepareMessage(File file) throws IOException {
		
		// add attachment to fileHandler node
		Path path = Paths.get(file.getAbsolutePath());
		
		// read the file as byte array and encode it in base64
		byte[] data = Files.readAllBytes(path);
		String sData = new String(data);
		LOGGER.info("Xml content : " + sData);
		byte[] encodedData = Base64.getEncoder().encode(data);
		return new String(encodedData);
	}

	@Override
	public SOAPMessage createRequest(IDcfUser user, String namespace, SOAPConnection con) throws SOAPException {
		
		// create the standard structure and get the message
		SOAPMessage request = createTemplateSOAPMessage(user, namespace, "dcf");

        SOAPBody soapBody = request.getSOAPPart().getEnvelope().getBody();
		
		SOAPElement soapElem = soapBody.addChildElement("SendMessage", "dcf");

		SOAPElement trxFileMessage = soapElem.addChildElement("trxFileMessage");
		
		SOAPElement fileHandler = trxFileMessage.addChildElement("fileHandler");

		// set attachment
		SOAPElement arg = trxFileMessage.addChildElement("fileName");
		arg.setTextContent(this.messageName);
		fileHandler.setValue(this.message);
		
		// save the changes in the message and return it
		request.saveChanges();
		
		return request;
	}

	@Override
	public Object processResponse(SOAPMessage soapResponse) throws SOAPException {
		return SendMessageParser.parse(soapResponse.getSOAPBody());
	}
}
