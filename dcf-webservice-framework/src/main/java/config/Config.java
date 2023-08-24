package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import proxy.ProxyMode;

/**
 * Configuration of the application
 * @author avonva
 * @author shahaal
 */
public class Config {
	
	public static final String TEMP_FOLDER = "temp" + System.getProperty("file.separator");
	
	private static final String PROXY_CONFIG_PATH = "config" + System.getProperty("file.separator") 
										+ "proxyConfig.xml";
	
	private static final String PARENT_PROXY_CONFIG_PATH = ".." + System.getProperty("file.separator") 
										+ PROXY_CONFIG_PATH;
	
	private static final String ENV_CONFIG_PATH = "config" + System.getProperty("file.separator") + "env.xml";
	private static final String ENV_TYPE = "Environment.Production";
	
	public static final String PROXY_HOST_NAME = "Proxy.ManualHostName";
	public static final String PROXY_PORT = "Proxy.ManualPort";
	public static final String PROXY_MODE = "Proxy.Mode";
	
	public static String getProxyConfigPath() {
		return Config.PROXY_CONFIG_PATH;
	}
	
	private static String getProxyConfigFromParent(String key) {
		return getValue(PARENT_PROXY_CONFIG_PATH, key);
	}
	
	/**
	 * Get customized proxy hostname
	 * @return
	 */
	public static String getProxyHostname() {
		
		String name = getProxyConfigFromParent(PROXY_HOST_NAME);
				
		if (name == null)
			return getValue(PROXY_CONFIG_PATH, PROXY_HOST_NAME);
		
		return name;
	}
	
	/**
	 * Get customized proxy port
	 * @return
	 */
	public static String getProxyPort() {
		
		String port = getProxyConfigFromParent(PROXY_PORT);
		
		if (port == null)
			return getValue(PROXY_CONFIG_PATH, PROXY_PORT);
		
		return port;
	}
	
	public static ProxyMode getProxyMode() {
		
		String mode = getProxyConfigFromParent(PROXY_MODE);
		
		if (mode == null)
			mode = getValue(PROXY_CONFIG_PATH, PROXY_MODE);

		return ProxyMode.fromString(mode);
	}
	
	public static Environment getEnvironment() {
		String production = getValue(ENV_CONFIG_PATH, ENV_TYPE);

		if (production == null)
			return Environment.TEST;
		
		boolean isProd = production.equalsIgnoreCase("YES")
				|| production.equalsIgnoreCase("Y");

		return isProd ? Environment.PRODUCTION : Environment.TEST;
	}
	
	public static boolean isProductionEnvironment() {
		return getEnvironment() == Environment.PRODUCTION;
	}
	
	/**
	 * Read the application properties from the xml file
	 * @return
	 */
	public static Properties getProperties(String filename) {
		
		Properties properties = new Properties();

		try(InputStream stream = new FileInputStream(filename)) {
			properties.loadFromXML(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return properties;
	}
	
	private static String getValue(String propertiesFilename, String property) {
		
		Properties prop = getProperties(propertiesFilename);
		
		if (prop == null)
			return null;
		
		String value = prop.getProperty(property);
		
		return value;
	}
}
