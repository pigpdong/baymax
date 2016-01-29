package com.tongbanjie.baymax.parser.druid.model;

import com.tongbanjie.baymax.exception.BayMaxException;

import java.util.*;

/**
 * Created by sidawei on 16/1/26.
 *
 * 一个CalculateUnit表示的是一个计算单元
 * 一个计算单元表示的是经过等式转化后的and相连的一组条件
 * 多个计算单元(多组条件)之间用or相连
 * (A and B)or(C and D)
 */
public class CalculateUnit {

    /**
     * set中不会有相同的column 否则会报错
     * 即一个计算单元内的条件都是用and连接的,所以不可能有 a=1 and a=2
     */
    private Map<String/*table*/, Set<ConditionUnit/*column value*/>> tablesAndConditions = new LinkedHashMap<String, Set<ConditionUnit>>();

    /**
     * get
     * @return
     */
    public Map<String, Set<ConditionUnit>> getTablesAndConditions() {
        return tablesAndConditions;
    }

    /**
     * 表名，列名 相同的要合并
     * @param tableName
     * @param columnName
     * @param values
     */
    public void addCondition(String tableName, String columnName, Object[] values) {

        if (values == null || values.length == 0) {
            // where a=null
            return;
        }

        if (values.length > 1){
            throw new BayMaxException("同一个计算单元中出现了两次同一个字段:" + columnName + "," + values.toString());
        }

        // 同一个计算单元的所有条件
        Set<ConditionUnit> conditionUnits = tablesAndConditions.get(tableName);

        if (conditionUnits == null) {
            conditionUnits = new LinkedHashSet<ConditionUnit>();
            tablesAndConditions.put(tableName, conditionUnits);
        }

        // 判断是否已经有这个列作为条件
        String uperColName = columnName.toUpperCase();
        ConditionUnit unit = new ConditionUnit(uperColName, values[0].toString());

        if (conditionUnits.contains(unit)){
            throw new BayMaxException("同一个计算单元中出现了两次同一个字段:" + columnName + "," + values.toString());
        }

        conditionUnits.add(unit);
    }

    /**
     * 计算条件 代表 column = value
     */
    public static class ConditionUnit{
        String column;
        String value;

        public ConditionUnit(String column, String value){
            this.column = column;
            this.value = value;
        }

        @Override
        public int hashCode() {
            return column.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return column.equals(o);
        }

        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
