package resource;

public interface IDcfResourceReference {
	
	/**
	 * Set the resource type
	 * @param type
	 */
	public void setType(String type);
	
	/**
	 * Set the resource id
	 * @param resourceId
	 */
	public void setResourceId(String resourceId);
	
	/**
	 * Get the resource type
	 * @return
	 */
	public String getType();
	
	/**
	 * Get the resource id
	 * @return
	 */
	public String getResourceId();
	
	/**
	 * Check if the resource has at least
	 * one field missing
	 * @return
	 */
	public boolean isIncomplete();
}
