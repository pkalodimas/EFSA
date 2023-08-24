package dcf_log;

import java.util.Collection;

/**
 * Class modeling an xml node contained in the Dcf log document
 * and representing the result of an upload operation.
 * @author avonva
 * @author shahaal
 */
public class LogNode {

	private String name;
	private DcfResponse result;
	private Collection<String> opLogs;
	
	public LogNode( String name, DcfResponse result, Collection<String> opLogs ) {
		this.name = name;
		this.result = result;
		this.opLogs = opLogs;
	}
	
	public String getName() {
		return this.name;
	}
	
	public DcfResponse getResult() {
		return this.result;
	}
	
	public Collection<String> getOpLogs() {
		return this.opLogs;
	}
	
	/**
	 * Check if the log node result is: correct
	 * @return
	 */
	public boolean isOperationCorrect() {
		return this.result == DcfResponse.OK;
	}
	
	@Override
	public String toString() {
		return "LogNode: name=" + this.name + ";result=" + this.result + ";opLogs=" + this.opLogs;
	}
}
