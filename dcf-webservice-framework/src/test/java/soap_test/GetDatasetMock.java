package soap_test;

import java.io.File;
import java.util.HashMap;

import config.Environment;
import soap.DetailedSOAPException;
import soap_interface.IGetDataset;
import user.IDcfUser;

public class GetDatasetMock implements IGetDataset {

	private HashMap<String, File> datasets;
	
	public GetDatasetMock() {
		this.datasets = new HashMap<>();
	}
	
	/**
	 * Set the file which will be returned
	 * @param file
	 */
	public void addDatasetFile(String datasetId, File file) {
		this.datasets.put(datasetId, file);
	}
	
	@Override
	public File getDatasetFile(Environment env, IDcfUser user, String datasetId) throws DetailedSOAPException {
		return this.datasets.get(datasetId);
	}
}
