package soap;

import pending_request.IPendingRequest;

/**
 * At which level we make a reserve operation? 
 * Minor has some editing limitations.
 * Values:
 * NONE
 * MINOR
 * MAJOR
 * @author avonva
 * @author shahaal
 *
 */
public enum UploadCatalogueFileAction {
	
	PUBLISH_MINOR("Publish minor"),
	PUBLISH_MAJOR("Publish major"),
	UNRESERVE("Unreserve"),
	RESERVE_MINOR("Reserve minor"),
	RESERVE_MAJOR("Reserve major");
	
	private String name;

	/**
	 * Initialise a reserve level
	 * @param level
	 * @param description
	 */
	UploadCatalogueFileAction(String name) {
        this.name = name;
    }

	public String getName() {
		return this.name;
	}
	
	public String getDatabaseCode() {
		
		String code = null;
		
		switch(this) {
		case PUBLISH_MINOR:
			code = IPendingRequest.TYPE_PUBLISH_MINOR;
			break;
		case PUBLISH_MAJOR:
			code = IPendingRequest.TYPE_PUBLISH_MAJOR;
			break;
		case UNRESERVE:
			code = IPendingRequest.TYPE_UNRESERVE;
			break;
		case RESERVE_MINOR:
			code = IPendingRequest.TYPE_RESERVE_MINOR;
			break;
		case RESERVE_MAJOR:
			code = IPendingRequest.TYPE_RESERVE_MAJOR;
			break;
		default:
			code = IPendingRequest.TYPE_OTHER;
			break;
		}
		
		return code;
	}
}
