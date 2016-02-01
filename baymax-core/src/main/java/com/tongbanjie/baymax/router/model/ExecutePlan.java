package com.tongbanjie.baymax.router.model;

import com.tongbanjie.baymax.jdbc.merge.MergeColumn;
import com.tongbanjie.baymax.jdbc.merge.OrderbyColumn;

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
     * 有聚合函数的字段
     */
    private Map<String/*columnName*/,   MergeColumn>            mergeColumnsName;
    private Map<Integer/*columnIndex*/, String/*columnName*/>   mergeColumnsIndex;

    /**
     * 分组
     */
    private List<String/*columnName*/>                          groupbyColumns;

    /**
     * 排序字段
     */
    private List<OrderbyColumn>                                 orderbyColumns;

    /*------------------------------------------------------------------------------------*/

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

    /*------------------------------------------------------------------------------------*/

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

    public Map<String, MergeColumn> getMergeColumnsName() {
        return mergeColumnsName;
    }

    public void setMergeColumnsName(Map<String, MergeColumn> mergeColumnsName) {
        this.mergeColumnsName = mergeColumnsName;
    }

    public Map<Integer, String> getMergeColumnsIndex() {
        return mergeColumnsIndex;
    }

    public void setMergeColumnsIndex(Map<Integer, String> mergeColumnsIndex) {
        this.mergeColumnsIndex = mergeColumnsIndex;
    }

    public List<OrderbyColumn> getOrderbyColumns() {
        return orderbyColumns;
    }

    public void setOrderbyColumns(List<OrderbyColumn> orderbyColumns) {
        this.orderbyColumns = orderbyColumns;
    }

    public List<String> getGroupbyColumns() {
        return groupbyColumns;
    }

    public void setGroupbyColumns(List<String> groupbyColumns) {
        this.groupbyColumns = groupbyColumns;
    }
}
