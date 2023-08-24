package soap_interface;

import java.io.File;

import javax.xml.soap.SOAPException;

import config.Environment;
import soap.DetailedSOAPException;
import user.IDcfUser;

/**
 * Generic get file request to the dcf.
 * @author avonva
 *
 */
public interface IGetDataset {
	
	/**
	 * Get an handle to the downloaded dataset
	 * @return
	 * @throws SOAPException
	 */
	public File getDatasetFile(Environment env, IDcfUser user, String datasetId) throws DetailedSOAPException;
}
