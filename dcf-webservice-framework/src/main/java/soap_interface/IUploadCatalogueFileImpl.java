package soap_interface;

import java.io.IOException;
import java.util.Map;

import config.Environment;
import pending_request.IPendingRequest;
import soap.DetailedSOAPException;
import soap.UploadCatalogueFileAction;
import soap.UploadCatalogueFileImpl.PublishLevel;
import soap.UploadCatalogueFileImpl.ReserveLevel;
import user.IDcfUser;

public interface IUploadCatalogueFileImpl {
	
	public IPendingRequest reserve(IDcfUser user, Environment env, ReserveLevel level, String catalogueCode, 
			String description) throws DetailedSOAPException, IOException;
	
	public IPendingRequest unreserve(IDcfUser user, Environment env, String catalogueCode, String description) 
			throws DetailedSOAPException, IOException;
	
	public IPendingRequest publish(IDcfUser user, Environment env, PublishLevel level, String catalogueCode) 
			throws DetailedSOAPException, IOException;
	
	public IPendingRequest uploadCatalogueFile(IDcfUser user, Environment env, 
			String attachment, String uploadCatalogueFileType, Map<String, String> requestData)
			throws DetailedSOAPException, IOException;
	
	/**
	 * Convert publish level to upload catalogue file action
	 * @param level
	 * @return
	 */
	public static UploadCatalogueFileAction fromPublishLevel(PublishLevel level) {
		
		UploadCatalogueFileAction action = null;
		
		switch(level) {
		case MAJOR:
			action = UploadCatalogueFileAction.PUBLISH_MAJOR;
			break;
		case MINOR:
			action = UploadCatalogueFileAction.PUBLISH_MINOR;
			break;
		default:
			break;
		}
		
		if (action == null)
			throw new IllegalArgumentException("Unrecognized publish level=" + level);
		
		return action;
	}
	
	/**
	 * Convert reserve level to upload catalogue file action
	 * @param level
	 * @return
	 */
	public static UploadCatalogueFileAction fromReserveLevel(ReserveLevel level) {
		
		UploadCatalogueFileAction action = null;
		
		switch(level) {
		case MAJOR:
			action = UploadCatalogueFileAction.RESERVE_MAJOR;
			break;
		case MINOR:
			action = UploadCatalogueFileAction.RESERVE_MINOR;
			break;
		default:
			break;
		}
		
		if (action == null)
			throw new IllegalArgumentException("Unrecognized reserve level=" + level);
		
		return action;
	}
}
