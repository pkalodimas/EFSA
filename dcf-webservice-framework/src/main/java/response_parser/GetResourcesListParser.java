package response_parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import resource.IDcfResourceReference;
import resource.IDcfResourcesList;

public class GetResourcesListParser<T extends IDcfResourceReference> {
	
	private static final Logger LOGGER = LogManager.getLogger(GetResourcesListParser.class);
	
	private static final String RESOURCE_REF_NODE = "resourceReference";
	private static final String RESOURCE_TYPE_NODE = "resourceType";
	private static final String RESOURCE_ID_NODE = "resourceId";
	
	private IDcfResourcesList<T> output;
	
	public GetResourcesListParser(IDcfResourcesList<T> output) {
		this.output = output;
	}
	
	/**
	 * get the resources from the document
	 * @param document
	 * @return
	 */
	public IDcfResourcesList<T> parse(Document cdata) {
		
		NodeList refs = cdata.getElementsByTagName(RESOURCE_REF_NODE);
		
		for (int i = 0; i < refs.getLength(); ++i) {
			Node ref = refs.item(i);
			this.output.add(getResource(ref));
		}
		
		return this.output;
	}
	
	/**
	 * Get the resource from the node
	 * @param resNode
	 * @return
	 */
	public T getResource(Node resNode) {
		
		NodeList fields = resNode.getChildNodes();
		
		T reference = this.output.create();
		
		for (int i = 0; i < fields.getLength(); ++i) {
			
			Node field = fields.item(i);
			
			String nodeName = field.getNodeName();
			String nodeValue = field.getTextContent();
			
			switch (nodeName) {
			case RESOURCE_TYPE_NODE:
				reference.setType(nodeValue);
				break;
			case RESOURCE_ID_NODE:
				reference.setResourceId(nodeValue);
				break;
			default:
				break;
			}
		}
		
		if (reference.isIncomplete()) {
			LOGGER.warn("Missing reference value for " + reference);
		}
		
		return reference;
	}
}
