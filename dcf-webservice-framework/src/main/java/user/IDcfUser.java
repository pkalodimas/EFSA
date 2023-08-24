package user;

public interface IDcfUser {

	/**
	 * Get the user name of the user
	 * @return
	 */
	public String getUsername();
	
	/**
	 * Get the password of the user
	 * @return
	 */
	public String getPassword();
	
	/**
	 * Get the type of user (openapi = true, dcf = false)
	 * @author shahaal
	 * @return
	 */
	public boolean isOpeanapi();
}