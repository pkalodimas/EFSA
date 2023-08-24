package catalogue;

import java.sql.Timestamp;

import soap.GetCataloguesList;

/**
 * Catalogue object retrieved with {@link GetCataloguesList}
 * @author avonva
 *
 */
public class DcfCatalogue implements IDcfCatalogue {
	
	private String code;
	private String name;
	private String label;
	private String scopenotes;
	private String version;
	private Timestamp lastUpdate;
	private Timestamp validFrom;
	private Timestamp validTo;
	private String status;
	private boolean deprecated;
	private String termCodeMask;
	private int termCodeLength;
	private String termMinCode;
	private boolean acceptNonStandardCodes;
	private boolean generateMissingCodes;
	private String catalogueGroups;

	@Override
	public void setCode(String code) {
		this.code = code;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public void setLabel(String label) {
		this.label = label;
	}
	@Override
	public void setScopenotes(String scopenotes) {
		this.scopenotes = scopenotes;
	}
	@Override
	public void setVersion(String version) {
		this.version = version;
	}
	@Override
	public void setLastUpdate(Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	@Override
	public void setValidFrom(Timestamp validFrom) {
		this.validFrom = validFrom;
	}
	@Override
	public void setValidTo(Timestamp validTo) {
		this.validTo = validTo;
	}
	@Override
	public void setStatus(String status) {
		this.status = status;
	}
	@Override
	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}
	@Override
	public void setTermCodeMask(String termCodeMask) {
		this.termCodeMask = termCodeMask;
	}
	@Override
	public void setTermCodeLength(int termCodeLength) {
		this.termCodeLength = termCodeLength;
	}
	@Override
	public void setTermMinCode(String termMinCode) {
		this.termMinCode = termMinCode;
	}
	@Override
	public void setAcceptNonStandardCodes(boolean acceptNonStandardCodes) {
		this.acceptNonStandardCodes = acceptNonStandardCodes;
	}
	@Override
	public void setGenerateMissingCodes(boolean generateMissingCodes) {
		this.generateMissingCodes = generateMissingCodes;
	}
	@Override
	public void setCatalogueGroups(String catalogueGroups) {
		this.catalogueGroups = catalogueGroups;
	}

	@Override
	public String getCode() {
		return this.code;
	}
	@Override
	public String getName() {
		return this.name;
	}
	@Override
	public String getLabel() {
		return this.label;
	}
	@Override
	public String getScopenotes() {
		return this.scopenotes;
	}
	@Override
	public String getVersion() {
		return this.version;
	}
	@Override
	public Timestamp getLastUpdate() {
		return this.lastUpdate;
	}
	@Override
	public Timestamp getValidFrom() {
		return this.validFrom;
	}
	@Override
	public Timestamp getValidTo() {
		return this.validTo;
	}
	@Override
	public String getStatus() {
		return this.status;
	}
	@Override
	public boolean isDeprecated() {
		return this.deprecated;
	}
	@Override
	public String getTermMinCode() {
		return this.termMinCode;
	}
	@Override
	public int getTermCodeLength() {
		return this.termCodeLength;
	}
	@Override
	public String getTermCodeMask() {
		return this.termCodeMask;
	}
	@Override
	public boolean isAcceptNonStandardCodes() {
		return this.acceptNonStandardCodes;
	}
	@Override
	public boolean isGenerateMissingCodes() {
		return this.generateMissingCodes;
	}
	@Override
	public String getCatalogueGroups() {
		return this.catalogueGroups;
	}
	
	@Override
	public String toString() {
		return "code=" + this.code 
				+ ";version=" + this.version
				+ ";label=" + this.label 
				+ ";scopenotes=" + this.scopenotes 
				+ ";termCodeLength=" + this.termCodeLength
				+ ";validFrom=" + this.validFrom 
				+ ";validTo=" + this.validTo;
	}
}


