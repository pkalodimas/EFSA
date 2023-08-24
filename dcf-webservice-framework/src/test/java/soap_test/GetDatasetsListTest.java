package soap_test;

import config.Environment;
import dataset.DcfDatasetsList;
import dataset.IDcfDataset;
import soap.DetailedSOAPException;
import soap.GetDatasetsList;
import user.DcfUser;

public class GetDatasetsListTest {

	public static void main(String[] args) throws DetailedSOAPException {
		DcfDatasetsList output = new DcfDatasetsList();
		GetDatasetsList<IDcfDataset> request = new GetDatasetsList<>();
		DcfUser user = new DcfUser();
		user.login("avonva", "Maniago92!");
		request.getList(Environment.PRODUCTION, user, "TSE.TEST", output);
		
		System.out.println(output);
	}
}
