package com.tongbanjie.baymax.jdbc.merge.groupby;

import com.tongbanjie.baymax.jdbc.merge.MergeColumn;
import com.tongbanjie.baymax.jdbc.merge.MergeMath;

import java.math.BigDecimal;

/**
 * Created by sidawei on 16/2/2.
 */
public class GroupbyAggMerger extends MergeMath{

    public static Object mergeAvg(BigDecimal sum1, BigDecimal count1, BigDecimal sum2, BigDecimal count2, MergeColumn.MergeType mergeType){
        return sum1.add(sum2).divide(count1.add(count2));
    }

    public static Object merge(BigDecimal o1, BigDecimal o2, MergeColumn.MergeType mergeType){
        if (o1 == null && o2 == null){
            return null;
        }
        if (o1 == null){
            return o2;
        }
        if (o2 == null){
            return o1;
        }

        Object value = null;
        switch (mergeType) {
            case MERGE_COUNT:
            case MERGE_SUM:
                value = mergeSum(o1, o2);
                break;
            case MERGE_MIN:
                value = mergeMin(o1, o2);
                break;
            case MERGE_MAX:
                value = mergeMax(o1, o2);
                break;
        }
        return value;
    }

    public static Object mergeSum(BigDecimal o1, BigDecimal o2) {
        return o1.add(o2);
    }

    public static Object mergeMin(BigDecimal o1, BigDecimal o2) {
        return o1.compareTo(o2) < 0 ? o1 : o2;
    }

    public static Object mergeMax(BigDecimal o1, BigDecimal o2) {
        return o1.compareTo(o2) > 0 ? o1 : o2;
    }

}
