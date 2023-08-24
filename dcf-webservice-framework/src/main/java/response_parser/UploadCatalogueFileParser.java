package response_parser;

import java.util.Iterator;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Node;

/**
 * Parse the soap response received with an upload catalogue file
 * request.
 * @author avonva
 * @author shahaal
 *
 */
public class UploadCatalogueFileParser {

	/**
	 * Search the log code in the soap message
	 * @return
	 * @throws SOAPException
	 */
	public static String parse(SOAPBody body) throws SOAPException {

		// get the children of the body
		Iterator<?> children = body.getChildElements();
		
		if (!children.hasNext())
			return null;
		
		// get the UploadCatalogueFileResponse node
		Node node = (Node) children.next();
		
		// get the return node
		node = node.getFirstChild();

		// continue only if we have indeed the return node
		if (node == null || !node.getLocalName().equals("return"))
			return null;
		
		// return the text inside the return node
		return node.getTextContent();
	}
}
