package soap_interface;

import config.Environment;
import soap.DetailedSOAPException;
import user.IDcfUser;

public interface IUploadCatalogueFile {
	public String send(Environment env, IDcfUser user, String attachment) throws DetailedSOAPException;
}
