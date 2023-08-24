package pending_request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class CompoundFieldManagerTest {

	@Test
	public static void stringToMap1() {
		Map<String, String> map = CompoundFieldManager.stringToMap("key=value");
		assertEquals(map.size(), 1);
	}
	
	@Test
	public static void stringToMap2() {
		Map<String, String> map = CompoundFieldManager.stringToMap("");
		assertEquals(map.size(), 0);
	}
	
	@Test
	public static void stringToMap3() {
		Map<String, String> map = CompoundFieldManager.stringToMap("key1=value1$key2=value2$key3=value3");
		assertEquals(map.size(), 3);
	}
	
	@Test
	public static void mapToString1() {
		Map<String, String> map = new HashMap<>();
		String compound = CompoundFieldManager.mapToString(map);
		assertEquals(compound.isEmpty(), true);
	}
	
	@Test
	public static void mapToString2() {
		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		String compound = CompoundFieldManager.mapToString(map);
		assertEquals(compound, "key1=value1");
	}
	
	@Test
	public static void mapToString3() {
		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		String compound = CompoundFieldManager.mapToString(map);
		assertTrue(compound.equals("key1=value1$key2=value2") 
				|| compound.equals("key2=value2$key1=value1"));
	}
	
	@Test
	public static void stringToMapError1() {
		assertThrows(IllegalArgumentException.class, () -> CompoundFieldManager.stringToMap("key=value$fojk"));
	}
	
	@Test
	public static void stringToMapError2() {
		assertThrows(IllegalArgumentException.class, () -> CompoundFieldManager.stringToMap("key"));
	}
	
	@Test
	public static void stringToMapError3() {
		assertThrows(IllegalArgumentException.class, () -> CompoundFieldManager.stringToMap("key$oifja$jfs"));
	}
	
	@Test
	public static void mapToStringError1() {
		Map<String, String> map = new HashMap<>();
		map.put("", "");
		assertThrows(IllegalArgumentException.class, () -> CompoundFieldManager.mapToString(map));
	}
	
	@Test
	public static void mapToStringError2() {
		Map<String, String> map = new HashMap<>();
		map.put("", "dsads");
		assertThrows(IllegalArgumentException.class, () -> CompoundFieldManager.mapToString(map));
	}	
	
	@Test
	public static void mapToStringError3() {
		Map<String, String> map = new HashMap<>();
		map.put("dsads", "");
		assertThrows(IllegalArgumentException.class, () -> CompoundFieldManager.mapToString(map));
	}
}
