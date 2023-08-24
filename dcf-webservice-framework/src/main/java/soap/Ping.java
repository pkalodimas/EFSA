package soap;

import java.util.Iterator;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Node;

import config.Environment;
import user.IDcfUser;

/**
 * Class to manage a ping request to the DCF web service
 * @author shahaal
 * @author avonva
 *
 */
public class Ping extends SOAPRequest {

	// The correct value that the ping should return to be correct
	private static final String PING_CORRECT_VALUE = "TRXOK";
	private static final String PING_NODE_NAME = "PingResponse";
	
	private static final String URL = "https://dcf-elect.efsa.europa.eu/elect2";
	private static final String TEST_URL = "https://dcf-01.efsa.test/dcf-dp-ws/elect2/?wsdl";
	private static final String NAMESPACE = "http://dcf-elect.efsa.europa.eu/";
	
	/**
	 * Make a ping request
	 * @author shahaal
	 * @author avonva
	 * @param env
	 * @param user
	 * @param openapiRequest
	 * @return
	 * @throws DetailedSOAPException
	 */
	public boolean ping(Environment env, IDcfUser user) throws DetailedSOAPException {

		SOAPConsole.log("Ping", user);
		
		boolean check;
		
		String url = env == Environment.PRODUCTION ? URL : TEST_URL;
		
		try {
			check = (boolean) makeRequest(env, user, NAMESPACE, url);
		} catch (SOAPException e) {
			throw new DetailedSOAPException(e);
		}
		
		SOAPConsole.log("Ping:", check);

		return check;
	}
	
	/**
	 * Create a ping request message
	 * 
	 * @param serverURI
	 * @throws SOAPException
	 */
	@Override
	public SOAPMessage createRequest(IDcfUser user, String namespace, SOAPConnection soapConnection) throws SOAPException {

		// create the standard structure and get the message
		SOAPMessage soapMsg = createTemplateSOAPMessage(user, namespace, "dcf");
		
		// get the body of the message
		SOAPBody soapBody = soapMsg.getSOAPPart().getEnvelope().getBody();

		// create the xml message structure to make a ping with SOAP
		soapBody.addChildElement("Ping", "dcf");

		// save the changes in the message and return it
		soapMsg.saveChanges();

		return soapMsg;
	}
	
	/**
	 * Get the ping response message and check if the results is ok (i.e. if the content is TRXOK)
	 * @param soapResponse, the ping response
	 * @param correctPingValue, the correct value that the ping should return if the server is up
	 * @return
	 * @throws SOAPException
	 */
	@Override
	public Object processResponse(SOAPMessage soapResponse) throws SOAPException {
		
		String response = "";
		
		// get the children of the body
		Iterator<?> children = soapResponse.getSOAPPart().getEnvelope().getBody().getChildElements();

		// find the ping response node
		while (children.hasNext()) {
			
			// get the current child of the body
			Node currentChild = (Node) children.next();
			
			// if the node is the ping response, get the PingResponse node
			if (currentChild.getLocalName().equals(PING_NODE_NAME)) {

				// get then the 'return' node from the pingResponse node
				Node child = currentChild.getFirstChild();

				// and get the trxState node from the 'return' node
				child = child.getFirstChild();
				
				// get the trxState content from the node
				response = child.getFirstChild().getNodeValue();
				
				break;
			}
		}
		
		// is it correct or not ?
		return response.equals(PING_CORRECT_VALUE);
	}
}
