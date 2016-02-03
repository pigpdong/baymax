package com.tongbanjie.baymax.jdbc.merge.groupby;

import com.tongbanjie.baymax.exception.BayMaxException;
import com.tongbanjie.baymax.jdbc.TStatement;
import com.tongbanjie.baymax.jdbc.merge.MergeColumn;
import com.tongbanjie.baymax.jdbc.merge.OrderbyColumn;
import com.tongbanjie.baymax.jdbc.merge.iterator.IteratorResultSetGetterAdapter;
import com.tongbanjie.baymax.jdbc.merge.orderby.OrderByComparetor;
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
public class GroupByResultSet extends GroupbyResultSetGetterAdapter {

    /*----------------------------------------------合并参数--------------------------------------------------*/

    private List<OrderbyColumn>                     orderbyColumns;
    private List<String>                            groupbyColumns;
    private Map<GroupbyKey, GroupbyValue>           mergedValus;
    private GroupbyMetaData                         metaData;
    private Map<String, MergeColumn.MergeType>      aggColumns;

    /*----------------------------------------------合并结果--------------------------------------------------*/

    /**
     * 合并后的每一行数据,下标从0开始
     */
    private GroupbyValue[]                          groupbyValues;
    /**
     * 当前调用next()指向了哪一行数据
     */
    private int                                     groupbyValuesIndex = -1;

    /*-------------------------------------------------------------------------------------------------------*/

    public GroupByResultSet(List<ResultSet> listResultSet, TStatement statement, ExecutePlan plan) throws SQLException {
        super(listResultSet, statement);
        orderbyColumns  = plan.getOrderbyColumns();
        groupbyColumns  = plan.getGroupbyColumns();
        aggColumns      = plan.getMergeColumns();
        plan.getMergeColumns();
        mergedValus     = new HashMap<GroupbyKey, GroupbyValue>();
        metaData        = new GroupbyMetaData(super.currentResultSet.getMetaData());
        // 合并结果集
        merge();
        // 结果集排序
        sort();
    }

    private void merge() throws SQLException {
        // 合并数据
        for (ResultSet set : getResultSet()){
            while (set.next()){
                GroupbyKey key = new GroupbyKey(set, groupbyColumns);
                if (mergedValus.containsKey(key)){
                    // merge
                    mergedValus.put(key, new GroupbyValue(set,metaData, aggColumns, mergedValus.get(key)));
                }else {
                    // add
                    mergedValus.put(key, new GroupbyValue(set, metaData, aggColumns, null));
                }
            }
        }

        Collection<GroupbyValue> groupbyVs = mergedValus.values();
        groupbyValues = new GroupbyValue[groupbyVs.size()];
        groupbyVs.toArray(groupbyValues);
    }

    private void sort(){
        if (orderbyColumns == null || orderbyColumns.size() == 0){
            return;
        }
        if (groupbyValues == null || groupbyValues.length == 0){
            return;
        }
        Arrays.sort(groupbyValues, new Comparator<GroupbyValue>() {
            OrderByComparetor comparetor = new OrderByComparetor(orderbyColumns);
            @Override
            public int compare(final GroupbyValue v1, final GroupbyValue v2) {
                try {
                    return comparetor.compare(new OrderByComparetor.CompareEntity() {
                        @Override
                        public Object getValue(String columnName) throws SQLException {
                            return v1.getValue(columnName, Object.class);
                        }
                    }, new OrderByComparetor.CompareEntity() {
                        @Override
                        public Object getValue(String columnName) throws SQLException {
                            return v2.getValue(columnName, Object.class);
                        }
                    });
                } catch (SQLException e) {
                    throw new BayMaxException("Group by ...Order by... 排序失败" , e);
                }
            }
        });
    }

    /**
     * 必须有order by 没有order by的 在解析的时候用 groupby的字段 作为orderby 重写sql
     * @return
     * @throws SQLException
     */
    @Override
    public boolean next() throws SQLException {
        if (groupbyValues == null || groupbyValues.length == 0){
            return false;
        }
        groupbyValuesIndex ++;
        if (groupbyValuesIndex >= groupbyValues.length){
            return false;
        }
        super.currentValue = groupbyValues[groupbyValuesIndex];
        return true;
    }

    @Override
    public boolean needEscape() {
        return false;
    }
}
