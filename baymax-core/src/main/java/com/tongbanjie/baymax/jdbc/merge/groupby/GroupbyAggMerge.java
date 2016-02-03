package com.tongbanjie.baymax.jdbc.merge.groupby;

import com.tongbanjie.baymax.jdbc.merge.MergeColumn;

/**
 * Created by sidawei on 16/2/2.
 */
public class GroupbyAggMerge {

    public static Object merge(Object o1, Object o2, MergeColumn.MergeType mergeType){
        Object value = null;
        switch (mergeType) {
            case MERGE_COUNT:
            case MERGE_SUM:
                value = GroupbyAggMerge.mergeCount(o1, o1);
                break;
            case MERGE_MIN:
                value = GroupbyAggMerge.mergeMin(o1, o2);
                break;
            case MERGE_MAX:
                value = GroupbyAggMerge.mergeMax(o1, o2);
                break;
            case MERGE_AVG:
                //value = GroupbyAggMerge.mergeAvg(getResultSet(), columnLabel+"SUM", columnLabel+"COUNT");
                break;
        }
        return null;
    }

    public static Object mergeCount(Object o1, Object o2) {
        return null;
    }

    public static Object mergeMin(Object valu, Object o) {
        return null;
    }

    public static Object mergeMax(Object valu, Object o) {
        return null;
    }
}
