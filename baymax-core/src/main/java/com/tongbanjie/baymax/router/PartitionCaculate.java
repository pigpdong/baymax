package com.tongbanjie.baymax.router;

import com.tongbanjie.baymax.parser.druid.model.ParseResult;
import com.tongbanjie.baymax.router.model.CalculateUnit;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import com.tongbanjie.baymax.router.model.TargetSql;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sidawei on 16/1/15.
 */
public class PartitionCaculate {
    public static ExecutePlan caculate(ParseResult result) {
        List<TargetSql> nodeSet = new LinkedList<TargetSql>();
//        for (RouteCalculateUnit unit : result.getRouteCalculateUnits()) {
//            List<TargetSql> rrsTmp = tryRouteForTables(unit);
//            if (rrsTmp != null) {
//                nodeSet.addAll(rrsTmp);
//            }
//        }
        ExecutePlan plan = new ExecutePlan();
        plan.setSqlList(nodeSet);
        return plan;
    }

    private static List<TargetSql> tryRouteForTables(CalculateUnit unit) {

        return null;
    }
}
