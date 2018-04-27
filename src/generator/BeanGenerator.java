package generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import generator.util.ColumnDef;
import generator.util.TypeMapping;
import generator.util.Util;

public class BeanGenerator {
	/** mysql连接信息 */
	private static final String conn_string = "jdbc:mysql://localhost:3306/test";
	/** 数据库名 */
	private static final String databaseName = "test";
	/** 数据库用户名 */
	private static final String username = "root";
	/** 数据库密码 */
	private static final String password = "123456";	
	/** Bean的包名 */
	private static final String packageName = "com.xxx";
	/** 类文件的编码 */
	private static final String fileEncoding = "UTF-8";
	/** 作者 */
	private static final String author = "Hetianyi";
	/** 版本 */
	private static final String version = "1.0";
	/** 指定转换的表，未指定则转换全部表 */
	private static final Set<String> tables = new HashSet<String>();
	/** 是否序列化 */
	private static final boolean isSerialized = true;
	/** 是否为类和字段加注解 */
	private static final boolean addAnnotation = false;
	/** 是否使用lombok来代替get,set方法 */
	private static final boolean useLombok = false;
	
	static {
		//tables.add("article");
	}
	
	private static boolean isEmpty(String input) {
		return null == input || input.trim().length() == 0;
	}
	/**
	 * 创建包文件夹
	 */
	private static File packageFile;
	private static void mksrcdirs() {
		File file = new File("src").getAbsoluteFile();
		if(!isEmpty(packageName)) {
			String[] packgelevel = packageName.split("\\.");
			String path = "";
			if(null != packgelevel && packgelevel.length > 0) {
				for(String level : packgelevel) {
					path += level + "/";
					packageFile = new File(file, path.substring(0, path.length() - 1)).getAbsoluteFile();
					System.out.println(packageFile.getAbsolutePath());
					if(!packageFile.exists()) {
						packageFile.mkdirs();
					}
				}
			}
		}
	}
	
	private static Connection getConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection(conn_string, username, password);
		return conn;
	}
	/**
	 * 获取数据库中所有表（包括视图）
	 */
	private static List<Map<String, String>> getTables(Connection conn) throws SQLException {
		List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
		String sql = "select * from information_schema.`TABLES` a where a.TABLE_SCHEMA = '"+ databaseName +"'";
		Statement state = conn.createStatement();
		ResultSet rs = state.executeQuery(sql);
		rs.beforeFirst();
		while(rs.next()) {
			Map<String, String> item = new HashMap<String, String>();
			String tn = rs.getString("TABLE_NAME");
			String tt = rs.getString("TABLE_TYPE");
			String tc = rs.getString("TABLE_COMMENT");
			item.put("TABLE_NAME", tn);
			item.put("TABLE_TYPE", tt);
			item.put("TABLE_COMMENT", tc);
			ret.add(item);
		}
		rs.close();
		state.close();
		return ret;
	}
	
	private static boolean parse(String tabType) {
		if("VIEW".equals(tabType))
			return false;
		return true;
	}
	
	
	private static String table_schema = "select a.* from information_schema.`COLUMNS` a where a.TABLE_SCHEMA='"+ databaseName +"' and a.TABLE_NAME = ";
	private static void parseTable(Map<String, String> table, Connection conn) throws Exception {
		String tn = table.get("TABLE_NAME");
		List<ColumnDef> defs = new ArrayList<ColumnDef>();
		String tt = table.get("TABLE_TYPE");
		if(parse(tt)) {
			Statement state = conn.createStatement();
			ResultSet rs = state.executeQuery(table_schema + "'"+ tn +"'");
			rs.beforeFirst();
			while(rs.next()) {
				ColumnDef def = new ColumnDef();
				def.setColumnName(rs.getString("COLUMN_NAME"));
				def.setColumnType(rs.getString("DATA_TYPE"));
				def.setId(rs.getString("COLUMN_KEY"));
				def.setSerialized(isSerialized);
				def.setAutoIncre(rs.getString("EXTRA"));
				def.setComment(rs.getString("COLUMN_COMMENT"));
				defs.add(def);
			}
			rs.close();
			state.close();
		}
		createBean(table, defs);
	}
	
	private static String gettype(String type) {
		return "VIEW".equals(type) ? "view " : "table";
	}
	
	private static void createBean(Map<String, String> table, List<ColumnDef> defs) throws IOException {
		String tn = table.get("TABLE_NAME");
		if(!tables.isEmpty() && !tables.contains(tn)) {
			return;
		}
		String beanName = Util.transformName(tn, true, 0);
		System.out.println("export -> [" + gettype(table.get("TABLE_TYPE")) + "]" + packageName + "." + beanName);
		File file = new File(packageFile, beanName + ".java");
		if(file.exists())
			file.delete();
		file.createNewFile();
		FileOutputStream ops = new FileOutputStream(file);
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(ops, fileEncoding)));
		
		Set<String> imports = new TreeSet<String>();
		StringBuilder file_body = new StringBuilder("");
		String package_body = "package " + packageName + ";\n";
		StringBuilder class_body = new StringBuilder("");
		StringBuilder getset_body = new StringBuilder("");
		
		//-----------------------------类注释开始
		class_body.append("/**\n * ");
		class_body.append(table.get("TABLE_COMMENT"));
		class_body.append("\n * @author ").append(author);
		class_body.append("\n * @version ").append(version);
		class_body.append("\n */");
		//-----------------------------类注释结束
		if (addAnnotation) {
			class_body.append("\n@Entity\n@Table(name=\"").append(tn).append("\")");
			imports.add("javax.persistence.Entity");
			imports.add("javax.persistence.Table");
		}
		if (useLombok) {
			class_body.append("\n@Getter\n@Setter\n@ToString");
			imports.add("lombok.Getter");
			imports.add("lombok.Setter");
			imports.add("lombok.ToString");
		}
		class_body.append("\npublic class ").append(beanName).append(" ");
		
		
		if(isSerialized) {
			class_body.append("implements Serializable {\n\n")
			.append("\tprivate static final long serialVersionUID = 1L;\n");
			if (addAnnotation) {
				imports.add("java.io.Serializable");
			}
		} else {
			class_body.append("{\n");
		}
		
		file_body.append(package_body);
		
		for(ColumnDef def : defs) {
			String fieldName = Util.transformName(def.getColumnName(), false, 0);
			String columnType = def.getColumnType();
			String comment = def.getComment();
			if(addAnnotation && columnType.startsWith("time")) {
				imports.add("java.sql.Timestamp");
			}
			if(addAnnotation && columnType.startsWith("date")) {
				imports.add("java.util.Date");
			}
			
			if(null != comment) {
				class_body.append("\n\t/** ").append(comment.replace("\n", "\n\t")).append(" */");
			}
			
			if(addAnnotation && def.isId()) {
				class_body.append("\n\t@Id");
				imports.add("javax.persistence.Id");
			}
			if(addAnnotation && def.isAutoIncre()) {
				class_body.append("\n\t@GeneratedValue");
				imports.add("javax.persistence.GeneratedValue");
			}
			if (addAnnotation) {
				class_body.append("\n\t@Column(name=\"").append(def.getColumnName()).append("\")");
				imports.add("javax.persistence.Column");
			}
			
			class_body.append("\n\tprivate ").append(TypeMapping.getSimpleTypeName(def.getColumnType())).append(" ")
			.append(fieldName).append(";\n");
			//get方法
			getset_body.append("\tpublic ").append(TypeMapping.getSimpleTypeName(def.getColumnType())).append(" ")
			.append(Util.getGetterMethodName(fieldName)).append("() {");
			getset_body.append("\n\t\treturn this.").append(fieldName).append(";\n\t}\n");
			//set方法
			getset_body.append("\n\tpublic ").append("void").append(" ")
			.append(Util.getSetterMethodName(fieldName)).append("(")
			.append(TypeMapping.getSimpleTypeName(def.getColumnType())).append(" ").append(fieldName).append(") {")
			.append("\n\t\tthis.").append(fieldName).append(" = ").append(fieldName).append(";\n\t}\n\n");
		}
		
		for(String im : imports) {
			file_body.append("\nimport ").append(im).append(";");
		}
		file_body.append("\n");
		file_body.append(class_body);
		file_body.append("\n\t//------------------------Getters and Setters------------------------\\\\\n\n");
		file_body.append(getset_body);
		file_body.append("}");
		//System.out.println(file_body);
		writer.write(file_body.toString());
		writer.flush();
		writer.close();
	}
	
	
	public static void main(String[] args) throws Exception {
//		Util.transformName("admin_article_management", false, 0);
//		if(true) return;
		mksrcdirs();
		Connection conn = getConnection();
		List<Map<String, String>> ret = getTables(conn);
		if(!ret.isEmpty()) {
			for(Map<String, String> map : ret) {
				parseTable(map, conn);
			}
		}
		//String 
		
	}

}
