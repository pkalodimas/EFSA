package soap;

import java.io.File;
import java.io.IOException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import config.Environment;
import user.IDcfUser;

/**
 * Generic get file request to the dcf.
 * 
 * @author avonva
 * @author shahaal
 *
 */
public class GetFile extends SOAPRequest {

	private static final String NAMESPACE = "http://dcf-elect.efsa.europa.eu/";
	private static final String URL = "https://dcf-elect.efsa.europa.eu/elect2";
	private static final String OPENAPI_URL = "https://openapi.efsa.europa.eu/api/collections.soap";
	private static final String TEST_URL = "https://dcf-01.efsa.test/dcf-dp-ws/elect2/?wsdl";

	private String resourceId;

	/**
	 * Get a file using its id
	 * 
	 * @param env
	 * @param user
	 * @param resourceId1
	 * @return
	 * @throws SOAPException
	 * @throws IOException
	 */
	public File getFile(Environment env, IDcfUser user, String resourceId1)
			throws SOAPException, IOException {
		
		this.resourceId = resourceId1;
		SOAPConsole.log("GetFile: resourceId=" + resourceId1, user);

		boolean isOpenapi = user.isOpeanapi();
		
		String url = (isOpenapi ? OPENAPI_URL : URL);
		String finalUrl = env == Environment.PRODUCTION ? url : TEST_URL;

		SOAPMessage response = (SOAPMessage) makeRequest(env, user, NAMESPACE, finalUrl);

		File file = writeAttachment(response);

		SOAPConsole.log("GetFile:", file);

		return file;
	}

	@Override
	public SOAPMessage createRequest(IDcfUser user, String namespace, SOAPConnection con) throws SOAPException {
		
		boolean openapiUser = user.isOpeanapi();
		
		// create the standard structure and get the message
		SOAPMessage soapMsg;
		
		if (openapiUser)
			soapMsg = createOpenapiTemplateSOAPMessage(user, namespace, "dcf");
		else
			soapMsg = createTemplateSOAPMessage(user, namespace, "dcf");

		SOAPBody soapBody = soapMsg.getSOAPPart().getEnvelope().getBody();
		SOAPElement soapElem = soapBody.addChildElement("GetFile", "dcf");

		// add resource id
		SOAPElement arg = soapElem.addChildElement("trxResourceId");
		arg.setTextContent(this.resourceId);

		// save the changes in the message and return it
		soapMsg.saveChanges();

		return soapMsg;
	}

	@Override
	public Object processResponse(SOAPMessage soapResponse) throws SOAPException {
		return soapResponse;
	}
}
