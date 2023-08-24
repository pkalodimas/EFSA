package soap;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import config.Environment;
import dataset.IDcfDataset;
import dataset.IDcfDatasetsList;
import response_parser.GetDatasetsListParser;
import soap_interface.IGetDatasetsList;
import user.IDcfUser;

/**
 * Get dataset list request for the DCF webservice. It can be used
 * by calling {@link #getList()} to get all the dataset
 * of the current user.
 * @author avonva
 * @author shahaal
 */
public class GetDatasetsList<T extends IDcfDataset> extends SOAPRequest implements IGetDatasetsList<T> {

	// web service link of the getDatasetList service
	private static final String URL = "https://dcf-elect.efsa.europa.eu/elect2/";
	private static final String LIST_NAMESPACE = "http://dcf-elect.efsa.europa.eu/";
	private static final String TEST_URL = "https://dcf-01.efsa.test/dcf-dp-ws/elect2/?wsdl";
	
	private String dataCollectionCode;
	private IDcfDatasetsList<T> output;
	
	/**
	 * Send the request and get the dataset list
	 * @throws SOAPException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IDcfDatasetsList<T> getList(Environment env, IDcfUser user, 
			String dataCollectionCode1, IDcfDatasetsList<T> output1) throws DetailedSOAPException {
		
		SOAPConsole.log("GetDatasetsList: dcCode=" + dataCollectionCode1, user);
		
		this.dataCollectionCode = dataCollectionCode1;
		this.output = output1;
		
		IDcfDatasetsList<T> datasets = null;

		String url = env == Environment.PRODUCTION ? URL : TEST_URL;
		
		Object response = makeRequest(env, user, LIST_NAMESPACE, url);
		
		SOAPConsole.log("GetDatasetsList:", response);
		
		// get the list from the response if possible
		if (response != null) {
			datasets = (IDcfDatasetsList<T>) response;
		}

		return datasets;
	}

	@Override
	public SOAPMessage createRequest(IDcfUser user, String namespace, SOAPConnection con) throws SOAPException {
		
		// create the standard structure and get the message
		SOAPMessage request = createTemplateSOAPMessage(user, namespace, "dcf");

		SOAPBody soapBody = request.getSOAPPart().getEnvelope().getBody();
		
		SOAPElement soapElem = soapBody.addChildElement("getDatasetList", "dcf");

		SOAPElement arg = soapElem.addChildElement("dataCollectionCode");
		arg.setTextContent(this.dataCollectionCode);

		// save the changes in the message and return it
		request.saveChanges();
		
		return request;
	}
	
	@Override
	public Object processResponse(SOAPMessage soapResponse) throws SOAPException {
		// parse the dom document and return the contents
		GetDatasetsListParser<T> parser = new GetDatasetsListParser<>(this.output);
		SOAPBody body = soapResponse.getSOAPPart().getEnvelope().getBody();
		return parser.parse(body);
	}
}
