package release;

import java.io.IOException;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import config.GithubChecker;
import config.GithubConfig;

public class ReleaseParser {

	private String jsonInput;
	private JsonValue value;
	
	/**
	 * Parser for a github release. It extracts several useful information
	 * needed to check if the app is up to date and to download new versions of it.
	 * @param jsonInput the github release in json format
	 */
	public ReleaseParser(String jsonInput) {
		this.jsonInput = jsonInput;
	}
	
	/**
	 * Parse the json input
	 * @return
	 */
	private JsonValue getJson() {
		
		if (this.value == null) {
			this.value = Json.parse(jsonInput);
		}
		
		return this.value;
	}
	
	/**
	 * Get the version of the release
	 * @return
	 */
	public String getVersion() {
		JsonValue value = getJson();
		return value.asObject().getString("tag_name", GithubChecker.DEFAULT_VERSION);  // default first version
	}
	
	/**
	 * Get the url from which it can be downloaded the last release
	 * @return
	 * @throws IOException 
	 */
	public String getDownloadUrl() throws IOException {

		// get assets
		JsonObject asset = GetAppRelease();
		
		String url = asset.getString("browser_download_url", "");
		
		return url;
	}
	
	/**
	 * Get the first github release asset contained
	 * in the assets array
	 * @return
	 */
	public JsonObject getFirstAsset() {
		
		JsonArray assets = getAssets();
		
		JsonObject first = null;
		
		// get first element of the assets
		if (!assets.isEmpty()) {
			first = assets.get(0).asObject();
		}
		
		return first;
	}
	
	/**
	 * Get the release url from which the app can
	 * be downloaded.
	 * @return
	 */
	public JsonObject GetAppRelease() {
		
		JsonArray assets = getAssets();
		
		GithubConfig config = new GithubConfig();
		
		// keyword to identify the file that needs to be downloaded
		String appKeyword = config.getValue(GithubConfig.APP_KEYWORD_NAME);
		
		for (JsonValue value : assets) {
			String assetName = value.asObject().getString("name", "");
			if (assetName.contains(appKeyword)) {
				return value.asObject();
			}
		}
		
		System.err.println("Cannot find application version with " + appKeyword 
				+ " flag in its name. Example tool-" + appKeyword + ".zip");
		System.err.println("Using first available asset instead");
		
		return getFirstAsset();
	}
	
	/**
	 * Get all the release assets
	 * @return
	 */
	public JsonArray getAssets() {
		
		JsonValue value = getJson();
		
		// get assets
		JsonArray assets = value.asObject().get("assets").asArray();
		
		return assets;
	}
	
	/**
	 * Get the size of the release (needed to download
	 * the app with {@link FileDownloader})
	 * @return
	 */
	public int getSize() {
		
		// get assets
		JsonObject app = GetAppRelease();
		if (app == null) {
			return -1;
		}
		
		int size = app.getInt("size", -1);
		
		return size;
	}
}
