package pending_request;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.soap.SOAPException;

import config.Environment;
import dcf_log.DcfLog;
import dcf_log.DcfResponse;
import dcf_log.IDcfLogParser;
import soap.UploadCatalogueFile;
import user.IDcfUser;

/**
 * A pending request is an object which polls the DCF
 * in order to retrieve the result of a web service
 * request. In particular, this is used with {@link UploadCatalogueFile}
 * requests, whose response is a code of a log which will be created
 * only when the request is fulfilled. Since it is not known when the
 * log will be created, a polling action is required to retrieve the log.
 * @author avonva
 *
 */
public interface IPendingRequest {

	public static final String TYPE_RESERVE_MAJOR = "RESERVE_MAJOR";
	public static final String TYPE_RESERVE_MINOR = "RESERVE_MINOR";
	public static final String TYPE_UNRESERVE = "UNRESERVE";
	public static final String TYPE_PUBLISH_MAJOR = "PUBLISH_MAJOR";
	public static final String TYPE_PUBLISH_MINOR = "PUBLISH_MINOR";
	public static final String TYPE_OTHER = "OTHER";
	
	/**
	 * Start polling the DCF
	 * @param parser which parser should be used to create a {@link DcfLog} object
	 * starting from the log {@link File}
	 * @return the response contained in the log
	 * @throws SOAPException used for download issues
	 */
	public DcfResponse start(IDcfLogParser parser) throws SOAPException, IOException;
	
	/**
	 * Force the request to restart if queued
	 */
	public void restart();
	
	/**
	 * Get when the pending request will be relaunched
	 * (only if its status is {@link PendingRequestStatus#QUEUED}).
	 * @return the time when the request will restart or -1 if the request
	 * is already on going (or with HIGH priority)
	 */
	public long getRestartTime();
	
	/**
	 * Get the DCF response contained in the log.
	 * @return the dcf response if the log was already retrieved,
	 * null otherwise.
	 */
	public DcfResponse getResponse();
	
	/**
	 * Get the environment used when the pending
	 * request was created
	 * @return
	 */
	public Environment getEnvironmentUsed();
	
	/**
	 * Get the current status of the request
	 * @return
	 */
	public PendingRequestStatus getStatus();
	
	/**
	 * Get the user of who created and sent the request
	 * @return
	 */
	public IDcfUser getRequestor();
	
	/**
	 * Get additional data used for the pending request
	 * @return
	 */
	public Map<String, String> getData();
	
	/**
	 * Get the code of the log involved in
	 * the pending request
	 * @return
	 */
	public String getLogCode();
	
	/**
	 * Get the retrieved log
	 * @return
	 */
	public DcfLog getLog();
	
	/**
	 * Get the type of the request
	 * @return
	 */
	public String getType();
	
	/**
	 * Check if the request is in pause or not
	 * @return
	 */
	public boolean isPaused();
	
	/**
	 * Get the priority of the pending request
	 * @return
	 */
	public PendingRequestPriority getPriority();
	
	/**
	 * Set the environment in which the pending request
	 * is created
	 * @param env
	 */
	public void setEnvironmentUsed(Environment env);
	
	/**
	 * Set who requested the web request
	 * @param user
	 */
	public void setRequestor(IDcfUser user);
	
	/**
	 * Set the code of the log which will
	 * be retrieved by the pending request
	 * @param logCode
	 */
	public void setLogCode(String logCode);
	
	/**
	 * Set the data of the pending request.
	 * Follow the syntax: key1=value1$key2=value2$...
	 * @param data
	 */
	public void setData(Map<String, String> data);
	
	/**
	 * Set the type of the pending request.
	 * Use the following static variables for
	 * predefined behaviors:
	 * <ul>
	 * <li>{@link #TYPE_RESERVE}</li>
	 * <li>{@link #TYPE_UNRESERVE}</li>
	 * <li>{@link #TYPE_PUBLISH}</li>
	 * <li>{@link #TYPE_UPLOAD_XML_CHANGES}</li>
	 * </ul>
	 * @param type
	 */
	public void setType(String type);
	
	/**
	 * Set the priority of the pending request
	 * @param priority
	 */
	public void setPriority(PendingRequestPriority priority);
	
	/**
	 * Add a listener to the events which happen in the pending
	 * request
	 * @param listener
	 */
	public void addPendingRequestListener(PendingRequestListener listener);
}
