package response_parser;

import java.sql.Timestamp;
import java.text.ParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import catalogue.IDcfCatalogue;
import catalogue.IDcfCataloguesList;
import utils.TimeUtils;

public class GetCataloguesListParser<T extends IDcfCatalogue> {

	// date format of the catalogues
	public static final String ISO_8601_24H_FULL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	
	private IDcfCataloguesList<T> output;
	
	public GetCataloguesListParser(IDcfCataloguesList<T> output) {
		this.output = output;
	}
	
	public IDcfCataloguesList<T> parse(Document cdata) {
		
		// get all the catalogues nodes from the CDATA field (which is text, but XML formatted)
		NodeList cataloguesNodes = cdata.getElementsByTagName("catalogue");

		// for each catalogue node get its properties and store it in Catalogue objects
		for (int i = 0; i < cataloguesNodes.getLength(); i++) {

			// create a catalogue builder to build the catalogue step by step
			T catalogue = this.output.create();

			// get the current catalogue node
			Node catalogueNode = cataloguesNodes.item(i);

			// add the catalogueDesc properties (i.e. code, name, label, scopenote, termCodeMask, 
			// termCodeLength, acceptNonStandardCodes, generateMissingCodes)
			catalogue = addProperties(catalogue, catalogueNode.getFirstChild());

			// add the catalogueVersion properties (i.e. version, validTo, status)
			// Note that since we have only 2 nodes, we can get the last child as the second one
			catalogue = addProperties(catalogue, catalogueNode.getLastChild());

			// add the catalogue into the output array
			this.output.add(catalogue);
		}
		
		return this.output;
	}
	
	/**
	 * Extract data of the catalogue from the node and set 
	 * them into the catalogue object passed in input
	 * @param catalogue the catalogue which will be modified
	 * @param node, the parent node which contains children which have catalogue information 
	 * (i.e. catalogueDesc, catalogueVersion)
	 * @return the modified catalogue
	 */
	private T addProperties(T catalogue, 
			Node node) {

		// get the children of the parent node
		NodeList catalogueProperties = node.getChildNodes();

		// For each child (i.e. each property which is son of the parent node)
		for (int j = 0; j < catalogueProperties.getLength(); j++) {

			// get the current property (e.g. code, name, validTo...)
			Node property = catalogueProperties.item(j);

			// get the property name (e.g. "code", "name", ...)
			String propertyName = property.getNodeName();

			// Note: to get the value it is necessary to get 
			// the child first, and then we can get the node value
			// from the child (I don't know why but this is it)
			Node propertyValueNode = property.getFirstChild();

			// skip if no name or value is found (happen if there are catalogue errors)
			if (propertyName == null || propertyValueNode == null)
				continue;

			// here we have the property name and the node which contains
			// the property value => we have to extract the property value
			// and then add it to the catalogue builder

			// Get the value of the property node
			String propertyValue = propertyValueNode.getNodeValue();

			// Add the property value to the catalogue builder
			// according to the property name
			switch (propertyName) {

			case "code":
				catalogue.setCode(propertyValue);
				break;
			case "name":
				catalogue.setName(propertyValue);
				break;
			case "label":
				catalogue.setLabel(propertyValue);
				break;
			case "scopeNote":
				catalogue.setScopenotes(propertyValue);
				break;
			case "termCodeMask":
				catalogue.setTermCodeMask(propertyValue);
				break;
			case "termCodeLength":
				try {
					int codeLength = Integer.parseInt(propertyValue);
					catalogue.setTermCodeLength(codeLength);
				}
				catch (NumberFormatException e) {
					e.printStackTrace();
				}
				break;
			case "acceptNonStandardCodes":
				catalogue.setAcceptNonStandardCodes(Boolean.parseBoolean(propertyValue));
				break;
			case "generateMissingCodes":
				catalogue.setGenerateMissingCodes(Boolean.parseBoolean(propertyValue));
				break;
			case "version":
				catalogue.setVersion(propertyValue);
				break;
			case "validFrom":
				
				if (propertyValue != null) {

					// convert the string to timestamp
					try {
						Timestamp validFromTs = TimeUtils.getTimestampFromString(
								propertyValue, ISO_8601_24H_FULL_FORMAT);
						catalogue.setValidFrom(validFromTs);
					}
					catch (ParseException e) {
						e.printStackTrace();
					}
				}
				
				break;

			case "validTo":

				if (propertyValue != null) {

					// convert the string to timestamp
					try {
						Timestamp validToTs = TimeUtils.getTimestampFromString(
								propertyValue, ISO_8601_24H_FULL_FORMAT);
						catalogue.setValidTo(validToTs);
					}
					catch (ParseException e) {
						e.printStackTrace();
					}
				}

				break;
			case "status":
				catalogue.setStatus(propertyValue);
				break;
			default:
				break;
			}  // end switch
		}  // end for

		return catalogue;  // return the catalogue
	}
}
