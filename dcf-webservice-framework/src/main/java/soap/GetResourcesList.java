package soap;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Document;

import config.Environment;
import resource.IDcfResourcesList;
import resource.IDcfResourceReference;
import response_parser.GetResourcesListParser;
import response_parser.IDcfList;
import user.IDcfUser;

/**
 * Request to the DCF for getting the resource list
 * @author avonva
 * @author shahaal
 *
 */
public class GetResourcesList<T extends IDcfResourceReference> extends GetList<T> {

	private String dataCollectionCode;
	
	// web service link of the getDatasetList service
	private static final String URL = "https://dcf-elect.efsa.europa.eu/elect2/";
	private static final String OPENAPI_URL = "https://openapi.efsa.europa.eu/api/collections.soap";
	private static final String TEST_URL = "https://dcf-01.efsa.test/dcf-dp-ws/elect2/?wsdl";
	private static final String NAMESPACE = "http://dcf-elect.efsa.europa.eu/";
	
	private IDcfResourcesList<T> output;
	
	// constructor for DCF or openapi users
	public GetResourcesList(boolean openapi) {
		super((openapi ? OPENAPI_URL : URL), TEST_URL, NAMESPACE);
	}
	
	// default constructor (only DCF)
	public GetResourcesList() {
		super(URL, TEST_URL, NAMESPACE);
	}
	
	public IDcfList<T> getList(Environment env, IDcfUser user, String dataCollectionCode1, IDcfResourcesList<T> output1) throws DetailedSOAPException {
		
		this.dataCollectionCode = dataCollectionCode1;
		this.output = output1;
		
		SOAPConsole.log("GetResourcesList: dcCode=" + dataCollectionCode1, user);
		IDcfList<T> response = super.getList(env, user);
		
		SOAPConsole.log("GetResourcesList:", response);
		
		return response;
	}
	
	@Override
	public IDcfResourcesList<T> getList(Document cdata) {
		GetResourcesListParser<T> parser = new GetResourcesListParser<>(this.output);
		return parser.parse(cdata);
	}

	@Override
	public SOAPMessage createRequest(IDcfUser user, String namespace, SOAPConnection con) throws SOAPException {
		
		boolean openapiUser = user.isOpeanapi();
		
		// create the standard structure and get the message
		SOAPMessage request;
		if(openapiUser)
			request = createOpenapiTemplateSOAPMessage(user, namespace, "dcf");
		else
			request = createTemplateSOAPMessage(user, namespace, "dcf");

		SOAPBody soapBody = request.getSOAPPart().getEnvelope().getBody();
		
		SOAPElement soapElem = soapBody.addChildElement("GetResourceList", "dcf");

		SOAPElement arg = soapElem.addChildElement("dataCollection");
		arg.setTextContent(this.dataCollectionCode);

		// save the changes in the message and return it
		request.saveChanges();

		return request;
	}
}
