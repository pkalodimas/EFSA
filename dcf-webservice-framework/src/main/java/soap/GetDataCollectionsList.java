package soap;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Document;

import config.Environment;
import data_collection.IDcfDataCollection;
import data_collection.IDcfDataCollectionsList;
import response_parser.GetDataCollectionsListParser;
import response_parser.IDcfList;
import user.IDcfUser;

/**
 * Get a list with all the data collections related to the current user
 * @author avonva
 * @author shahaal
 *
 */
public class GetDataCollectionsList<T extends IDcfDataCollection> extends GetList<T> {

	private static final String NAMESPACE = "http://dcf-elect.efsa.europa.eu/";
	private static final String URL = "https://dcf-elect.efsa.europa.eu/elect2";
	private static final String OPENAPI_URL = "https://openapi.efsa.europa.eu/api/collections.soap";
	private static final String TEST_URL = "https://dcf-01.efsa.test/dcf-dp-ws/elect2/?wsdl";
	
	private IDcfDataCollectionsList<T> output;
	
	public GetDataCollectionsList(boolean openapi) {
		super((openapi ? OPENAPI_URL : URL), TEST_URL, NAMESPACE);
	}
	
	public GetDataCollectionsList() {
		super(URL, TEST_URL, NAMESPACE);
	}

	public IDcfList<T> getList(Environment env, IDcfUser user, IDcfDataCollectionsList<T> output1) throws DetailedSOAPException {
		SOAPConsole.log("GetDataCollectionsList", user);
		
		this.output = output1;
		IDcfList<T> response = super.getList(env, user);
		
		SOAPConsole.log("GetDataCollectionsList:", response);
		
		return response;
	}
	
	@Override
	public IDcfDataCollectionsList<T> getList(Document cdata) {

		GetDataCollectionsListParser<T> parser = new GetDataCollectionsListParser<>(this.output);
		return parser.parse(cdata);
	}

	@Override
	public SOAPMessage createRequest(IDcfUser user, String namespace, SOAPConnection con) throws SOAPException {
		
		boolean openapiUser = user.isOpeanapi();
		
		SOAPMessage soapMsg;
		
		// create the standard structure and get the message
		if(openapiUser)
			soapMsg = createOpenapiTemplateSOAPMessage(user, namespace, "dcf");
		else
			soapMsg = createTemplateSOAPMessage(user, namespace, "dcf");

		// get the body of the message
		SOAPBody soapBody = soapMsg.getSOAPPart().getEnvelope().getBody();

		// create the xml message structure to get the dataset list
		soapBody.addChildElement("GetDataCollectionList", "dcf");

		// save the changes in the message and return it
		soapMsg.saveChanges();

		return soapMsg;
	}
}
