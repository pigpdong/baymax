package com.tongbanjie.baymax.parser;

/**
 * SQL解析后的原数据
 * 
 * @author dawei
 *
 */
public class SqlParseDate {
	
	private String tableName;
	
	private String sqlType;
	
	private String key;
	
	/**
	 * Double,String,Date
	 */
	private Object value;
	
	private String originalValue;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getOriginalValue() {
		return originalValue;
	}

	public void setOriginalValue(String originalValue) {
		this.originalValue = originalValue;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getSqlType() {
		return sqlType;
	}

	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}
}
