package user_interface;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import config.ProxyConfig;
import http.HttpManager;
import proxy.ProxyMode;

public class Actions {
	
	private static final Logger LOGGER = LogManager.getLogger(Actions.class);

	/**
	 * Test the connection with proxy settings
	 * @param url
	 * @return
	 */
	public static boolean testConnection(String url) {
		boolean success;
		try {
			HttpManager.get(url);
			success = true;
		} catch (IOException e) {
			LOGGER.error("There is an error upon testing the connection ", e);
			e.printStackTrace();
			success = false;
		}
		
		return success;
	}
	
	/**
	 * Save the configuration
	 * @param mode
	 * @param hostname
	 * @param port
	 * @throws IOException 
	 */
	public static void save(ProxyMode mode, String hostname, String port) throws IOException {
		ProxyConfig config = new ProxyConfig();
		config.setProxyMode(mode);
		config.setProxyHostname(hostname);
		config.setProxyPort(port);
	}
}
