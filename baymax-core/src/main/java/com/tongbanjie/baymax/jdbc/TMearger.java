package com.tongbanjie.baymax.jdbc;

import com.tongbanjie.baymax.jdbc.mearge.MergeCol;
import com.tongbanjie.baymax.router.model.ExecutePlan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by sidawei on 16/1/29.
 */
public class TMearger {

    public static TResultSet mearge(ExecutePlan plan, List<ResultSet> sets, TStatement outStmt) throws SQLException {

        TResultSet resultSet = new TResultSet(sets, outStmt);

        Map<String, Integer> mergeColumns = plan.getMergeColumns();
        if (plan.getSqlList().size() > 1 && sets != null && sets.size() > 1 && mergeColumns != null && mergeColumns.size() > 0){
            // 合并
            for (Map.Entry<String, Integer> entry : mergeColumns.entrySet()){
                String alias = entry.getKey();
                int mergeType = entry.getValue();
                switch (mergeType){
                    case MergeCol.MERGE_COUNT:
                        resultSet.addMeargeValue(alias, mergeCount(sets, alias));
                        break;
                    case MergeCol.MERGE_SUM:
                        resultSet.addMeargeValue(alias, mergeSum(sets, alias));
                        break;
                    case MergeCol.MERGE_MIN:
                        resultSet.addMeargeValue(alias, mergeMin(sets, alias));
                        break;
                    case MergeCol.MERGE_MAX:
                        resultSet.addMeargeValue(alias, mergeMax(sets, alias));
                        break;
                    case MergeCol.MERGE_AVG:
                        resultSet.addMeargeValue(alias, mergeAvg(sets, alias+"_sum", alias+"_count"));
                        break;
                }
            }
        }
        return resultSet;
    }

    /**
     * 合并Count
     * @param sets
     * @param alias
     * @return
     * @throws SQLException
     */
    private static Long mergeCount(List<ResultSet> sets, String alias) throws SQLException {
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
    private static Double mergeSum(List<ResultSet> sets, String alias) throws SQLException {
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
    private static Object mergeMin(List<ResultSet> sets, String alias) throws SQLException {
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
    private static Object mergeMax(List<ResultSet> sets, String alias) throws SQLException {
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
    private static Object mergeAvg(List<ResultSet> sets, String aliasSum, String aliasCount) throws SQLException {

        Double sum = mergeSum(sets, aliasCount);
        Long count = mergeCount(sets, aliasSum);

        if (sum != null && count != null){
            return sum / count;
        }
        return null;
    }
}
