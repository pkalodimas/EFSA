package soap;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import config.Environment;
import response_parser.IDcfList;
import user.IDcfUser;

/**
 * Generic get list request
 * @author avonva
 * @author shahaal
 * @param <T>
 */
public abstract class GetList<T> extends SOAPRequest {
	
	private static final Logger LOGGER = LogManager.getLogger(GetList.class);
	
	private String url;
	private String testUrl;
	private String namespace;

	/**
	 * 
	 * @param type
	 * @param prodUrl
	 * @param testUrl
	 * @param namespace
	 */
	public GetList(String url, String testUrl, String namespace) {
		this.url = url;
		this.testUrl = testUrl;
		this.namespace = namespace;
	}
	
	/**
	 * Get the list of T elements
	 * @return
	 * @throws DOMException
	 * @throws DetailedSOAPException 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public IDcfList<T> getList(Environment env, IDcfUser user) throws DetailedSOAPException {
		String endpoint = env == Environment.PRODUCTION ? this.url : this.testUrl;
		return (IDcfList<T>) makeRequest(env, user, this.namespace, endpoint);
	}
	
	@Override
	public Object processResponse(SOAPMessage soapResponse) throws SOAPException {
		
		// get the children of the body
		NodeList returnNodes = soapResponse.getSOAPPart().
				getEnvelope().getBody().getElementsByTagName("return");

		if (returnNodes.getLength() == 0) {
			LOGGER.error("GetList: no return node was found in the soap response");
			return null;
		}
		
		// get return node
		Node returnNode = returnNodes.item(0);
		
		// return the parsed data field
		Document cdata = getCData(returnNode);
		
		if (cdata == null) {
			LOGGER.error("GetList: no data was found in the soap response");
			return null;
		}
		
		return getList(cdata);
	}
	
	/**
	 * Parse the cdata content of the received response.
	 * @param cdata the cdata node
	 * @return desired list of objects
	 */
	public abstract IDcfList<T> getList(Document cdata);
	
}
