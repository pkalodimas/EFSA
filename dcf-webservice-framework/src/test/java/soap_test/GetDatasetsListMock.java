package soap_test;

import config.Environment;
import dataset.IDcfDataset;
import dataset.IDcfDatasetsList;
import soap.DetailedSOAPException;
import soap_interface.IGetDatasetsList;
import user.IDcfUser;

public class GetDatasetsListMock<T extends IDcfDataset> implements IGetDatasetsList<T> {
	
	private IDcfDatasetsList<T> list;
	@Override
	public IDcfDatasetsList<T> getList(Environment env, IDcfUser user, String dcCode, IDcfDatasetsList<T> output) 
			throws DetailedSOAPException {
		
		if (this.list != null)
			for (T d: this.list)
				output.add(d);
		
		return this.list;
	}
	
	public void setList(IDcfDatasetsList<T> list) {
		this.list = list;
	}
}
