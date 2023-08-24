package resource;

import soap.GetResourcesList;

/**
 * Single object contained in the {@link GetResourcesList}
 * response
 * @author avonva
 * @author shahaal
 *
 */
public class DcfResourceReference implements IDcfResourceReference {

	private String type;
	private String resourceId;
	
	@Override
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	@Override
	public String getType() {
		return this.type;
	}
	@Override
	public String getResourceId() {
		return this.resourceId;
	}
	
	/**
	 * Check if a field is missing
	 * @return
	 */
	@Override
	public boolean isIncomplete() {
		return (this.type == null || this.resourceId == null || this.type.isEmpty() || this.resourceId.isEmpty());
	}
	
	@Override
	public String toString() {
		return "ResourceReference: type=" + this.type + ";resourceId=" + this.resourceId;
	}
}
