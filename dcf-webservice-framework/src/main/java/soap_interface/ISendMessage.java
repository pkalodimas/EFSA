package soap_interface;

import java.io.File;
import java.io.IOException;

import config.Environment;
import message.MessageResponse;
import soap.DetailedSOAPException;
import user.IDcfUser;

/**
 * SendMessage request to the dcf
 * @author avonva
 *
 */
public interface ISendMessage {
	
	/**
	 * Send a dataset to the dcf
	 * @param filename
	 * @throws IOException 
	 */
	public MessageResponse send(Environment env, IDcfUser user, File file) throws DetailedSOAPException, IOException;
}
