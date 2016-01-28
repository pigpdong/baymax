package com.tongbanjie.baymax.router.model;

import java.util.ArrayList;
import java.util.List;

public class ExecutePlan {
	
	private ExecuteType executeType;
	
	private List<TargetSql> sqlList;
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer()
		.append(String.format("[ExecuteType:%s]\n", executeType.getValue()));
		for(TargetSql sql : sqlList){
			sb.append("######");
			sb.append(sql.toString());
		}
		sb.append("######");
		return sb.toString();
	}
	
	public void addSql(TargetSql sql){
		if(sqlList == null){
			sqlList = new ArrayList<TargetSql>();
		}
		sqlList.add(sql);
	}

	public List<TargetSql> getSqlList() {
		return sqlList;
	}

	public void setSqlList(List<TargetSql> sqlList) {
		this.sqlList = sqlList;
	}

	public ExecuteType getExecuteType() {
		return executeType;
	}

	public void setExecuteType(ExecuteType executeType) {
		this.executeType = executeType;
	}
}
