package com.tongbanjie.baymax.model;

import java.util.ArrayList;
import java.util.List;

public class RouteResult {
	
	private RouteResultType resultType;
	
	private List<Sql> sqlList;
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer().append("RouteResultType[" + resultType.toString() +"]");
		for(Sql sql : sqlList){
			sb.append(sql.toString());
		}
		return sb.toString();
	}
	
	public void addSql(Sql sql){
		if(sqlList == null){
			sqlList = new ArrayList<Sql>();
		}
		sqlList.add(sql);
	}

	public List<Sql> getSqlList() {
		return sqlList;
	}

	public void setSqlList(List<Sql> sqlList) {
		this.sqlList = sqlList;
	}

	public RouteResultType getResultType() {
		return resultType;
	}

	public void setResultType(RouteResultType resultType) {
		this.resultType = resultType;
	}


}
