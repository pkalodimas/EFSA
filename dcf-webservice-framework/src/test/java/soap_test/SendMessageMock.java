package soap_test;

import java.io.File;
import java.io.IOException;

import config.Environment;
import message.MessageResponse;
import message.TrxCode;
import soap.DetailedSOAPException;
import soap_interface.ISendMessage;
import user.IDcfUser;

public class SendMessageMock implements ISendMessage {
	
	private MessageResponse response;
	
	public void setResponse(MessageResponse response) {
		this.response = response;
	}
	
	@Override
	public MessageResponse send(Environment env, IDcfUser user, File file) throws DetailedSOAPException, IOException {
		
		if (this.response != null)
			return this.response;
		
		String messageId = String.valueOf((int) (10000 + Math.random() * 90000));
		return new MessageResponse(messageId, TrxCode.TRXOK, null);
	}
}
