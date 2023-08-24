package soap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import catalogue.DcfCataloguesList;
import catalogue.IDcfCatalogue;
import catalogue.IDcfCataloguesList;
import config.Environment;
import http.HttpManager;
import proxy.ProxyConfigException;
import response_parser.GetCataloguesListParser;
import response_parser.IDcfList;
import user.IDcfUser;
import utils.FileUtils;
import zip_manager.ZipManager;

/**
 * Abstract class used to create soap requests and to process soap responses.
 * 
 * @author avonva
 * @author shahaal
 */
public class GetCataloguesListOpenAPI<T extends IDcfCatalogue> {

	private static final Logger LOGGER = LogManager.getLogger(GetCataloguesListOpenAPI.class);

	private IDcfUser user;
	private String namespace;
	private SOAPError error; // error, if occurred
	private Environment env;

	private IDcfCataloguesList<T> output;

	/**
	 * Set the url where we make the request and the namespace of the request
	 * 
	 * @param url
	 * @param namespace
	 */
	public GetCataloguesListOpenAPI(Environment env, IDcfCataloguesList<T> output) {
		this.env = env;
		this.namespace = "http://ws.catalog.dc.efsa.europa.eu/";
		this.output = output;
	}

	public Environment getEnvironment() {
		return this.env;
	}

	@SuppressWarnings("unchecked")
	public IDcfList<T> getList() throws DetailedSOAPException {
		return (IDcfList<T>) makeRequest("https://openapi.efsa.europa.eu/catalogues");
	}

	/**
	 * Use the proxy if present
	 * 
	 * @param stringUrl
	 * @return
	 * @throws MalformedURLException
	 * @throws ProxyConfigException
	 */
	private static URL getEndpoint(String stringUrl) throws MalformedURLException, ProxyConfigException {

		Proxy proxy = HttpManager.getProxy();

		URL endpoint = new URL(null, stringUrl, new URLStreamHandler() {

			@Override
			protected URLConnection openConnection(URL url) throws IOException {

				// The url is the parent of this stream handler, so must
				// create clone
				URL clone = new URL(url.toString());

				URLConnection connection = null;

				// set the proxy if needed
				if (proxy == null)
					connection = clone.openConnection();
				else
					connection = clone.openConnection(proxy);

				connection.setConnectTimeout(30000);
				connection.setReadTimeout(30000);
				return connection;
			}
		});

		return endpoint;
	}

	/**
	 * Get an https connection which ignores certificates
	 * 
	 * @param url
	 * @return
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private static HttpsURLConnection avoidCertificates(String url)
			throws KeyManagementException, NoSuchAlgorithmException, IOException {

		HttpsURLConnection httpsConnection = null;

		// Create SSL context and trust all certificates
		SSLContext sslContext = SSLContext.getInstance("SSL");

		TrustManager[] trustAll = new TrustManager[] { new TrustAllCertificates() };

		sslContext.init(null, trustAll, new java.security.SecureRandom());

		// Set trust all certificates context to HttpsURLConnection
		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

		// Open HTTPS connection
		URL link = new URL(url);

		httpsConnection = (HttpsURLConnection) link.openConnection();

		// Trust all hosts
		httpsConnection.setHostnameVerifier(new TrustAllHosts());

		// Connect
		httpsConnection.connect();

		return httpsConnection;
	}

	/**
	 * Dummy class implementing HostnameVerifier to trust all host names
	 */
	private static class TrustAllHosts implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	/**
	 * Dummy class implementing X509TrustManager to trust all certificates
	 */
	private static class TrustAllCertificates implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] certs, String authType) {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] certs, String authType) {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

	/**
	 * Create the request and get the response. Process the response and return the
	 * results.
	 * 
	 * @param soapConnection
	 * @return
	 * @throws DetailedSOAPException
	 */
	public Object makeRequest(String url) throws DetailedSOAPException {

		final boolean isHttps = url.toLowerCase().startsWith("https");

		HttpsURLConnection httpsConnection = null;

		// if https with test => skip certificates
		if (isHttps && this.env == Environment.TEST) {
			try {
				httpsConnection = avoidCertificates(url);
			} catch (KeyManagementException | NoSuchAlgorithmException | IOException e) {
				e.printStackTrace();
			}
		}

		try {
			// Connect to the url
			SOAPConnectionFactory connectionFactory = SOAPConnectionFactory.newInstance();

			// open the connection
			SOAPConnection soapConnection = connectionFactory.createConnection();

			// create the request message
			SOAPMessage request = createRequest();

			SOAPMessage response;
			try {

				URL endpoint = getEndpoint(url);

				// get the response
				response = soapConnection.call(request, endpoint);

			} catch (MalformedURLException | ProxyConfigException e) {
				e.printStackTrace();

				LOGGER.error("ERROR OCCURRED. Proceeding with NO_PROXY.", e);

				// get the response with no proxy
				response = soapConnection.call(request, url);
			}

			// close the soap connection
			soapConnection.close();

			// if https with test => skip certificates
			if (httpsConnection != null) {
				httpsConnection.disconnect();
			}

			// parse the response and get the result
			return processResponse(response);
		} catch (SOAPException e) {

			// parse error codes
			throw new DetailedSOAPException(e);
		}
	}

	/**
	 * Get the error type if it occurred
	 * 
	 * @return
	 */
	public SOAPError getSOAPError() {
		return this.error;
	}

	/**
	 * Create the standard structure of a SOAPMessage, including the authentication
	 * block
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws SOAPException
	 */
	public SOAPMessage createTemplateSOAPMessage(String prefix) throws SOAPException {

		// create the soap message
		MessageFactory msgFactory = MessageFactory.newInstance();
		SOAPMessage soapMsg = msgFactory.createMessage();
		SOAPPart soapPart = soapMsg.getSOAPPart();

		// add the content type header
		soapMsg.getMimeHeaders().addHeader("Content-Type", "text/xml;charset=UTF-8");
		soapMsg.getMimeHeaders().addHeader("Ocp-Apim-Subscription-Key", "842d0d2edbeb4b1db69b841e535d2f4f");

		// create the envelope and name it
		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.addNamespaceDeclaration(prefix, this.namespace);

		// return the message
		return soapMsg;
	}

	/**
	 * Get an xml document starting from a string text formatted as xml
	 * 
	 * @param xml
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws Exception
	 */
	public static Document getDocument(String xml) throws ParserConfigurationException, SAXException, IOException {

		// create the factory object to create the document object
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// get the builder from the factory
		DocumentBuilder builder = factory.newDocumentBuilder();

		// Set the input source (the text string)
		InputSource is = new InputSource(new StringReader(xml));

		// get the xml document and return it
		return builder.parse(is);
	}

	/**
	 * Load an xml document using a file
	 * 
	 * @param file
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document getDocument(File file) throws ParserConfigurationException, SAXException, IOException {

		// create the factory object to create the document object
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// get the builder from the factory
		DocumentBuilder builder = factory.newDocumentBuilder();

		// get the xml document and return it
		return builder.parse(file);
	}

	/**
	 * Get a document from an input stream
	 * 
	 * @param input
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document getDocument(InputStream input)
			throws ParserConfigurationException, SAXException, IOException {

		// create the factory object to create the document object
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// get the builder from the factory
		DocumentBuilder builder = factory.newDocumentBuilder();

		// get the xml document and return it
		return builder.parse(input);
	}

	/**
	 * Convert the cdata content of a node into document
	 * 
	 * @param node
	 * @return
	 */
	public static Document getCData(Node node) {

		Document cdata;
		try {
			cdata = getDocument(node.getFirstChild().getNodeValue());
		} catch (DOMException | ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			return null;
		}

		return cdata;
	}

	/**
	 * Get the first attachment of the message
	 * 
	 * @param message
	 * @return
	 * @throws SOAPException
	 */
	public static AttachmentPart getFirstAttachmentPart(SOAPMessage message) {

		// get the attachment
		Iterator<?> iter = message.getAttachments();

		if (!iter.hasNext()) {
			return null;
		}

		// get the response attachment
		AttachmentPart attachment = (AttachmentPart) iter.next();

		return attachment;
	}

	/**
	 * Process an xml attachment without binding it into a dom Document
	 * 
	 * @param soapResponse
	 * @return the input stream containing the xml
	 * @throws SOAPException
	 */
	public static File writeXmlIntoFile(SOAPMessage soapResponse, boolean isZipped) throws SOAPException {

		AttachmentPart part = getFirstAttachmentPart(soapResponse);

		// if no attachment => errors in processing response, return null
		if (part == null) {
			LOGGER.info("No attachment found!");
			return null;
		}

		File file = FileUtils.createTempFile("attachment_" + System.currentTimeMillis(), ".xml");

		// create an attachment handler to analyze the soap attachment
		try (AttachmentHandler handler = new AttachmentHandler(part, isZipped);
				InputStream inputStream = handler.readAttachment();
				OutputStream outputStream = new FileOutputStream(file);) {

			byte[] buf = new byte[512];
			int num;

			// write file
			while ((num = inputStream.read(buf)) != -1)
				outputStream.write(buf, 0, num);

			outputStream.close();
			inputStream.close();
			handler.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return file;
	}

	/**
	 * Write a zipped stream into the disk
	 * 
	 * @param message          message containing the attachment
	 * @param attachmentFormat format of the file in the zip file
	 * @return
	 * @throws SOAPException
	 */
	public static File writeZippedAttachment(SOAPMessage message, String attachmentFormat) throws SOAPException {

		AttachmentPart attachmentPart = getFirstAttachmentPart(message);

		if (attachmentPart == null)
			return null;

		File file = FileUtils.createTempFile("attachment_" + System.currentTimeMillis(), attachmentFormat);

		try (InputStream stream = attachmentPart.getRawContent()) {

			if (stream == null) {
				LOGGER.error("No raw contents in the attachment found");
				return null;
			}

			// unzip the stream into a file
			ZipManager.unzipStream(stream, file);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return file;
	}

	/**
	 * Get the xsd from the attachment
	 * 
	 * @param response
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassCastException
	 * @throws SOAPException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static Document getXsdAttachment(SOAPMessage response)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException,
			SOAPException, IOException, ParserConfigurationException, SAXException {

		AttachmentPart part = getFirstAttachmentPart(response);

		if (part == null)
			return null;

		try (InputStream stream = part.getRawContent()) {

			// parse the document
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(stream);

			return doc;
		}
	}

	protected static Document fileToXsd(File file) throws SAXException, IOException, ParserConfigurationException {

		if (!file.exists())
			return null;

		// parse the document
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(file);

		return doc;
	}

	/**
	 * Write the first attachment of the response
	 * 
	 * @param response
	 * @param file
	 * @throws SOAPException
	 * @throws IOException
	 */
	public static File writeAttachment(SOAPMessage response) throws SOAPException, IOException {

		File file = FileUtils.createTempFile("attachment_" + System.currentTimeMillis(), "");

		// get the attachment
		AttachmentPart attachment = getFirstAttachmentPart(response);

		if (attachment == null)
			return null;

		// read attachment and write it
		try (InputStream inputStream = attachment.getRawContent();
				OutputStream outputStream = new FileOutputStream(file)) {

			byte[] buf = new byte[512];
			int num;

			// write file
			while ((num = inputStream.read(buf)) != -1) {
				outputStream.write(buf, 0, num);
			}

			return file;
		}
	}

	/**
	 * get the first xml attachment in a dom document
	 * 
	 * @param message
	 * @return
	 * @throws SOAPException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document getFirstXmlAttachment(SOAPMessage message)
			throws SOAPException, ParserConfigurationException, SAXException, IOException {

		// get the stream
		try (InputStream stream = getFirstRawAttachment(message)) {

			if (stream == null)
				return null;

			// parse the stream and get the document
			Document xml = getDocument(stream);

			return xml;
		}
	}

	/*
	 * get the attachment raw format
	 */
	public static InputStream getFirstRawAttachment(SOAPMessage message) throws DetailedSOAPException {

		AttachmentPart part = getFirstAttachmentPart(message);

		if (part == null)
			return null;

		// get the stream
		InputStream stream;
		try {
			stream = part.getRawContent();
		} catch (SOAPException e) {
			e.printStackTrace();
			throw new DetailedSOAPException(e);
		}

		return stream;
	}

	public IDcfUser getUser() {
		return this.user;
	}

	public SOAPMessage createRequest() throws SOAPException {

		// create the standard structure and get the message
		SOAPMessage soapMsg = createTemplateSOAPMessage("ws");

		// get the body of the message
		SOAPBody soapBody = soapMsg.getSOAPPart().getEnvelope().getBody();

		// create the xml message structure to get the dataset list
		SOAPElement soapElem = soapBody.addChildElement("getCatalogueList", "ws");

		// set that we want an XML file as output
		SOAPElement arg = soapElem.addChildElement("arg2");
		arg.setTextContent("XML");

		// save the changes in the message and return it
		soapMsg.saveChanges();

		return soapMsg;
	}

	public Object processResponse(SOAPMessage soapResponse) throws SOAPException {

		// get the children of the body
		NodeList returnNodes = soapResponse.getSOAPPart().getEnvelope().getBody().getElementsByTagName("return");

		if (returnNodes.getLength() == 0) {
			LOGGER.error("GetList: no return node was found in the soap response");
			return null;
		}

		// get return node
		Node returnNode = returnNodes.item(0);

		// return the parsed cdata field
		Document cdata = getCData(returnNode);

		if (cdata == null) {
			LOGGER.error("GetList: no cdata was found in the soap response");
			return null;
		}

		return getList(cdata);
	}

	public IDcfCataloguesList<T> getList(Document cdata) {
		GetCataloguesListParser<T> parser = new GetCataloguesListParser<>(this.output);
		return parser.parse(cdata);
	}

	public static void main(String[] args) throws DetailedSOAPException {
		IDcfCataloguesList<IDcfCatalogue> output = new DcfCataloguesList();
		GetCataloguesListOpenAPI<IDcfCatalogue> request = new GetCataloguesListOpenAPI<>(Environment.PRODUCTION,
				output);

		request.getList();

		System.out.println(output);
	}
}
