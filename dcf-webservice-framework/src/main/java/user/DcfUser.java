package user;

import config.Environment;
import soap.DetailedSOAPException;
import soap.ExportCatalogueFile;
import soap.Ping;

/**
 * class used to provide essential methods to extended user profile
 * 
 * @author shahaal
 * @author avonva
 */
public class DcfUser implements IDcfUser {

	private String username;
	private String password;
	// true if opeanpi, false if dcf
	private boolean userType;

	/**
	 * Login the user (DCF or openapi)
	 * 
	 * @param username1
	 * @param password1
	 * @param type
	 */
	public void login(String username1, String password1, boolean type) {
		this.username = username1;
		this.password = password1;
		this.userType = type;
	}

	/**
	 * Login the user (only DCF)
	 * 
	 * @param username1
	 * @param password1
	 */
	public void login(String username1, String password1) {
		this.username = username1;
		this.password = password1;
		this.userType = false;
	}

	/**
	 * Login the user and check if credentials are correct
	 * 
	 * @param username1
	 * @param password1
	 * @return
	 * @throws DetailedSOAPException
	 */
	public boolean verifiedLogin(Environment env, String username1, String password1) throws DetailedSOAPException {

		this.login(username1, password1);

		boolean logged = false;

		// try a ping request to check credentials
		try {
			Ping request = new Ping();
			logged = request.ping(env, this);
		} catch (DetailedSOAPException e) {
			throw e;
		}

		// if wrong credential => remove them
		if (!logged) {
			logout();
		}

		return logged;
	}

	/**
	 * Login the user and check if credentials are correct by downloading the dump
	 * catusers catalogue
	 * 
	 * @author shahaal
	 * @param env
	 * @param username1
	 * @param password1
	 * @param catusers
	 * @return
	 * @throws DetailedSOAPException
	 */
	public boolean verifiedLoginOpenapi(Environment env, String username1, String token, String catusers)
			throws DetailedSOAPException {

		this.login(username1, token, true);

		boolean logged = false;

		// try a ping request to check credentials
		try {
			ExportCatalogueFile request = new ExportCatalogueFile();
			request.exportCatalogue(env, this, catusers);
			logged = true;
		} catch (DetailedSOAPException e) {
			throw e;
		}

		// if wrong credential => remove them
		if (!logged) {
			logout();
		}
		
		return logged;
	}

	public void logout() {
		this.username = null;
		this.password = null;
	}

	/**
	 * Get the saved username
	 * 
	 * @return
	 */
	@Override
	public String getUsername() {
		return this.username;
	}

	/**
	 * Get the saved password/token
	 * 
	 * @return
	 */
	@Override
	public String getPassword() {
		return this.password;
	}

	/**
	 * Get the user type(opeanpi/dcf)
	 * 
	 * @return
	 */
	@Override
	public boolean isOpeanapi() {
		return this.userType;
	}

	/**
	 * Check if the user is logged in
	 * 
	 * @return
	 */
	public boolean isLoggedIn() {
		return this.username != null && this.password != null;
	}

	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof IDcfUser))
			return super.equals(obj);

		return this.username.equals(((IDcfUser) obj).getUsername());
	}

	@Override
	public String toString() {
		return this.username;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}