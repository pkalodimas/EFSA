package dcf_log;

/**
 * Enumerator to track the dcf response to web service requests.
 * @author avonva
 *
 */
public enum DcfResponse {
	
	OK,     // all ok

	AP,     // the dcf received the request but it was rejected
	
	ERROR;  // operation failed due to connection problems or similar
}
