package data_collection;

import response_parser.IDcfList;

public interface IDcfDCTableLists<T extends IDcfDCTable> extends IDcfList<T> {
	public IDcfCatalogueConfig createConfig();
}
