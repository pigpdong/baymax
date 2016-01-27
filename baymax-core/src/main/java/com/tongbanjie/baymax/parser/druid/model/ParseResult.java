package com.tongbanjie.baymax.parser.druid.model;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParseResult {

	//private List<RouteCalculateUnit> routeCalculateUnits = new ArrayList<RouteCalculateUnit>();

    private List/*or*/<List/*and*/<SQLBinaryOpExpr>> conditions;
	
	/**
	 * （共享属性）
	 */
	private String sql = "";
	
	//tables为路由计算共享属性，多组RouteCalculateUnit使用同样的tables
	private List<String> tables = new ArrayList<String>();
	
    //private RouteCalculateUnit routeCalculateUnit = new RouteCalculateUnit(this);

	/**
	 * key table alias, value talbe realname;
	 */
	private Map<String, String> tableAliasMap = new LinkedHashMap<String, String>();

    /*---------------------------------------------------------------------------------------*/

	public Map<String, String> getTableAliasMap() {
		return tableAliasMap;
	}

	public void setTableAliasMap(Map<String, String> tableAliasMap) {
		this.tableAliasMap = tableAliasMap;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public List<String> getTables() {
		return tables;
	}

	public void addTable(String tableName) {
		this.tables.add(tableName);
	}

    public List<List<SQLBinaryOpExpr>> getConditions() {
        return conditions;
    }

    public void setConditions(List<List<SQLBinaryOpExpr>> conditions) {
        this.conditions = conditions;
    }
}
