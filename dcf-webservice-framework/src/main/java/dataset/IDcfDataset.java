package dataset;

public interface IDcfDataset {
	
	/**
	 * Set the dataset id
	 * @param id
	 */
	public void setId(String id);
	
	/**
	 * Set the dataset sender id
	 * @param senderId
	 */
	public void setSenderId(String senderId);
	
	/**
	 * Set the dataset status
	 * @param status
	 */
	public void setStatus(DcfDatasetStatus status);
	
	/**
	 * Set the last message id involved for this dataset
	 * @param msgId
	 */
	public void setLastMessageId(String msgId);
	
	/**
	 * Set the last message id which modified the content
	 * of the dataset
	 * @param msgId
	 */
	public void setLastModifyingMessageId(String msgId);
	
	/**
	 * Set the last message id which validated the content
	 * of the dataset
	 * @param msgId
	 */
	public void setLastValidationMessageId(String msgId);
	
	/**
	 * Get the dataset id
	 * @return
	 */
	public String getId();
	
	/**
	 * Get the dataset sender id (id given
	 * by the data provider)
	 * @return
	 */
	public String getSenderId();
	
	/**
	 * Get the dataset status
	 * @return
	 */
	public DcfDatasetStatus getStatus();
	
	/**
	 * Get the last message id involved for this dataset
	 */
	public String getLastMessageId();
	
	/**
	 * Get the last message id which modified the content
	 * of the dataset
	 */
	public String getLastModifyingMessageId();
	
	/**
	 * Get the last message id which validated the content
	 * of the dataset
	 */
	public String getLastValidationMessageId();
}
