package com.tongbanjie.baymax.router.model;

import javax.sql.DataSource;

/**
 * 路由后用于执行的SQL
 * 
 * @author dawei
 *
 */
public class TargetSql {
	
	private DataSource dataSource;
	
	private String partition;
	
	private String logicTableName;
	
	private String targetTableName;
	
	private String originalSql;
	
	private String targetSql;
	
	private SqlType sqlType;
	
	/**
	 * 只有{@link sqlReWrite==true} 控制Statement拦截器是否使用targetSQL。
	 * 只有{@link sqlReWrite==true} 控制statement是否使用{@link reWriteParameter}
	 */
	private boolean sqlReWrite;
	
	/**
	 * 是有{@link sqlReWrite==true} && {@link reWriteParameter!=null}才会替换parameter
	 */
	private Object reWriteParameter;
	
	@Override
	public String toString() {
		return new StringBuffer()
		.append(String.format("[partition:%s]\n[logicTableName:%s]\n[targetTableName:%s]\n[originalSql:%s]\n[targetSql:%s]\n",
				new Object[]{partition, logicTableName, targetTableName, originalSql, targetSql})).toString();
				
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getLogicTableName() {
		return logicTableName;
	}

	public void setLogicTableName(String logicTableName) {
		this.logicTableName = logicTableName;
	}

	public String getTargetTableName() {
		return targetTableName;
	}

	public void setTargetTableName(String targetTableName) {
		this.targetTableName = targetTableName;
	}

	public String getOriginalSql() {
		return originalSql;
	}

	public void setOriginalSql(String originalSql) {
		this.originalSql = originalSql;
	}

	public String getTargetSql() {
		return targetSql;
	}

	public void setTargetSql(String targetSql) {
		this.targetSql = targetSql;
	}

	public String getPartition() {
		return partition;
	}

	public void setPartition(String partition) {
		this.partition = partition;
	}

	public boolean isSqlReWrite() {
		return sqlReWrite;
	}

	public void setSqlReWrite(boolean sqlReWrite) {
		this.sqlReWrite = sqlReWrite;
	}

	public Object getReWriteParameter() {
		return reWriteParameter;
	}

	public void setReWriteParameter(Object reWriteParameter) {
		this.reWriteParameter = reWriteParameter;
	}

	public SqlType getSqlType() {
		return sqlType;
	}

	public void setSqlType(SqlType sqlType) {
		this.sqlType = sqlType;
	}

}
