package catalogue;

import java.sql.Timestamp;

public interface IDcfCatalogue {

	public void setCode(String code);
	public void setName(String name);
	public void setLabel(String label);
	public void setScopenotes(String scopenotes);
	public void setVersion(String version);
	public void setLastUpdate(Timestamp lastUpdate);
	public void setValidFrom(Timestamp validFrom);
	public void setValidTo(Timestamp validTo);
	public void setStatus(String status);
	public void setDeprecated(boolean deprecated);
	public void setTermCodeMask(String termCodeMask);
	public void setTermCodeLength(int termCodeLength);
	public void setTermMinCode(String termMinCode);
	public void setAcceptNonStandardCodes(boolean acceptNonStandardCodes);
	public void setGenerateMissingCodes(boolean generateMissingCodes);
	public void setCatalogueGroups(String catalogueGroups);
	
	public String getCode();
	public String getName();
	public String getLabel();
	public String getScopenotes();
	public String getVersion();
	public Timestamp getLastUpdate();
	public Timestamp getValidFrom();
	public Timestamp getValidTo();
	public String getStatus();
	public boolean isDeprecated();
	public String getTermMinCode();
	public int getTermCodeLength();
	public String getTermCodeMask();
	public boolean isAcceptNonStandardCodes();
	public boolean isGenerateMissingCodes();
	public String getCatalogueGroups();
}
