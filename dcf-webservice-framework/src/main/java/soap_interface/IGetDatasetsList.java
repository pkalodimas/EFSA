package soap_interface;

import config.Environment;
import dataset.IDcfDataset;
import dataset.IDcfDatasetsList;
import soap.DetailedSOAPException;
import user.IDcfUser;

/**
 * Get dataset list request for the DCF webservice. It can be used
 * by calling {@link #getList()} to get all the dataset of an user
 * @author avonva
 *
 */
public interface IGetDatasetsList<T extends IDcfDataset> {
	
	/**
	 * Send the request and get the dataset list
	 * @throws DetailedSOAPException
	 */
	public IDcfDatasetsList<T> getList(Environment env, IDcfUser user, String dcCode, 
			IDcfDatasetsList<T> output) throws DetailedSOAPException;
}
