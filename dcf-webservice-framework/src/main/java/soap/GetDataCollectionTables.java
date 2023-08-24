package soap;

import java.io.File;
import java.io.IOException;

import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;

import config.Environment;
import data_collection.IDcfDCTable;
import data_collection.IDcfDCTableLists;
import response_parser.DCResourceParser;
import user.IDcfUser;

public class GetDataCollectionTables<T extends IDcfDCTable> extends GetFile {
	
	public IDcfDCTableLists<T> getTables(Environment env, IDcfUser user, 
			String resourceId, IDcfDCTableLists<T> output) throws SOAPException, IOException, XMLStreamException {
		
		IDcfDCTableLists<T> tables = null;
		
		File file = getFile(env, user, resourceId);
		
		if (file == null)
			return null;
		
		try(DCResourceParser<T> parser = new DCResourceParser<>(output, file);) {
			tables = parser.parse();
		}
		
		return tables;	
	}
}
