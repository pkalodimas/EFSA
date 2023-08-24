package data_collection;

import java.util.ArrayList;

public class DcfDCTablesList extends ArrayList<DcfDCTable> implements IDcfDCTableLists<DcfDCTable> {

	private static final long serialVersionUID = -2547404158763272542L;

	@Override
	public DcfDCTable create() {
		return new DcfDCTable();
	}

	@Override
	public IDcfCatalogueConfig createConfig() {
		return new DcfCatalogueConfig();
	}

}
