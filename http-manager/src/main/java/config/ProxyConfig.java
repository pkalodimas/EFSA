package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import proxy.ProxyMode;

/**
 * Configuration of the application
 * @author avonva
 * @author shahaal
 */
public class ProxyConfig {
	
	private static final Logger LOGGER = LogManager.getLogger(ProxyConfig.class);

	private String PROXY_CONFIG_PATH = "config/proxyConfig.xml";
	
	private String PARENT_PROXY_CONFIG_PATH = ".." + System.getProperty("file.separator") 
										+ PROXY_CONFIG_PATH;
	
	public static final String PROXY_HOST_NAME = "Proxy.ManualHostName";
	public static final String PROXY_PORT = "Proxy.ManualPort";
	public static final String PROXY_MODE = "Proxy.Mode";

	/**
	 * Set the path where the config folder is present, by the default is current directory
	 * @param basePath
	 */
	public ProxyConfig(String basePath) {
		this.PROXY_CONFIG_PATH = basePath + PROXY_CONFIG_PATH;
	}

	public ProxyConfig() {}
	
	/**
	 * Check if the configuration file is in the
	 * current folder or in the parent folder
	 * @return
	 */
	private boolean isInCurrentFolder() {
		File file = new File(PROXY_CONFIG_PATH);
		LOGGER.info("The configuration file is in the current folder: " + file.exists());
		return file.exists();
	}
	
	/**
	 * Get the path where the config file is
	 * stored
	 * @return
	 */
	private String getPath() {
		if (isInCurrentFolder())
			return PROXY_CONFIG_PATH;
		else
			return PARENT_PROXY_CONFIG_PATH;
	}
	
	public String getConfigPath() {
		return this.PROXY_CONFIG_PATH;
	}
	
	private String getFromParent(String key) throws IOException {
		return getValue(PARENT_PROXY_CONFIG_PATH, key);
	}
	
	/**
	 * Get customized proxy hostname
	 * @return
	 * @throws IOException 
	 */
	public String getProxyHostname() throws IOException {
		
		String name;
		try {
			name = getValue(PROXY_CONFIG_PATH, PROXY_HOST_NAME);
		} catch (IOException e) {
			LOGGER.error("Error in getting value for proxy hostname: ", e);
			name = getFromParent(PROXY_HOST_NAME);
		}

		return name;
	}
	
	/**
	 * Get customized proxy port
	 * @return
	 */
	public String getProxyPort() throws IOException {
		
		String port;
		try {
			port = getValue(PROXY_CONFIG_PATH, PROXY_PORT);
		}
		catch (IOException e) {
			LOGGER.error("Error in getting value for proxy port: ", e);
			port = getFromParent(PROXY_PORT);
		}
		
		return port;
	}
	
	public ProxyMode getProxyMode() throws IOException {
		
		String mode;
		try {
			mode = getValue(PROXY_CONFIG_PATH, PROXY_MODE);
		} catch (IOException e) {
			LOGGER.error("Error in getting value for proxy mode: ", e);
			mode = getFromParent(PROXY_MODE);
		}

		return ProxyMode.fromString(mode);
	}
	
	public void setProxyMode(ProxyMode mode) throws IOException {
		String config = getPath();
		setValue(config, PROXY_MODE, mode.getKeyword());
	}
	
	public void setProxyHostname(String hostname) throws IOException {
		String config = getPath();
		setValue(config, PROXY_HOST_NAME, hostname);
	}
	
	public void setProxyPort(String port) throws IOException {
		String config = getPath();
		setValue(config, PROXY_PORT, port);
	}
	
	/**
	 * Read the application properties from the xml file
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public Properties getProperties(String filename) throws IOException {
		Properties properties = new Properties();

		try(InputStream stream = new FileInputStream(filename)) {
			properties.loadFromXML(stream);
		}
		LOGGER.info("Application properties from the xml file: ", properties);
		return properties;
	}
	
	private String getValue(String propertiesFilename, String property) throws IOException {
		
		Properties prop = getProperties(propertiesFilename);
		
		if (prop == null)
			return null;
		
		String value = prop.getProperty(property);
		
		return value;
	}
	
	/**
	 * Set a value in the file
	 * @param propertiesFilename
	 * @param property
	 * @param value
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private void setValue(String propertiesFilename, String property, String value) throws IOException {
		Properties prop = getProperties(propertiesFilename);
		prop.setProperty(property, value);
		
		try(FileOutputStream outputStream = new FileOutputStream(propertiesFilename);) {
			prop.storeToXML(outputStream, null);
		}
	}
}
