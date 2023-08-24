package pending_request;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class CompoundFieldManager {
	
	/**
	 * Convert compound string a=1$b=2 into map {a=1, b=2}
	 * @param compoundField
	 * @return
	 */
	public static Map<String, String> stringToMap(String compoundField) {
		
		StringTokenizer st = new StringTokenizer(compoundField, "$");
		
		Map<String, String> map = new HashMap<>();
		
		if (!compoundField.isEmpty()) {
			while(st.hasMoreTokens()) {
				
				String[] split = st.nextToken().split("=");
				
				if (split.length != 2)
					throw new IllegalArgumentException("Wrong input parameter=" + compoundField);
				
				map.put(split[0], split[1]);
			}
		}
		
		return map;
	}
	
	/**
	 * Convert map {a=1, b=2} into compound string a=1$b=2 
	 * @param map
	 * @return
	 */
	public static String mapToString(Map<String, String> map) {

		StringBuilder sb = new StringBuilder();
		
		Iterator<String> iterator = map.keySet().iterator();
		while(iterator.hasNext()) {
			
			String key = iterator.next();
			String value = map.get(key);
			
			if (key.isEmpty() || value.isEmpty())
				throw new IllegalArgumentException("Invalid map elements. Either one key or one value is empty");
			
			sb.append(key).append("=").append(value);
			
			if (iterator.hasNext())
				sb.append("$");
		}
		
		return sb.toString();
	}
}
