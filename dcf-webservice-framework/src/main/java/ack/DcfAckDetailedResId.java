package ack;

/**
 * Acknowledge obtained with {@link GetAck}
 * @author shahaal
 *
 */
public class DcfAckDetailedResId {
	
	private FileState state;
	private IDcfAckLog log;
	
	/**
	 * Create an acknowledgement.
	 * @param state status of the ack
	 * @param log log of the ack, if present
	 */
	public DcfAckDetailedResId(FileState state, IDcfAckLog log) {
		this.state = state;
		this.log = log;
	}
	
	public FileState getState() {
		return this.state;
	}
	
	public IDcfAckLog getLog() {
		return this.log;
	}
	
	public boolean isReady() {
		return this.state != null && this.state == FileState.READY;
	}
	
	public boolean hasFault() {
		return this.state != null && this.state == FileState.EXCEPTION;
	}
	
	public boolean isDenied() {
		return this.state != null && this.state == FileState.ACCESS_DENIED;
	}
	
	@Override
	public String toString() {
		return "AckDetailedResID: state=" + this.state + "; log=" + this.log;
	}
}
