package soap;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ack.DcfAck;
import ack.DcfAckDetailedResId;
import ack.DcfAckLog;
import ack.FileState;
import config.Environment;
import soap_interface.IGetAck;
import user.IDcfUser;

/**
 * Get acknowledge request
 * @author avonva
 * @author shahaal
 *
 */
public class GetAck extends SOAPRequest implements IGetAck {

	private static final Logger LOGGER = LogManager.getLogger(GetAck.class);
	
	private static final String URL = "https://dcf-elect.efsa.europa.eu/elect2/";
	private static final String NAMESPACE = "http://dcf-elect.efsa.europa.eu/";
	private static final String TEST_URL = "https://dcf-01.efsa.test/dcf-dp-ws/elect2/?wsdl";
	
	private String messageId;
	private SOAPMessage response;

	
	/**
	 * Get the ack of the message
	 * @return
	 * @throws SOAPException
	 */
	@Override
	public DcfAck getAck(Environment env, IDcfUser user, String messageId1) throws DetailedSOAPException {
		
		this.messageId = messageId1;
		
		SOAPConsole.log("GetAck: messageId=" + messageId1, user);

		String url = env == Environment.PRODUCTION ? URL : TEST_URL;
		Object response1 = makeRequest(env, user, NAMESPACE, url);
		
		SOAPConsole.log("GetAck:", response1);
		
		if (response1 == null)
			return null;
		
		return (DcfAck) response1;
	}
	
	/**
	 * Get the ack of the message
	 * @return
	 * @throws SOAPException
	 */
	@Override
	public DcfAckDetailedResId getAckDetailedResId(Environment env, IDcfUser user, String detailedResId) throws DetailedSOAPException {
		
		this.messageId = detailedResId;
		
		SOAPConsole.log("GetAckDetailedResId: messageId=" + detailedResId, user);

		String url = env == Environment.PRODUCTION ? URL : TEST_URL;
		Object response1 = makeRequest(env, user, NAMESPACE, url);
		
		SOAPConsole.log("GetAckDetailedResId:", response1);
		
		if (response1 == null)
			return null;
		
		return (DcfAckDetailedResId) response1;
	}

	/**
	 * Get the attachment of the ack response
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws SOAPException 
	 */
	public Document getAttachment() throws SOAPException, 
		ParserConfigurationException, SAXException, IOException {
		
		if (this.response == null) {
			LOGGER.error("Cannot get attachment. Make request first!");
			return null;
		}
		
		return getFirstXmlAttachment(this.response);
	}
	
	/**
	 * Get raw attachment of the response
	 * @return
	 * @throws SOAPException
	 */
	public InputStream getRawAttachment() throws DetailedSOAPException {
		
		if (this.response == null) {
			LOGGER.error("Cannot get attachment. Make request first!");
			return null;
		}
		
		return getFirstRawAttachment(this.response);
	}
	
	@Override
	public SOAPMessage createRequest(IDcfUser user, String namespace, SOAPConnection con) throws SOAPException {
		
		// create the standard structure and get the message
		SOAPMessage request = createTemplateSOAPMessage(user, namespace, "dcf");

		SOAPBody soapBody = request.getSOAPPart().getEnvelope().getBody();
		
		SOAPElement soapElem = soapBody.addChildElement("GetAck", "dcf");

		SOAPElement arg = soapElem.addChildElement("messageId");
		arg.setTextContent(this.messageId);

		// save the changes in the message and return it
		request.saveChanges();
		
		return request;
	}

	@Override
	public Object processResponse(SOAPMessage soapResponse) throws SOAPException {
		
		boolean hasFault = hasFaultCode(soapResponse);
		
		// get the state from the response
		FileState state;
		
		if (hasFault)
			state = FileState.EXCEPTION;
		else 
			state = extractState(soapResponse);
		
		DcfAckLog log = null;
		
		// no attachment in these cases
		if (state == FileState.READY) {
			
			log = extractAcklog(soapResponse);
			
			if (log == null)
				LOGGER.warn("Ack ready but no log found for message id: " + this.messageId);
		}

		// create the ack object
		DcfAck ack = new DcfAck(state, log);
		
		return ack;
	}
	
	/**
	 * Check if the ack raised an exception
	 * @param soapResponse
	 * @return
	 * @throws SOAPException
	 */
	private static boolean hasFaultCode(SOAPMessage soapResponse) throws SOAPException {
		
		NodeList fault = soapResponse.getSOAPPart()
				.getEnvelope().getBody().getElementsByTagName("faultcode");

		return (fault.getLength() > 0);
	}
	
	/**
	 * Extract the ack state from the response
	 * @param soapResponse
	 * @return
	 * @throws SOAPException
	 */
	private static FileState extractState(SOAPMessage soapResponse) throws SOAPException {
		
		// get state
		NodeList children = soapResponse.getSOAPPart()
				.getEnvelope().getBody().getElementsByTagName("fileState");

		if (children.getLength() == 0)
			return null;

		Node stateNode = children.item(0);

		String stateText = stateNode.getTextContent();

		if (stateText.isEmpty())
			return null;

		// get the state from the response
		FileState state = FileState.fromString(stateText);
		
		return state;
	}
	
	/**
	 * Extract the ack log from the attachment
	 * @param soapResponse
	 * @return
	 * @throws SOAPException
	 */
	private DcfAckLog extractAcklog(SOAPMessage soapResponse) throws SOAPException {
		
		this.response = soapResponse;
		
		Document attachment;
		try {
			
			attachment = getFirstXmlAttachment(soapResponse);
			
			if (attachment == null)
				return null;
			
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			return null;
		}
		
		// get the ack from the attachment
		DcfAckLog log = new DcfAckLog(attachment);
		log.setRawLog(getFirstRawAttachment(soapResponse));
		
		return log;
	}
}