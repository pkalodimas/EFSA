package dcf_log;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Builder to create a single LogNode step by step.
 * 
 * @author avonva
 * @author shahaal
 */
public class LogNodeBuilder {
	
	private static final Logger LOGGER = LogManager.getLogger(LogNodeBuilder.class);

	private String name;
	private DcfResponse result;
	private Collection<String> opLogs;

	public LogNodeBuilder() {
		this.opLogs = new ArrayList<>();
	}

	public LogNodeBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public LogNodeBuilder setResult(String result) {

		try {
			this.result = DcfResponse.valueOf(result);
		} catch (IllegalArgumentException e) {
			LOGGER.error("Error while getting value ", e);
			e.printStackTrace();
			this.result = DcfResponse.ERROR;
		}

		return this;
	}

	public LogNodeBuilder setResult(DcfResponse result) {
		this.result = result;
		return this;
	}

	public LogNodeBuilder addOpLog(String opLog) {
		this.opLogs.add(opLog);
		return this;
	}

	public void setOpLogs(Collection<String> opLogs) {
		this.opLogs = opLogs;
	}

	public LogNode build() {
		return new LogNode(this.name, this.result, this.opLogs);
	}

	@Override
	public String toString() {
		return "name=" + this.name + "; result=" + this.result + "; opLogs=" + this.opLogs;
	}
}
