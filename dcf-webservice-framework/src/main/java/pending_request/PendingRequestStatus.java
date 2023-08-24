package pending_request;

/**
 * Status of a {@link PendingRequest}
 * @author avonva
 *
 */
public enum PendingRequestStatus {
	WAITING,     // initial status
	DOWNLOADING, // if we are retrieving the response log
	QUEUED,      // if request was queued in the dcf (busy dcf)
	COMPLETED,   // if the request was completed successfully
	ERROR        // error
}
