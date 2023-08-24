package message;

public class MessageResponse {
	
	private String messageId;
	private TrxCode trxState;
	private String trxError;
	
	public MessageResponse(String messageId, TrxCode trxState, String trxError) {
		this.messageId = messageId;
		this.trxState = trxState;
		this.trxError = trxError;
	}
	
	public String getMessageId() {
		return this.messageId;
	}
	public TrxCode getTrxState() {
		return this.trxState;
	}
	public String getTrxError() {
		return this.trxError;
	}
	
	public SendMessageErrorType getErrorType() {
		return SendMessageErrorType.fromString(this.trxError);
	}
	
	/**
	 * Check if the response was valid or not
	 * @return
	 */
	public boolean isCorrect() {
		return this.trxState == TrxCode.TRXOK;
	}
	
	@Override
	public String toString() {
		return "MessageId=" + this.messageId + "; trxCode=" + this.trxState;
	}
}
