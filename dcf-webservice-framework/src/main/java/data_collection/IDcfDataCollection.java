package data_collection;

import java.sql.Timestamp;

public interface IDcfDataCollection {
	
	/**
	 * Check if the data collection is currently
	 * opened
	 * @return
	 */
	public boolean isActive();
	
	/**
	 * Set the data collection id
	 * @param id
	 */
	public void setId(int id);
	
	/**
	 * Set the data collection code
	 * @param code
	 */
	public void setCode(String code);
	
	/**
	 * Set the description
	 * @param description
	 */
	public void setDescription(String description);
	
	/**
	 * Set the category
	 * @param category
	 */
	public void setCategory(String category);
	
	/**
	 * Set the timestamp from which the dc is
	 * considered opened
	 * @param activeFrom
	 */
	public void setActiveFrom(Timestamp activeFrom);
	
	/**
	 * Set the timestamp from which the dc is 
	 * considered closed
	 * @param activeTo
	 */
	public void setActiveTo(Timestamp activeTo);
	
	/**
	 * Set a resource linked to the dc
	 * @param resourceId
	 */
	public void setResourceId(String resourceId);
	
	/**
	 * Get the id of the data collection
	 * @return
	 */
	public int getId();
	
	/**
	 * Get the data collection code
	 * @return
	 */
	public String getCode();
	
	/**
	 * Get a description of the dc
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Get the category
	 * @return
	 */
	public String getCategory();
	
	/**
	 * Get the timestamp from which the data
	 * collection is considered active
	 * @return
	 */
	public Timestamp getActiveFrom();
	
	/**
	 * Get the timestamp from which the
	 * data collection is considered closed
	 * @return
	 */
	public Timestamp getActiveTo();
	
	/**
	 * Get the linked resource
	 * @return
	 */
	public String getResourceId();
}
