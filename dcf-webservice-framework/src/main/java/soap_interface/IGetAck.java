package soap_interface;

import javax.xml.soap.SOAPException;

import ack.DcfAck;
import ack.DcfAckDetailedResId;
import config.Environment;
import soap.DetailedSOAPException;
import user.IDcfUser;

/**
 * Get acknowledge request
 * 
 * @author avonva
 * @author shahaal
 *
 */
public interface IGetAck {

	/**
	 * Get the ack of a message
	 * 
	 * @return
	 * @throws SOAPException
	 */
	public DcfAck getAck(Environment env, IDcfUser user, String messageId) throws DetailedSOAPException;

	/**
	 * Get the ack of a message
	 * 
	 * @return
	 * @throws SOAPException
	 */
	public DcfAckDetailedResId getAckDetailedResId(Environment env, IDcfUser user, String detailedResId)
			throws DetailedSOAPException;
}
