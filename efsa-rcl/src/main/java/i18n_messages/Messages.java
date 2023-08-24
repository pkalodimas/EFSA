package i18n_messages;

import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {
	
	private static final String BUNDLE_NAME = "rcl_messages_en";

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);

	private static RCLBundle bundle = new RCLBundle(RESOURCE_BUNDLE);
	
	public static String get(String key, String... values) {
		return bundle.get(key, values);
	}
}