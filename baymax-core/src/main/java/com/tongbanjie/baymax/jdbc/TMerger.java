package com.tongbanjie.baymax.jdbc;

import com.tongbanjie.baymax.jdbc.merge.MergeColumn;
import com.tongbanjie.baymax.jdbc.merge.iterator.IteratorResutSet;
import com.tongbanjie.baymax.jdbc.merge.agg.AggResultSet;
import com.tongbanjie.baymax.jdbc.merge.orderby.OrderByResultSet;
import com.tongbanjie.baymax.router.model.ExecutePlan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by sidawei on 16/1/29.
 */
public class TMerger {

    public static TResultSet mearge(ExecutePlan plan, List<ResultSet> sets, TStatement outStmt) throws SQLException {

        if (sets == null || sets.size() <= 1){
            return new IteratorResutSet(sets, outStmt);
        }

        // agg
        Map<String, MergeColumn.MergeType> mergeColumns = plan.getMergeColumns();
        if (mergeColumns != null && mergeColumns.size() > 0){
            return new AggResultSet(sets, outStmt, plan);
        }

        // orderby
        if (plan.getOrderbyColumns() != null && plan.getOrderbyColumns().size() > 0){
            return new OrderByResultSet(sets, outStmt, plan);
        }

        // agg + groupby
        // agg + groupby + orderby
        // groupby + orderby

        // 普通ResultSet
        return new IteratorResutSet(sets, outStmt);
    }

    /**
     * 合并Count
     * @param sets
     * @param alias
     * @return
     * @throws SQLException
     */
    public static Long mergeCount(List<ResultSet> sets, String alias) throws SQLException {
        Long number = null;
        for (int i = 0; i < sets.size(); i++){
            Object o = sets.get(i).getLong(alias);
            Long value = sets.get(i).getLong(alias);
            if (number == null){
                number = value;
            }else if (value != null){
                number += value;
            }
        }
        return number;
    }

    /**
     * 合并Sum
     * @param sets
     * @param alias
     * @return
     * @throws SQLException
     */
    public static Double mergeSum(List<ResultSet> sets, String alias) throws SQLException {
        Double number = null;
        for (int i = 0; i < sets.size(); i++){
            Double value = sets.get(i).getDouble(alias);
            if (number == null){
                number = value;
            }else if (value != null){
                number += value;
            }
        }
        return number;
    }

    /**
     * 合并Min
     * @param sets
     * @param alias
     * @return
     * @throws SQLException
     */
    public static Object mergeMin(List<ResultSet> sets, String alias) throws SQLException {
        Double number = null;
        for (int i = 0; i < sets.size(); i++){
            Double value = sets.get(i).getDouble(alias);
            if (number == null){
                number = value;
            }else if (value != null && value < number){
                number  = value;
            }
        }
        return number;
    }

    /**
     * 合并Max
     * @param sets
     * @param alias
     * @return
     * @throws SQLException
     */
    public static Object mergeMax(List<ResultSet> sets, String alias) throws SQLException {
        Double number = null;
        for (int i = 0; i < sets.size(); i++){
            Double value = sets.get(i).getDouble(alias);
            if (number == null){
                number = value;
            }else if (value != null && value > number){
                number  = value;
            }
        }
        return number;
    }

    /**
     * 合并Avg
     * @param sets
     * @param aliasSum
     * @param aliasCount
     * @return
     * @throws SQLException
     */
    public static Object mergeAvg(List<ResultSet> sets, String aliasSum, String aliasCount) throws SQLException {

        Double sum = mergeSum(sets, aliasCount);
        Long count = mergeCount(sets, aliasSum);

        if (sum != null && count != null){
            return sum / count;
        }
        return null;
    }
}
