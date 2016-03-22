package com.tongbanjie.baymax.test;

import com.tongbanjie.baymax.parser.model.CalculateUnit;
import com.tongbanjie.baymax.parser.model.ConditionUnit;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sidawei on 16/3/21.
 */
public class PrintUtil {

    public static void printCalculates(List<CalculateUnit> calculateUnits){
        for (CalculateUnit unit : calculateUnits){
            printCalculate(unit);
        }
    }

    public static void printCalculate(CalculateUnit unit){
        Map<String/*table*/, Set<ConditionUnit/*column value*/>> tablesAndConditions = unit.getTablesAndConditions();
        for (Iterator ite = tablesAndConditions.entrySet().iterator(); ite.hasNext();){
            Map.Entry<String/*table*/, Set<ConditionUnit/*column value*/>> entry = (Map.Entry<String, Set<ConditionUnit>>) ite.next();
            StringBuffer sb = new StringBuffer(entry.getKey()).append("==>");
            Set<ConditionUnit> conditionUnits = entry.getValue();
            if (conditionUnits != null){
                for (ConditionUnit conditionUnit : conditionUnits){
                    sb.append(conditionUnit.getColumn()).append("=");
                    sb.append(conditionUnit.getValues()).append("|");
                }
            }
            System.out.println(sb);
        }
    }

}
