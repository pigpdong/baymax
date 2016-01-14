package com.tongbanjie.baymax.parser.druid.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 路由计算单元
 * 
 * 一个路由单元代表的是 多个and的组合
 * 
 * @author wang.dw
 * @date 2015-3-14 下午6:24:54
 * @version 0.1.0 
 * @copyright wonhigh.cn
 */
public class RouteCalculateUnit {
	private Map<String/*table*/, Map<String/*column*/, Set<ColumnRoutePair>/*value*/>> tablesAndConditions = new LinkedHashMap<String, Map<String, Set<ColumnRoutePair>>>();

	public Map<String, Map<String, Set<ColumnRoutePair>>> getTablesAndConditions() {
		return tablesAndConditions;
	}

	/**
	 * 表名，列名 相同的要合并
	 * @param tableName
	 * @param columnName
	 * @param value
	 */
	public void addShardingExpr(String tableName, String columnName, Object value) {
		Map<String, Set<ColumnRoutePair>> tableColumnsMap = tablesAndConditions.get(tableName);
		
		if (value == null) {
			// where a=null
			return;
		}
		
		if (tableColumnsMap == null) {
			tableColumnsMap = new LinkedHashMap<String, Set<ColumnRoutePair>>();
			tablesAndConditions.put(tableName, tableColumnsMap);
		}
		
		String uperColName = columnName.toUpperCase();
		Set<ColumnRoutePair> columValues = tableColumnsMap.get(uperColName);

		if (columValues == null) {
			columValues = new LinkedHashSet<ColumnRoutePair>();
			tablesAndConditions.get(tableName).put(uperColName, columValues);
		}

		if (value instanceof Object[]) {
			for (Object item : (Object[]) value) {
				if(item == null) {
					continue;
				}
				columValues.add(new ColumnRoutePair(item.toString()));
			}
		} else if (value instanceof RangeValue) {
			columValues.add(new ColumnRoutePair((RangeValue) value));
		} else {
			columValues.add(new ColumnRoutePair(value.toString()));
		}
	}
	
	public void clear() {
		tablesAndConditions.clear();
	}
	
	
}
