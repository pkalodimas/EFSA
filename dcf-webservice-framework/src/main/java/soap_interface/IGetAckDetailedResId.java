package soap_interface;

import javax.xml.soap.SOAPException;

import ack.DcfAckDetailedResId;
import config.Environment;
import soap.DetailedSOAPException;
import user.IDcfUser;

/**
 * Get acknowledge request
 * @author shahaal
 *
 */
public interface IGetAckDetailedResId {
	
	/**
	 * Get the ack detailed res id of a message
	 * @return
	 * @throws SOAPException
	 */
	public DcfAckDetailedResId getAckDetailedResId(Environment env, IDcfUser user, String messageId) throws DetailedSOAPException;
}
