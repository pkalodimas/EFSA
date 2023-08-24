package pending_request;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import config.Environment;
import soap.DetailedSOAPException;
import soap.UploadCatalogueFileAction;
import soap.UploadCatalogueFileImpl.PublishLevel;
import soap.UploadCatalogueFileImpl.ReserveLevel;
import soap_interface.IUploadCatalogueFileImpl;
import user.IDcfUser;

public class UploadCatalogueFilePersistentImplMock implements IUploadCatalogueFileImpl {

	private UploadCatalogueFileImplMock upl;
	private IPendingRequestDao<IPendingRequest> dao;
	
	public UploadCatalogueFilePersistentImplMock(IPendingRequestDao<IPendingRequest> dao) {
		this.upl = new UploadCatalogueFileImplMock();
		this.dao = dao;
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
			request.addPendingRequestListener(getListener());
		}
		
		return requests;
	}
	
	private IPendingRequest makePersistent(IPendingRequest request) throws IOException {
		
		try {
			this.dao.insert(request);
		}
		catch (SQLException e) {
			throw new IOException(e);
		}
		
		request.addPendingRequestListener(getListener());
		
		return request;
	}
	
	private PendingRequestListener getListener() {
		return new PendingRequestListener() {
			
			@Override
			public void statusChanged(PendingRequestStatusChangedEvent event) {
				if (event.getNewStatus() == PendingRequestStatus.COMPLETED) {
					try {
						UploadCatalogueFilePersistentImplMock.this.dao.remove(event.getPendingRequest().getLogCode());
					} catch (SQLException | IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}
	
	@Override
	public IPendingRequest reserve(IDcfUser user, Environment env, ReserveLevel level, String catalogueCode, String description)
			throws DetailedSOAPException, IOException {
		IPendingRequest pr = sendAction(user, env, IUploadCatalogueFileImpl.fromReserveLevel(level), catalogueCode, description);
		return makePersistent(pr);
	}

	@Override
	public IPendingRequest unreserve(IDcfUser user, Environment env, String catalogueCode, String description) throws DetailedSOAPException, IOException {
		IPendingRequest pr = sendAction(user, env, UploadCatalogueFileAction.UNRESERVE, catalogueCode, description);
		return makePersistent(pr);
	}

	@Override
	public IPendingRequest publish(IDcfUser user, Environment env, PublishLevel level, String catalogueCode) throws DetailedSOAPException, IOException {
		IPendingRequest pr = sendAction(user, env, IUploadCatalogueFileImpl.fromPublishLevel(level), catalogueCode, null);
		return makePersistent(pr);
	}

	private IPendingRequest sendAction(IDcfUser user, Environment env, UploadCatalogueFileAction action, 
			String catalogueCode, String description) throws DetailedSOAPException, IOException {
		IPendingRequest pr = this.upl.sendAction(user, env, action, catalogueCode, description);
		return makePersistent(pr);
	}

	@Override
	public IPendingRequest uploadCatalogueFile(IDcfUser user, Environment env, String attachment,
			String uploadCatalogueFileType, Map<String, String> requestData) throws DetailedSOAPException, IOException {
		IPendingRequest pr = this.upl.uploadCatalogueFile(user, env, attachment, uploadCatalogueFileType, requestData);
		return makePersistent(pr);
	}}
