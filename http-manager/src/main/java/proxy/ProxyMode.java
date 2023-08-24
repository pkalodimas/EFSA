package proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum ProxyMode {
	
	NO_PROXY("NO_PROXY", "None"),
	AUTO("AUTO", "Automatic detection"),
	MANUAL("MANUAL", "Manual setting");
	
	private static final Logger LOGGER = LogManager.getLogger(ProxyMode.class);
	private String keyword;
	private String label;
	
	ProxyMode(String keyword, String label) {
		this.keyword = keyword;
		this.label = label;
	}
	
	public static ProxyMode fromString(String keyword) {
		for (ProxyMode mode : ProxyMode.values()) {
			if (mode.keyword.equalsIgnoreCase(keyword)) {
				return mode;
			}
		}
		
		LOGGER.warn("No valid proxy mode found, using NO_PROXY");
		return NO_PROXY;
	}
	
	public String getKeyword() {
		return keyword;
	}
	
	public String getLabel() {
		return label;
	}
	
	@Override
	public String toString() {
		return getLabel();
	}
}
