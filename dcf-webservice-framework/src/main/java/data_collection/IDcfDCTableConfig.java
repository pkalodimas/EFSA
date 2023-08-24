package data_collection;

public interface IDcfDCTableConfig {

	/**
	 * Get the data collection related to this configuration
	 * @return
	 */
	public IDcfDataCollection getDataCollection();
	
	/**
	 * Get the fact table related to this configuration
	 * @return
	 */
	public IDcfDCTable getTable();
	
	/**
	 * Get the configuration object
	 * @return
	 */
	public IDcfCatalogueConfig getConfig();
}
