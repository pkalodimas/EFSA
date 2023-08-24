package ack;

import java.io.InputStream;
import java.util.Collection;

import dataset.DcfDatasetStatus;
import soap.GetAck;

/**
 * Log obtained from the {@link GetAck} request
 * @author avonva
 *
 */
public interface IDcfAckLog {
	
	/**
	 * get the log
	 * @return
	 */
	public InputStream getRawLog();
	
	/**
	 * Get the data collection used for this message
	 * @return
	 */
	public String getDCCode();
	
	/**
	 * Get the message val res code
	 * @return
	 */
	public MessageValResCode getMessageValResCode();

	/**
	 * Get the detailed ack res id
	 * @return
	 */
	public String getDetailedAckResId();
	
	/**
	 * Get the error message for discarded messages
	 * @return
	 */
	public String getMessageValResText();
	
	/**
	 * Get the operation res code
	 * @return
	 */
	public OkCode getOpResCode();
	
	/**
	 * Check if the op res code is ok
	 * @return
	 */
	public boolean isOk();
	
	/**
	 * Get the error message that is attached to the
	 * operation node. Only present if the {@link #getOpResCode()}
	 * is {@link OkCode#KO}.
	 * @return
	 */
	public Collection<String> getOpResLog();
	
	/**
	 * Check if the log contains errors
	 * @return
	 */
	public boolean hasErrors();
	
	/**
	 * Get the type of error if present
	 * @return
	 */
	public OpResError getOpResError();
	
	/**
	 * Get the retrieved dataset id
	 * @return
	 */
	public String getDatasetId();
	
	/**
	 * Get the status of the dataset
	 * @return
	 */
	public DcfDatasetStatus getDatasetStatus();
}
