package sample.token;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TokenManager {
	
	
	private static Map<String,  Map<String, String>> map = Collections.synchronizedMap(new HashMap<String, Map<String, String>>());

	public static String put(Map<String, String> files) {
		String token = UUID.randomUUID().toString();

		map.put(token,files);
		return token;
	}

	public static Map<String, String> get(String token) {
		return map.get(token);
	}

	public static void destroy(String token) {
		map.remove(token);
	}

}
