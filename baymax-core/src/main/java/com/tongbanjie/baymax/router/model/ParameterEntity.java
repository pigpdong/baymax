package com.tongbanjie.baymax.router.model;

/**
 * SQL解析后的Statement参数
 * 
 * @author dawei
 *
 */
public class ParameterEntity {
	
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
}
