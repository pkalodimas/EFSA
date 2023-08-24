package pending_request;

public class PendingRequestStatusChangedEvent {

	private IPendingRequest pendingRequest;
	private PendingRequestStatus oldStatus;
	private PendingRequestStatus newStatus;
	
	/**
	 * Event called by the {@link PendingRequestListener}
	 * @param pendingRequest
	 * @param oldStatus
	 * @param newStatus
	 */
	public PendingRequestStatusChangedEvent(IPendingRequest pendingRequest, PendingRequestStatus oldStatus,
			PendingRequestStatus newStatus) {
		this.pendingRequest = pendingRequest;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
	}
	
	/**
	 * Get the pending request involved
	 * @return
	 */
	public IPendingRequest getPendingRequest() {
		return this.pendingRequest;
	}
	
	/**
	 * Get the old status of the pending request
	 * @return
	 */
	public PendingRequestStatus getOldStatus() {
		return this.oldStatus;
	}
	
	/**
	 * Get the current status of the pending request
	 * @return
	 */
	public PendingRequestStatus getNewStatus() {
		return this.newStatus;
	}
	
	@Override
	public String toString() {
		return "status=" + this.oldStatus + " => " + this.newStatus + "; pendingRequest=" + this.pendingRequest;
	}
}
