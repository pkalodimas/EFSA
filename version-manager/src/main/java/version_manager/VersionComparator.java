package version_manager;

import java.util.Comparator;

public class VersionComparator implements Comparator<String> {
	
	/**
	 * Compare two versions dot separated (as 1.1, 1.0.2.1)
	 * The left digits are considered the most significant.
	 * @return 
	 * <ul>
	 * 	<li>0 if the versions are equal</li>
	 * 	<li>-1 if {@code version1} is older than {@code version2}</li>
	 * 	<li>1 if {@code version1} is newer than {@code version2}</li>
	 * </ul>
	 */
	public int compare(String version1, String version2) {
		
		if (version1.equals(version2))
			return 0;
		
		String[] version1Parts = version1.split("\\.");
		String[] version2Parts = version2.split("\\.");
		
		int compare = 0;
		for (int i = 0; i < Math.min(version1Parts.length, version2Parts.length); ++i) {
			
			// if a number is not found, the default is 0
			int version1Num = i < version1Parts.length ? Integer.parseInt(version1Parts[i]) : 0;
			int version2Num = i < version2Parts.length ? Integer.parseInt(version2Parts[i]) : 0;
			
			if (version1Num == version2Num)
				continue;
			
			if (version1Num < version2Num) {
				compare = -1;
				break;
			}
			else {
				compare = 1;
				break;
			}
		}

		return compare;
	}
}
