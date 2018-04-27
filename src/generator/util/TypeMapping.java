package generator.util;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TypeMapping {
	private static final Map<String, Class<?>> mappings = new HashMap<String, Class<?>>();
	static {
		mappings.put("int", Integer.class);
		mappings.put("tinyint", Integer.class);
		mappings.put("smallint", Integer.class);
		mappings.put("mediumint", Integer.class);
		mappings.put("bigint", Long.class);
		mappings.put("double", Double.class);
		
		mappings.put("char", String.class);
		mappings.put("varchar", String.class);
		mappings.put("tinytext", String.class);
		mappings.put("text", String.class);
		mappings.put("mediumtext", String.class);
		mappings.put("longtext", String.class);
		
		mappings.put("datetime", Date.class);
		mappings.put("date", Date.class);
		mappings.put("timestamp", Timestamp.class);
		mappings.put("time", Timestamp.class);
	}
	
	
	public static String getJavaType(String mysqltype) {
		Class<?> clazz = mappings.get(mysqltype);
		if(null == mappings) {
			throw new RuntimeException("没有找到对应类型，请补充：" + mysqltype + " -> ?" );
		}
		return clazz.getName();
	}
	
	public static String getSimpleTypeName(String mysqltype) {
		Class<?> clazz = mappings.get(mysqltype);
		if(null == clazz) {
			throw new RuntimeException("没有找到对应类型，请补充：" + mysqltype + " -> ?" );
		}
		return clazz.getSimpleName();
	}
}
