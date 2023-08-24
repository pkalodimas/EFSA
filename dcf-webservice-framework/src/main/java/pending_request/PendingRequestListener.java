package pending_request;

/**
 * Listen to the status changes for a pending request
 * @author avonva
 *
 */
public interface PendingRequestListener {
	
	/**
	 * Method called when the status of a {@link IPendingRequest}
	 * changes.
	 * @param event
	 */
	public void statusChanged(PendingRequestStatusChangedEvent event);
}
