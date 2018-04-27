package generator.util;


public class Util {
	
	public static String transformName(String name, boolean className, int start) {
		name = name.toLowerCase().substring(start);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if(c == '_') {
				if(i < name.length() - 1) {
					char nc = name.charAt(i + 1);
					sb.append(String.valueOf(nc).toUpperCase());
					i++; 
				}
			} else {
				sb.append(String.valueOf(c));
			}
		}
		if(className)
			return sb.toString().substring(0, 1).toUpperCase() + sb.toString().substring(1);
		else
			return sb.toString().substring(0, 1).toLowerCase() + sb.toString().substring(1);
	}
	/**
	 * 获得字段的get方法名称
	 * @param fieldName
	 * @return 
	 */
	public static String getGetterMethodName(String fieldName){
		return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
	}
	/**
	 * 获得字段的set方法名称
	 * @param fieldName
	 * @return 
	 */
	public static String getSetterMethodName(String fieldName){
		return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
	}
}
