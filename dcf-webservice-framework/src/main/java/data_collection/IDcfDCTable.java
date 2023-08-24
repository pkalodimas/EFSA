package data_collection;

import java.util.Collection;

public interface IDcfDCTable {

	public void setId(int id);
	
	public void setName(String name);
	
	/**
	 * Set the data collection configurations
	 * @param config
	 */
	public void setConfigs(Collection<IDcfCatalogueConfig> configs);
	
	/**
	 * Add a catalogue configuration
	 * @param config
	 */
	public void addConfig(IDcfCatalogueConfig config);
	
	public int getId();
	public String getName();
	public Collection<IDcfCatalogueConfig> getConfigs();

}
