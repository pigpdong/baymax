package com.tongbanjie.baymax.parser.druid.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParseResult {

	//private List<RouteCalculateUnit> routeCalculateUnits = new ArrayList<RouteCalculateUnit>();

    //private List/*or*/<List/*and*/<SQLBinaryOpExpr>> conditions;

    private List<CalculateUnit> calculateUnits;
	
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

    /**
     * 要查询的所有的列的别名,如果没有别名,别名＝field
     */
    private Map<String/*field*/, String/*alias*/> aliaColumns;

    /**
     * select中是否有*
     */
    private boolean hasAllColumnExpr;

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

    public List<CalculateUnit> getCalculateUnits() {
        return calculateUnits;
    }

    public void setCalculateUnits(List<CalculateUnit> calculateUnits) {
        this.calculateUnits = calculateUnits;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public Map<String, String> getAliaColumns() {
        return aliaColumns;
    }

    public void setAliaColumns(Map<String, String> aliaColumns) {
        this.aliaColumns = aliaColumns;
    }

    public boolean isHasAllColumnExpr() {
        return hasAllColumnExpr;
    }

    public void setHasAllColumnExpr(boolean hasAllColumnExpr) {
        this.hasAllColumnExpr = hasAllColumnExpr;
    }
}
