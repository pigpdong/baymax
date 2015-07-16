package com.tongbanjie.baymax.router.model;

/**
 * SQL解析后的Statement参数
 * 
 * @author dawei
 *
 */
public class SqlArgEntity {
	
	/**
	 * sql中提取出的参数名称
	 */
	private String key;
	
	/**
	 * Double,String,Date
	 */
	private Object value;
	
	/**
	 * sql中提取出的原始参数value
	 */
	private String originalValue;
	
	/**
	 * 比较类型
	 */
	private CompareType compareType;
	
	public static enum CompareType{
		
		/**
		 * 等于
		 */
		eq,
		
		/**
		 * 大于
		 */
		moreThen,
		
		/**
		 * 大于等于
		 */
		moreThen_eq,
		
		/**
		 * 小于
		 */
		lessThen,
		
		/**
		 * 小于等于
		 */
		lessThen_eq,
		
		other;
		
		public static CompareType getByName(String c){
			if("=".equals(c)){
				return CompareType.eq;
			}else if(">".equals(c)){
				return CompareType.moreThen;
			}else if(">=".equals(c)){
				return CompareType.moreThen_eq;
			}else if("<".equals(c)){
				return CompareType.lessThen;
			}else if("<=".equals(c)){
				return CompareType.lessThen_eq;
			}else{
				return CompareType.other;
			}
		}
	}

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

	public CompareType getCompareType() {
		return compareType;
	}

	public void setCompareType(CompareType compareType) {
		this.compareType = compareType;
	}
}
