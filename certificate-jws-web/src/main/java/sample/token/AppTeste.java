package sample.token;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AppTeste {
	
	
	//private static Map<String,  List<Files>> map = Collections.synchronizedMap(new HashMap<String, List<Files>>());
	private static Map<String,  Map<String, String>> map = Collections.synchronizedMap(new HashMap<String, Map<String, String>>());
	
	public static void main(String[] args) {
		
//		Files files1 = new Files();
//		files1.setFile("teste 1");
//		
//		Files files2 = new Files();
//		files2.setFile("teste 2");
//		
//		List<Files> list = new ArrayList<Files>();
//		list.add(files1);
//		list.add(files2);
//		
//		map.put("1", list);
//		
//		for (Files files : map.get("1")) {
//			System.out.println(files.getFile());
//		}
//		
//			System.out.println("Tem " + map.get("1").get(0).getFile());
		
		
		Map<String, String> files = Collections.synchronizedMap(new HashMap<String, String>());
		files.put("teste1",null);
		files.put("teste2",null);
		
		for (String names : files.keySet()) {
			System.out.println(names);
		}
		System.out.println(files.keySet());
		
		map.put("1", files);
		System.out.println(map.get("1").size());
		map.get("1").put("teste1","teste");
		System.out.println(map.get("1").size());
		System.out.println(map.get("1").get("teste1"));
		

		
	}
	

}
