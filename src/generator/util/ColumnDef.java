package generator.util;


/**
 * 
 * @author 
 * @version
 */
public class ColumnDef {
	
	private String columnName;
	private String columnType;
	private boolean isId = false;
	private boolean autoIncre = false;
	private boolean isSerialized = false;
	private String comment;
	
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String colunmName) {
		this.columnName = colunmName.toString().toLowerCase();
	}
	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String colunmName) {
		this.columnType = colunmName.toString();
	}
	public boolean isId() {
		return isId;
	}
	public void setId(String colunmKey) {
		colunmKey = null == colunmKey ? "" : colunmKey.toString();
		this.isId = colunmKey.equals("PRI");
	}
	public boolean isSerialized() {
		return isSerialized;
	}
	public void setSerialized(boolean isSerialized) {
		this.isSerialized = isSerialized;
	}
	public boolean isAutoIncre() {
		return autoIncre;
	}
	public void setAutoIncre(String extra) {
		extra = null == extra ? "" : extra.toString();
		this.autoIncre = extra.equals("auto_increment");
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		if(null != comment && comment.trim().length() > 0) {
			this.comment = comment;
		}
	}
}
