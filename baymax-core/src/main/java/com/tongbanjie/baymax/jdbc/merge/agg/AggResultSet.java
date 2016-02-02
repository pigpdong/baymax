package com.tongbanjie.baymax.jdbc.merge.agg;

import com.tongbanjie.baymax.jdbc.TMerger;
import com.tongbanjie.baymax.jdbc.TStatement;
import com.tongbanjie.baymax.jdbc.merge.DataConvert;
import com.tongbanjie.baymax.jdbc.merge.MergeColumn;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sidawei on 16/1/31.
 */
public class AggResultSet extends AggResultSetGetterAdapter {

    Map<String/*columnName*/, MergeColumn.MergeType> mergeColumns;

    Map<Integer/*columnIndex*/, String/*columnName*/> mergeColumnsIndex;

    public AggResultSet(List<ResultSet> listResultSet, TStatement statement, ExecutePlan plan) {
        super(listResultSet, statement);
        mergeColumns = plan.getMergeColumns();
        // TODO index转化为alias
    }

    /**
     * 先考虑没有groupby 只有一行的时候
     * @return
     * @throws SQLException
     */
    @Override
    public boolean next() throws SQLException {
        List<ResultSet> sets = getResultSet();
        if (sets == null) {
            return false;
        }
        boolean hasNext = false;
        Iterator<ResultSet> ite = sets.iterator();
        while (ite.hasNext()){
            ResultSet set = ite.next();
            if (!set.next()){
                ite.remove();
            }else {
                hasNext = true;
            }
        }
        return hasNext;
    }

    @Override
    public boolean needEscape() {
        return false;
    }

    /*------------------*/

    @Override
    public boolean isAggColumn(String name){
        return mergeColumns.containsKey(name);
    }

    @Override
    public boolean isAggColumn(int index){
        return mergeColumnsIndex.containsKey(index);
    }

    /**
     * @param columnLabel
     * @param type
     * @param <T>
     * @return
     * @throws SQLException
     */
    @Override
    public <T> T merge(String columnLabel, Class<T> type) throws SQLException {
        Object value = null;
        MergeColumn.MergeType mergeType = mergeColumns.get(columnLabel);
        switch (mergeType) {
            case MERGE_COUNT:
            case MERGE_SUM:
                value = TMerger.mergeCount(getResultSet(), columnLabel);
                break;
            case MERGE_MIN:
                value = TMerger.mergeMin(getResultSet(), columnLabel);
                break;
            case MERGE_MAX:
                value = TMerger.mergeMax(getResultSet(), columnLabel);
                break;
            case MERGE_AVG:
                value = TMerger.mergeAvg(getResultSet(), columnLabel+"SUM", columnLabel+"COUNT");
                break;
        }
        if (value == null){
            return null;
        }
        return (T) DataConvert.convertValue(value, type);
    }

    @Override
    public <T> T merge(int columnIndex, Class<T> type) throws SQLException {
        return merge(mergeColumnsIndex.get(columnIndex), type);
    }

}
