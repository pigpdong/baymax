package com.tongbanjie.baymax.jdbc.merge.groupby;

import com.tongbanjie.baymax.jdbc.TStatement;
import com.tongbanjie.baymax.jdbc.merge.MergeColumn;
import com.tongbanjie.baymax.jdbc.merge.OrderbyColumn;
import com.tongbanjie.baymax.jdbc.merge.iterator.IteratorResultSetGetterAdapter;
import com.tongbanjie.baymax.router.model.ExecutePlan;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by sidawei on 16/1/31.
 *
 * groupby + orderby
 *
 * agg + groupby + orderby
 *
 * agg + groupby
 *
 * groupby
 *
 * orderby
 *
 * agg
 *
 * none
 *
 * 注意：暂时不考虑having中有聚合函数的请款,因为需要合并聚合函数的值,然后重新计算表达式
 *
 */
public class GroupByResultSet extends IteratorResultSetGetterAdapter {

    private List<OrderbyColumn>             orderbyColumns;
    private List<String>                    groupbyColumns;
    private Map<GroupbyKey, GroupbyValue>   mergedValus;
    private ResultSetMetaData               metaData;
    private  Map<String, MergeColumn.MergeType> aggColumns;

    public GroupByResultSet(List<ResultSet> listResultSet, TStatement statement, ExecutePlan plan) throws SQLException {
        super(listResultSet, statement);
        orderbyColumns = plan.getOrderbyColumns();
        groupbyColumns = plan.getGroupbyColumns();
        aggColumns = plan.getMergeColumns();
        plan.getMergeColumns();
        currentResultSet.getMetaData();
        mergedValus = new HashMap<GroupbyKey, GroupbyValue>();
        metaData = super.currentResultSet.getMetaData();
        // 构造合并的结果
        merge();
    }

    private void merge() throws SQLException {

        // TODO 先不考虑orderby

        // 初始化MetaData
        ResultSetMetaData metaData = super.currentResultSet.getMetaData();
        int size = metaData.getColumnCount();
        for (int i = 0; i< size; i++){
            String columnLabel = metaData.getColumnLabel(i);
        }

        // 合并数据
        for (ResultSet set : getResultSet()){
            while (set.next()){
                GroupbyKey key = new GroupbyKey(set);
                GroupbyValue value = new GroupbyValue(set);
                if (mergedValus.containsKey(key)){
                    // merge
                    mergedValus.put(key, merge(mergedValus.get(key), value));
                }else {
                    // add
                    mergedValus.put(key, value);
                }
            }
        }
    }

    /**
     * 合并同一个组的两行数据
     */
    private GroupbyValue merge(GroupbyValue v1, GroupbyValue v2) throws SQLException {
        // TODO 先不考虑avg
        // 如果是聚合函数需要合并
        Object[] valus1 = v1.getValus();
        Object[] valus2 = v2.getValus();
        for (int i = 0; i < valus1.length; i++){

        }
        return null;
    }

    /**
     * 必须有order by 没有order by的 在解析的时候用 groupby的字段 作为orderby 重写sql
     * @return
     * @throws SQLException
     */
    @Override
    public boolean next() throws SQLException {
        List<ResultSet> sets = getResultSet();
        if (sets == null) {
            return false;
        }
        return true;
    }

    /**
     * 代表一个分组
     */
    private class GroupbyKey{

        List<Object> keys;

        public GroupbyKey(ResultSet set) throws SQLException {
            keys = new ArrayList<Object>(groupbyColumns.size());
            for (String column : groupbyColumns){
                keys.add(set.getObject(column));
            }
        }

        /**
         * 用于查找同一个组
         * @param o
         * @return
         */
        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof GroupbyKey)){
                return false;
            }
            List<Object> outKey = (List)o;
            for (Object item : keys){
                for (Object outItem : outKey){
                    if (!item.equals(outItem)){
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hashcode = 0;
            for (Object item : keys){
                hashcode += item.hashCode();
            }
            return hashcode;
        }
    }

    /**
     * 代表一行数据
     */
    private class GroupbyValue{

        /**
         * 一行中 每一列的值
         */
        private Object[] valus;

        public GroupbyValue(ResultSet set) throws SQLException {
            this(set, null);
        }

        /**
         *
         * @param set
         * @param merge     同组的上一行
         * @throws SQLException
         */
        public GroupbyValue(ResultSet set, GroupbyValue merge) throws SQLException {
            int size = metaData.getColumnCount();
            valus = new Object[size];
            for (int i = 0; i< size; i++){
                String columnLabel = metaData.getColumnLabel(i);
                valus[i] = set.getObject(columnLabel);
                if (merge != null){
                    // 合并
                    if (aggColumns.containsKey(columnLabel)){

                    }
                }
            }
        }

        public Object[] getValus() {
            return valus;
        }

        public void setValus(Object[] valus) {
            this.valus = valus;
        }
    }

    /**
     * 表示一个分组
     */
    private class GroupbyUnit implements Comparable<GroupbyUnit>{

        private ResultSet set;

        public GroupbyUnit(ResultSet set) {
            this.set = set;
        }

        public ResultSet getSet() {
            return set;
        }

        /**
         * 用于判断是否是同一个分组的数据,是同一个分组的,数据要合并
         * @param o
         * @return
         */
        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        /**
         * 用于根据orderby对分组排序
         * @param groupbyUnit
         * @return
         */
        @Override
        public int compareTo(GroupbyUnit groupbyUnit) {
            return 0;
        }
    }

    @Override
    public boolean needEscape() {
        return false;
    }
}
