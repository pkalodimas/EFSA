package soap;

import java.util.HashMap;
import java.util.Map;

import config.Environment;
import pending_request.IPendingRequest;
import pending_request.PendingRequest;
import soap_interface.IUploadCatalogueFileImpl;
import user.IDcfUser;

/**
 * This class provides an implementation of the most commonly used
 * upload catalogue file actions.
 * @author avonva
 * @author shahaal
 *
 */
public class UploadCatalogueFileImpl implements IUploadCatalogueFileImpl {

	public static final String CATALOGUE_CODE_DATA_KEY = "catalogue";
	public static final String RESERVE_NOTE_DATA_KEY = "reserveNote";
	
	public enum ReserveLevel {
		MINOR,
		MAJOR;
		
		public static ReserveLevel fromRequestType(String requestType) {
			
			if (requestType.equals(IPendingRequest.TYPE_RESERVE_MAJOR))
				return ReserveLevel.MAJOR;
			
			if (requestType.equals(IPendingRequest.TYPE_RESERVE_MINOR))
				return ReserveLevel.MINOR;
			
			return null;
		}
		
		public static ReserveLevel fromDatabaseKey(String key) {
			
			for (ReserveLevel level : ReserveLevel.values()) {
				if (level.getDatabaseKey().equals(key))
					return level;
			}
			
			return null;
		}
		
		public String getDatabaseKey() {
			return this == MINOR ? IPendingRequest.TYPE_RESERVE_MINOR : IPendingRequest.TYPE_RESERVE_MAJOR;
		}
	}
	
	public enum PublishLevel {
		MINOR,
		MAJOR;
		
		public static PublishLevel fromRequestType(String requestType) {
			
			if (requestType.equals(IPendingRequest.TYPE_PUBLISH_MAJOR))
				return PublishLevel.MAJOR;
			
			if (requestType.equals(IPendingRequest.TYPE_PUBLISH_MINOR))
				return PublishLevel.MINOR;
			
			return null;
		}
		
		public static PublishLevel fromDatabaseKey(String key) {
			
			for (PublishLevel level : PublishLevel.values()) {
				if (level.getDatabaseKey().equals(key))
					return level;
			}
			
			return null;
		}
		
		public String getDatabaseKey() {
			return this == MINOR ? IPendingRequest.TYPE_PUBLISH_MINOR : IPendingRequest.TYPE_PUBLISH_MAJOR;
		}
	}

	/**
	 * Reserve a catalogue
	 * @param level
	 * @param catalogueCode
	 * @param description
	 * @return
	 * @throws DetailedSOAPException
	 */
	@Override
	public IPendingRequest reserve(IDcfUser user, Environment env, ReserveLevel level, 
			String catalogueCode, String description) throws DetailedSOAPException {
		
		UploadCatalogueFileAction action = IUploadCatalogueFileImpl.fromReserveLevel(level);
		
		String attachment = UploadMessages.get(action, catalogueCode, description);

		String type = action.getDatabaseCode();
		
		Map<String, String> data = new HashMap<>();
		data.put(CATALOGUE_CODE_DATA_KEY, catalogueCode);
		
		if (description != null) {
			data.put(RESERVE_NOTE_DATA_KEY, description);
		}
		
		return uploadCatalogueFile(user, env, attachment, type, data);
	}
	
	/**
	 * Unreserve a catalogue
	 * @param catalogueCode
	 * @param description
	 * @return
	 * @throws DetailedSOAPException
	 */
	@Override
	public IPendingRequest unreserve(IDcfUser user, Environment env, String catalogueCode, 
			String description) throws DetailedSOAPException {
		
		String attachment = UploadMessages.get(UploadCatalogueFileAction.UNRESERVE, catalogueCode, description);

		String type = UploadCatalogueFileAction.UNRESERVE.getDatabaseCode();
		
		Map<String, String> data = new HashMap<>();
		data.put(CATALOGUE_CODE_DATA_KEY, catalogueCode);
		
		if (description != null) {
			data.put(RESERVE_NOTE_DATA_KEY, description);
		}
		
		return uploadCatalogueFile(user, env, attachment, type, data);
	}
	
	/**
	 * Publish a catalogue
	 * @param level
	 * @param catalogueCode
	 * @return
	 * @throws DetailedSOAPException
	 */
	@Override
	public IPendingRequest publish(IDcfUser user, Environment env, PublishLevel level, 
			String catalogueCode) throws DetailedSOAPException {
		UploadCatalogueFileAction action = IUploadCatalogueFileImpl.fromPublishLevel(level);
		
		String attachment = UploadMessages.get(action, catalogueCode, null);

		String type = action.getDatabaseCode();
		
		Map<String, String> data = new HashMap<>();
		data.put(CATALOGUE_CODE_DATA_KEY, catalogueCode);
		
		return uploadCatalogueFile(user, env, attachment, type, data);
	}

	/**
	 * Upload a generic attachment
	 * @param user who requested the service
	 * @param env environment (production/test)
	 * @param attachment the attachment which will be uploaded
	 * @param uploadCatalogueFileType the type of request, this is used to
	 * differentiate the requests when the response is retrieved
	 * @param requestData additional data which can be stored into the
	 * pending request
	 * @return a {@link IPendingRequest} object which can be used to
	 * retrieve the result of the request
	 * @throws DetailedSOAPException
	 */
	@Override
	public IPendingRequest uploadCatalogueFile(IDcfUser user, Environment env, 
			String attachment, String uploadCatalogueFileType, Map<String, String> requestData) throws DetailedSOAPException {
		
		UploadCatalogueFile ucf = new UploadCatalogueFile();
		String logCode = ucf.send(env, user, attachment);
		PendingRequest pr = new PendingRequest(uploadCatalogueFileType, user, logCode, env);
		pr.setData(requestData);
		return pr;
	}
}
