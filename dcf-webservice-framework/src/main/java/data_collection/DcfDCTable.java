package data_collection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A table related to a data collection
 * @author avonva
 * @author shahaal
 *
 */
public class DcfDCTable implements IDcfDCTable {

	private int id = -1;
	private String name;
	private Collection<IDcfCatalogueConfig> configs;
	
	public DcfDCTable() {
		this.configs = new ArrayList<>();
	}
	
	/**
	 * Create a data collection table
	 * @param name name of the table
	 */
	public DcfDCTable(String name) {
		this();
		this.name = name;	
	}
	
	@Override
	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public int getId() {
		return this.id;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public Collection<IDcfCatalogueConfig> getConfigs() {
		return this.configs;
	}

	@Override
	public void setConfigs(Collection<IDcfCatalogueConfig> configs) {
		this.configs = configs;
	}
	
	@Override
	public void addConfig(IDcfCatalogueConfig config) {
		this.configs.add(config);
	}

	@Override
	public String toString() {
		return "DC TABLE: id=" + (this.id == -1 ? "not defined yet" : this.id )
				+ ";name=" + this.name
				+ ";configs=" + this.configs;
	}
}
