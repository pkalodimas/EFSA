package dcf_log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;

public class DcfLogParserMock implements IDcfLogParser {

	@Override
	public DcfLog parse(File file) throws IOException {
		//solve memory leak
		try(FileInputStream f = new FileInputStream(file)){
			return this.parse(f);
		}
	}

	@Override
	public DcfLog parse(InputStream input) throws IOException {
		return new DcfLog("reserve", new Timestamp(1029830193), 
				new Timestamp(1029830193), "reserve.xml", 
				"ACTION", "1.2", "RESERVED MINOR", "OK", 
				DcfResponse.OK, new ArrayList<>(), 
				new ArrayList<>(), new ArrayList<>());
	}
}
