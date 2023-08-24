package soap;

import java.io.File;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import config.Environment;
import soap_interface.IGetDataset;
import user.IDcfUser;

/**
 * Generic get file request to the dcf.
 * @author avonva
 * @author shahaal
 *
 */
public class GetDataset extends SOAPRequest implements IGetDataset {

	private static final String NAMESPACE = "http://dcf-elect.efsa.europa.eu/";
	private static final String URL = "https://dcf-elect.efsa.europa.eu/elect2";
	private static final String TEST_URL = "https://dcf-01.efsa.test/dcf-dp-ws/elect2/?wsdl";
	
	private String datasetId;
	
	/**
	 * Get an handle to the downloaded dataset
	 * @return
	 * @throws SOAPException
	 */
	@Override
	public File getDatasetFile(Environment env, IDcfUser user, String datasetId1) throws DetailedSOAPException {
		
		this.datasetId = datasetId1;
		
		SOAPConsole.log("GetDataset: datasetId=" + datasetId1, user);

		String url = env == Environment.PRODUCTION ? URL : TEST_URL;
		Object response = makeRequest(env, user, NAMESPACE, url);
		
		SOAPConsole.log("GetDataset:", response);
		
		if (response == null)
			return null;
		
		return (File) response;
	}
	
	/**
	 * Get the url for making get file requests
	 * @return
	 */
	public static String getUrl() {
		return URL;
	}
	
	@Override
	public SOAPMessage createRequest(IDcfUser user, String namespace, SOAPConnection con) throws SOAPException {

		// create the standard structure and get the message
		SOAPMessage soapMsg = createTemplateSOAPMessage(user, namespace, "dcf");
		SOAPBody soapBody = soapMsg.getSOAPPart().getEnvelope().getBody();
		SOAPElement soapElem = soapBody.addChildElement("getDataset", "dcf");

		// add resource id
		SOAPElement arg = soapElem.addChildElement("datasetId");
		arg.setTextContent(this.datasetId);

		// save the changes in the message and return it
		soapMsg.saveChanges();

		return soapMsg;
	}

	@Override
	public Object processResponse(SOAPMessage soapResponse) throws SOAPException {
		return writeZippedAttachment(soapResponse, ".xml");
	}
}
