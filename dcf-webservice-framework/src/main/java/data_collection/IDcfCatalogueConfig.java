package data_collection;

public interface IDcfCatalogueConfig {

	public void setId(int id);
	public void setDataElementName(String dataElem);
	public void setCatalogueCode(String catalogueCode);
	public void setHierarchyCode(String hierarchyCode);
	
	public int getId();
	
	/**
	 * Get the name of the data collection variable
	 * @return
	 */
	public String getDataElementName();
	
	/**
	 * Get the code of the catalogue which contains
	 * the data collection variable
	 * @return
	 */
	public String getCatalogueCode();
	
	/**
	 * Get the code of the hierarchy which contains
	 * the data collection variable
	 * @return
	 */
	public String getHierarchyCode();
}
