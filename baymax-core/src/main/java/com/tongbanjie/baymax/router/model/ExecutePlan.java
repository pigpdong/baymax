package com.tongbanjie.baymax.router.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExecutePlan {

    /**
     * 执行的类型
     */
	private ExecuteType executeType;

    /**
     * 目标sql
     */
	private List<TargetSql> sqlList;

    /**
     * 需要合并的字段
     */
    private Map<String, Integer> mergeColumns;
	
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

    public Map<String, Integer> getMergeColumns() {
        return mergeColumns;
    }

    public void setMergeColumns(Map<String, Integer> mergeColumns) {
        this.mergeColumns = mergeColumns;
    }

}
