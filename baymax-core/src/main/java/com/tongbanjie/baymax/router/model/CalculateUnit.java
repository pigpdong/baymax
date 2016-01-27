package com.tongbanjie.baymax.router.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by sidawei on 16/1/26.
 */
public class CalculateUnit {

    private Map<String/*table*/, Map<String/*column*/, Set<String>/*value*/>> tablesAndConditions = new LinkedHashMap<String, Map<String, Set<String>>>();

    /**
     *
     * @return
     */
    public Map<String, Map<String, Set<String>>> getTablesAndConditions() {
        return tablesAndConditions;
    }

    /**
     * 表名，列名 相同的要合并
     * @param tableName
     * @param columnName
     * @param value
     */
    public void addCondition(String tableName, String columnName, Object value) {
        Map<String, Set<String>> tableColumnsMap = tablesAndConditions.get(tableName);

        if (value == null) {
            // where a=null
            return;
        }

        if (tableColumnsMap == null) {
            tableColumnsMap = new LinkedHashMap<String, Set<String>>();
            tablesAndConditions.put(tableName, tableColumnsMap);
        }

        String uperColName = columnName.toUpperCase();
        Set<String> columValues = tableColumnsMap.get(uperColName);

        if (columValues == null) {
            columValues = new LinkedHashSet<String>();
            tablesAndConditions.get(tableName).put(uperColName, columValues);
        }

        if (value instanceof Object[]) {
            for (Object item : (Object[]) value) {
                if(item == null) {
                    continue;
                }
                columValues.add(item.toString());
            }
        } else {
            columValues.add(value.toString());
        }
    }
}
