package data_collection;

/**
 * A configuration which contains one of the data collection
 * variables. In particular it specifies the catalogue and 
 * the hierarchy which contains this variable.
 * @author avonva
 * @author shahaal
 *
 */
public class DcfCatalogueConfig implements IDcfCatalogueConfig {

	private int id = -1;
	private String dataElementName;
	private String catalogueCode;
	private String hierarchyCode;
	
	public DcfCatalogueConfig() {}
	
	public DcfCatalogueConfig( String dataElementName, 
			String catalogueCode, String hierarchyCode ) {
		this.dataElementName = dataElementName;
		this.catalogueCode = catalogueCode;
		this.hierarchyCode = hierarchyCode;
	}
	
	@Override
	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public void setDataElementName(String dataElem) {
		this.dataElementName = dataElem;
	}
	
	@Override
	public void setCatalogueCode(String catalogueCode) {
		this.catalogueCode = catalogueCode;
	}
	
	@Override
	public void setHierarchyCode(String hierarchyCode) {
		this.hierarchyCode = hierarchyCode;
	}
	
	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public String getDataElementName() {
		return this.dataElementName;
	}

	@Override
	public String getCatalogueCode() {
		return this.catalogueCode;
	}

	@Override
	public String getHierarchyCode() {
		return this.hierarchyCode;
	}
	
	@Override
	public String toString() {
		return "CAT CONFIG: id= " + (this.id == -1 ? "not defined yet" : this.id ) 
				+ "dataElemName=" + this.dataElementName
				+ ";catCode=" + this.catalogueCode
				+ ";hierCode=" + this.hierarchyCode;
	}
}
