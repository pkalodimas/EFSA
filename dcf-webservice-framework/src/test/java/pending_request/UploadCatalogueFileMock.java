package pending_request;

import java.util.UUID;

import config.Environment;
import soap.DetailedSOAPException;
import soap_interface.IUploadCatalogueFile;
import user.IDcfUser;

public class UploadCatalogueFileMock implements IUploadCatalogueFile {

	@Override
	public String send(Environment env, IDcfUser user, String attachment) throws DetailedSOAPException {
		String uuid = UUID.randomUUID().toString();
		return "LOG_" + uuid + "_WS";
	}
}
