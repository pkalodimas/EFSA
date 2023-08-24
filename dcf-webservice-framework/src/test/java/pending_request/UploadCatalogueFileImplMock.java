package pending_request;

import java.io.IOException;
import java.util.Map;

import config.Environment;
import soap.DetailedSOAPException;
import soap.UploadCatalogueFileAction;
import soap.UploadCatalogueFileImpl;
import soap.UploadCatalogueFileImpl.PublishLevel;
import soap.UploadCatalogueFileImpl.ReserveLevel;
import soap_interface.IUploadCatalogueFileImpl;
import user.IDcfUser;

public class UploadCatalogueFileImplMock implements IUploadCatalogueFileImpl {

	private UploadCatalogueFileMock upc;
	
	public UploadCatalogueFileImplMock() {
		this.upc = new UploadCatalogueFileMock();
	}
	
	@Override
	public IPendingRequest reserve(IDcfUser user, Environment env, ReserveLevel level, String catalogueCode, String description)
			throws DetailedSOAPException {
		return sendAction(user, env, IUploadCatalogueFileImpl.fromReserveLevel(level), catalogueCode, description);
	}

	@Override
	public IPendingRequest unreserve(IDcfUser user, Environment env, String catalogueCode, String description) throws DetailedSOAPException {
		return sendAction(user, env, UploadCatalogueFileAction.UNRESERVE, catalogueCode, null);
	}

	@Override
	public IPendingRequest publish(IDcfUser user, Environment env, PublishLevel level, String catalogueCode) throws DetailedSOAPException {
		return sendAction(user, env, IUploadCatalogueFileImpl.fromPublishLevel(level), catalogueCode, null);
	}
	
	protected IPendingRequest sendAction(IDcfUser user, Environment env, UploadCatalogueFileAction action, 
			String catalogueCode, String description) throws DetailedSOAPException {
		
		String logCode = this.upc.send(env, user, "my-attachment");
		
		String type = action.getDatabaseCode();
		PendingRequest pr = new PendingRequest(type, user, logCode, env);
		
		pr.addData(UploadCatalogueFileImpl.CATALOGUE_CODE_DATA_KEY, catalogueCode);
		
		if (description != null)
			pr.addData(UploadCatalogueFileImpl.RESERVE_NOTE_DATA_KEY, description);
		
		return pr;
	}

	@Override
	public IPendingRequest uploadCatalogueFile(IDcfUser user, Environment env, String attachment,
			String uploadCatalogueFileType, Map<String, String> requestData) throws DetailedSOAPException, IOException {
		
		String logCode = this.upc.send(env, user, "my-attachment");
		
		PendingRequest pr = new PendingRequest(uploadCatalogueFileType, user, logCode, env);
		pr.setData(requestData);
		return pr;
	}

}
