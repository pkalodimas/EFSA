package soap;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import config.Environment;
import pending_request.IDcfPendingRequestsList;
import pending_request.IPendingRequest;
import pending_request.IPendingRequestDao;
import pending_request.PendingRequestListener;
import pending_request.PendingRequestStatusChangedEvent;
import soap.UploadCatalogueFileImpl.PublishLevel;
import soap.UploadCatalogueFileImpl.ReserveLevel;
import soap_interface.IUploadCatalogueFileImpl;
import user.IDcfUser;

/**
 * This class provides a wrapper for the {@link UploadCatalogueFileImpl}.
 * In particular, it saves the retrieved {@link IPendingRequest} objects
 * from the soap calls to {@link UploadCatalogueFile} into the database, 
 * in order to be able to reuse them in different runs of the application.
 * Note that the class itself manages also the update and the removal
 * of the pending requests from the database (at the condition that
 * the class methods are used to retrieve the pending requests from
 * the database).
 * @author avonva
 * @author shahaal
 *
 */
public class UploadCatalogueFilePersistentImpl implements IUploadCatalogueFileImpl {
	
	private static final Logger LOGGER = LogManager.getLogger(UploadCatalogueFilePersistentImpl.class);
	
	private IPendingRequestDao<IPendingRequest> dao;
	
	/**
	 * Initialise the class
	 * @param dao which dao should be used to save the data
	 */
	public UploadCatalogueFilePersistentImpl(IPendingRequestDao<IPendingRequest> dao) {
		this.dao = dao;
	}

	/**
	 * Reserve a catalogue
	 * @param level
	 * @param catalogueCode
	 * @param description
	 * @return
	 * @throws DetailedSOAPException
	 * @throws IOException 
	 * @throws SQLException 
	 */
	@Override
	public IPendingRequest reserve(IDcfUser user, Environment env, ReserveLevel level, String catalogueCode, 
			String description) throws DetailedSOAPException, IOException {

		UploadCatalogueFileImpl impl = new UploadCatalogueFileImpl();
		IPendingRequest request = impl.reserve(user, env, level, catalogueCode, description);
		return makePersistent(request);
	}
	
	/**
	 * Unreserve a catalogue
	 * @param catalogueCode
	 * @param description
	 * @return
	 * @throws DetailedSOAPException
	 * @throws IOException 
	 * @throws SQLException 
	 */
	@Override
	public IPendingRequest unreserve(IDcfUser user, Environment env, String catalogueCode, String description) 
			throws DetailedSOAPException, IOException {
		
		UploadCatalogueFileImpl impl = new UploadCatalogueFileImpl();
		IPendingRequest request = impl.unreserve(user, env, catalogueCode, description);
		return makePersistent(request);
	}
	
	/**
	 * Publish a catalogue
	 * @param level
	 * @param catalogueCode
	 * @return
	 * @throws DetailedSOAPException
	 * @throws IOException 
	 * @throws SQLException 
	 */
	@Override
	public IPendingRequest publish(IDcfUser user, Environment env, PublishLevel level, String catalogueCode) 
			throws DetailedSOAPException, IOException {
		
		UploadCatalogueFileImpl impl = new UploadCatalogueFileImpl();
		IPendingRequest request = impl.publish(user, env, level, catalogueCode);
		return makePersistent(request);
	}
	
	/**
	 * Upload a generic catalogue file
	 * @param user
	 * @param env
	 * @param attachment
	 * @param uploadCatalogueFileType type of the request, this is a code
	 * used to know which operation was completed when the request was fulfilled
	 * @return
	 * @throws DetailedSOAPException
	 * @throws IOException
	 */
	public IPendingRequest uploadCatalogueFile(IDcfUser user, Environment env, 
			String attachment, String uploadCatalogueFileType) throws DetailedSOAPException, IOException {
		
		UploadCatalogueFileImpl impl = new UploadCatalogueFileImpl();
		
		IPendingRequest request = impl.uploadCatalogueFile(user, env, attachment, 
				uploadCatalogueFileType, new HashMap<>());
		
		return makePersistent(request);
	}
	
	/**
	 * Upload a generic catalogue file
	 * @param user
	 * @param env
	 * @param attachment
	 * @param uploadCatalogueFileType type of the request, this is a code
	 * used to know which operation was completed when the request was fulfilled
	 * @param requestData additional data which will be stored in the database
	 * for the current request (as catalogue code etc..)
	 * @return
	 * @throws DetailedSOAPException
	 * @throws IOException
	 */
	@Override
	public IPendingRequest uploadCatalogueFile(IDcfUser user, Environment env, 
			String attachment, String uploadCatalogueFileType, Map<String, String> requestData) 
					throws DetailedSOAPException, IOException {
		
		UploadCatalogueFileImpl impl = new UploadCatalogueFileImpl();
		
		IPendingRequest request = impl.uploadCatalogueFile(user, env, attachment, 
				uploadCatalogueFileType, requestData);
		
		return makePersistent(request);
	}
	
	/**
	 * Get all the requests of the user. This will also handle
	 * the deletion of the requests which will be completed.
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public IDcfPendingRequestsList<IPendingRequest> getUserPendingRequests(IDcfUser user, 
			IDcfPendingRequestsList<IPendingRequest> output) 
			throws SQLException, IOException {
		
		IDcfPendingRequestsList<IPendingRequest> requests = this.dao.getUserPendingRequests(user, output);
		
		// add listeners in order to remove/update the requests
		for (IPendingRequest request : requests) {
			request.addPendingRequestListener(getPersistentListener());
		}
		
		return requests;
	}
	
	/**
	 * Make the request persistent in the db. In particular the request
	 * will be saved into the database and when it is completed
	 * it will be deleted.
	 * @param request
	 * @return
	 * @throws IOException
	 */
	private IPendingRequest makePersistent(IPendingRequest request) throws IOException {
		
		// save the pending request into the database
		try {
			this.dao.insert(request);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e);  // follow the interface declaration
		}
		
		// add the listener
		request.addPendingRequestListener(getPersistentListener());
		
		return request;
	}
	
	/**
	 * Get listener which can be used to remove
	 * the pending requests when they are completed
	 * @return
	 */
	private PendingRequestListener getPersistentListener() {
		
		PendingRequestListener listener = new PendingRequestListener() {
			
			@Override
			public void statusChanged(PendingRequestStatusChangedEvent event) {
				
				String logCode = event.getPendingRequest().getLogCode();

				switch(event.getNewStatus()) {
				case COMPLETED:  // request finished, remove from db
					try {
						UploadCatalogueFilePersistentImpl.this.dao.remove(logCode);
					}
					catch(IOException | SQLException e) {
						e.printStackTrace();
						LOGGER.error("Cannot remove from db, pending request=" + event.getPendingRequest(), e);
					}
					break;
				default:
					break;
				}
			}
		};
		
		return listener;
	}
}
