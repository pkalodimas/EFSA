package soap;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import config.Environment;
import user.IDcfUser;

/**
 * Get an xsd file from the dcf
 * @author avonva
 *
 */
public class GetXsdFile extends GetFile {

	/**
	 * Get the xsd file
	 * @return
	 * @throws SOAPException
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public Document getXsdFile(Environment env, IDcfUser user, String resourceId) throws SOAPException, SAXException, IOException, ParserConfigurationException {
		
		File response = getFile(env, user, resourceId);
		if (response == null)
			return null;
		
		return fileToXsd(response);
	}
}
