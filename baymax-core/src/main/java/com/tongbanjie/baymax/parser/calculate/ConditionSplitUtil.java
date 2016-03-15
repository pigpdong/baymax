package com.tongbanjie.baymax.parser.calculate;

import com.alibaba.druid.stat.TableStat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sidawei on 16/3/15.
 */
public class ConditionSplitUtil {

    /**
     * 包含or语句
     * TODO 拆分为(x and x and x) or (x and x) or x的模式
     * mergedConditionList = visitor.splitConditions();
     * throw new RuntimeException("TODO 拆分为(x and x and x) or (x and x) or x的模式");
     *
     * @param conditions
     * @return
     */
    public static List<List<TableStat.Condition>> splitConditions(List<TableStat.Condition> conditions){
        List<List<TableStat.Condition>> conditionOr = new ArrayList<List<TableStat.Condition>>();
        return conditionOr;
    }

}
