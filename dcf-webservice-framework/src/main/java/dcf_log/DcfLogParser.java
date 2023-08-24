package dcf_log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class used to parse a dcf log document and retrieve
 * all the {@link LogNode} contained in it.
 * @author avonva
 * @author shahaal
 *
 */
public class DcfLogParser implements IDcfLogParser {

	//private static final String ROOT = "transmissionResult";
	
	private static final String ACTION = "action";
	private static final String TRANSMISSION_DATE = "transmissionDateTime";
	private static final String PROCESSING_DATE = "processingDateTime";
	private static final String UPLOADED_FILENAME = "uploadedFileName";
	private static final String CATALOGUE_CODE = "catalogueCode";
	private static final String CATALOGUE_VERSION = "catalogueVersion";
	private static final String CATALOGUE_STATUS = "catalogueStatus";
	private static final String MACRO_OP_NAME = "macroOperationName";
	private static final String MACRO_OP_RESULT = "macroOperationResult";
	private static final String MACRO_OP_LOGS_BLOCK = "macroOperationLogs";
	private static final String MACRO_OP_LOG = "operationLog";
	
	private static final String OPERATIONS_BLOCK = "operations";
	private static final String OPERATION_BLOCK = "operation";
	private static final String OP_NAME = "operationName";
	private static final String OP_RESULT = "operationResult";
	private static final String OP_LOGS_BLOCK = "operationLogs";
	private static final String OP_LOG = "operationLog";
	
	private static final String VALIDATION_ERROR_BLOCK = "validationErrorLogs";
	private static final String VALIDATION_ERROR = "validationError";
	
	/**
	 * Parse the log document and retrieve all the operation log nodes
	 * @param file
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	@Override
	public DcfLog parse(File file) throws IOException {
		
		try(InputStream input = new FileInputStream(file);) {
			return this.parse(input);
		}
	}

	@Override
	public DcfLog parse(InputStream input) throws IOException {
		
		Document document;
		try {
			document = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().parse(input);
		} catch (SAXException | ParserConfigurationException e) {
			e.printStackTrace();
			throw new IOException(e);  // follow interface
		}
		
		DcfLogBuilder logBuilder = new DcfLogBuilder();
		logBuilder.setAction(getSingleNodeText(document, ACTION));
		logBuilder.setTransmissionDate(getSingleNodeText(document, TRANSMISSION_DATE));
		logBuilder.setUploadedFilename(getSingleNodeText(document, UPLOADED_FILENAME));
		logBuilder.setProcessingDate(getSingleNodeText(document, PROCESSING_DATE));
		logBuilder.setCatalogueCode(getSingleNodeText(document, CATALOGUE_CODE));
		logBuilder.setCatalogueVersion(getSingleNodeText(document, CATALOGUE_VERSION));
		logBuilder.setCatalogueStatus(getSingleNodeText(document, CATALOGUE_STATUS));
		logBuilder.setMacroOpName(getSingleNodeText(document, MACRO_OP_NAME));
		logBuilder.setMacroOpResult(getSingleNodeText(document, MACRO_OP_RESULT));
		
		logBuilder.setMacroOpLogs(getMacroOperations(document));
		logBuilder.setLogNodes(getOperations(document));
		logBuilder.setValidationErrors(getValidationErrors(document));
		
		return logBuilder.build();
	}
	
	private static Collection<String> getMacroOperations(Document document) {
		
		Collection<String> ops = new ArrayList<>();
		
		NodeList nodes = document.getElementsByTagName(MACRO_OP_LOGS_BLOCK);
		
		if (nodes.getLength() > 0) {
			NodeList macroOps = nodes.item(0).getChildNodes();
			
			for (int i = 0; i < macroOps.getLength(); ++i) {
				if (macroOps.item(i).getNodeName().equals(MACRO_OP_LOG))
					ops.add(macroOps.item(i).getTextContent());
			}
		}
		
		return ops;
	}
	
	private static Collection<LogNode> getOperations(Document document) {
		
		Collection<LogNode> ops = new ArrayList<>();
		
		NodeList operations = document.getElementsByTagName(OPERATIONS_BLOCK);
		
		if (operations.getLength() > 0) {
			NodeList operationNodes = operations.item(0).getChildNodes();
			
			for (int i = 0; i < operationNodes.getLength(); ++i) {
				if (operationNodes.item(i).getNodeName().equals(OPERATION_BLOCK))
					ops.add(getOperationNode(operationNodes.item(i)));
			}
		}
		
		return ops;
	}
	
	private static LogNode getOperationNode(Node operationNode) {

		NodeList data = operationNode.getChildNodes();

		LogNodeBuilder builder = new LogNodeBuilder();
		for (int j = 0; j < data.getLength(); ++j) {

			String text = data.item(j).getTextContent();

			switch (data.item(j).getNodeName()) {
			case OP_NAME:
				builder.setName(text);
				break;
			case OP_RESULT:
				builder.setResult(text);
				break;
			case OP_LOGS_BLOCK:
				builder.setOpLogs(getOperationLogs(data.item(j)));
				break;
			default:
				break;
			}
		}

		return builder.build();
	}
	
	private static Collection<String> getOperationLogs(Node operationLogsNode) {
		
		Collection<String> opLogs = new ArrayList<>();
		
		NodeList data = operationLogsNode.getChildNodes();
		
		for (int j = 0; j < data.getLength(); ++j) {
			
			switch (data.item(j).getNodeName()) {
			case OP_LOG:
				opLogs.add(data.item(j).getTextContent());
				break;
			default:
				break;
			}
		}
		
		return opLogs;
	}
	
	private static Collection<LogNode> getValidationErrors(Document document) {
		
		Collection<LogNode> errors = new ArrayList<>();
		
		NodeList nodes = document.getElementsByTagName(VALIDATION_ERROR_BLOCK);
		
		if (nodes.getLength() > 0) {
			NodeList validationErrors = nodes.item(0).getChildNodes();

			for (int i = 0; i < validationErrors.getLength(); ++i) {
				if (validationErrors.item(i).getNodeName().equals(VALIDATION_ERROR))
					errors.add(getValidationError(validationErrors.item(i)));
			}
		}

		return errors;
	}
	
	private static LogNode getValidationError(Node validationNode) {
		
		LogNodeBuilder builder = new LogNodeBuilder();
		builder.setResult(DcfResponse.ERROR);
		builder.setName(VALIDATION_ERROR);
		
		NodeList data = validationNode.getChildNodes();
		
		for (int i = 0; i < data.getLength(); ++i) {
			builder.addOpLog(data.item(i).getTextContent());
		}
		
		return builder.build();
	}
	
	private static String getSingleNodeText(Document document, String node) {
		
		NodeList list = document.getElementsByTagName(node);
		
		if (list.getLength() == 0)
			return null;
		
		return list.item(0).getTextContent();
	}
}
