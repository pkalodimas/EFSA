package soap_test;

import ack.DcfAck;
import ack.DcfAckDetailedResId;
import config.Environment;
import soap.DetailedSOAPException;
import soap_interface.IGetAck;
import user.IDcfUser;

public class GetAckMock implements IGetAck {

	private DcfAck ack;
	private DcfAckDetailedResId detailedAck;
	
	public void setAck(DcfAck ack) {
		this.ack = ack;
	}
	
	public void setDetailedAck(DcfAckDetailedResId detailedAck) {
		this.detailedAck=detailedAck;
	}
	
	@Override
	public DcfAck getAck(Environment env, IDcfUser user, String messageId) throws DetailedSOAPException {
		return this.ack;
	}
	
	@Override
	public DcfAckDetailedResId getAckDetailedResId(Environment env, IDcfUser user, String detaildeResId) throws DetailedSOAPException {
		return this.detailedAck;
	}
}
