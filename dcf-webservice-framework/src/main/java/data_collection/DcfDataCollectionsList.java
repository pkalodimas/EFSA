package data_collection;

import java.util.ArrayList;

public class DcfDataCollectionsList extends ArrayList<IDcfDataCollection> 
	implements IDcfDataCollectionsList<IDcfDataCollection> {
	
	private static final long serialVersionUID = 1275212356670435939L;

	@Override
	public IDcfDataCollection create() {
		return new DcfDataCollection();
	}
}
