package com.tongbanjie.baymax.router.strategy;

import com.tongbanjie.baymax.exception.BayMaxException;
import com.tongbanjie.baymax.parser.model.CalculateUnit;
import com.tongbanjie.baymax.parser.model.ConditionUnit;
import com.tongbanjie.baymax.parser.model.ConditionUnitOperator;
import com.tongbanjie.baymax.utils.Pair;

import java.util.*;

/**
 * Created by sidawei on 16/3/20.
 */
public class PartitionTable extends PartitionTableMetaData{

    public List<Pair<String/* targetDB */, String/* targetTable */>> execute(CalculateUnit calculateUnit) {
        // and 相连的条件
        Set<ConditionUnit/*column value*/> conditionUnits = calculateUnit.getTablesAndConditions().get(getLogicTableName());

        if (conditionUnits == null || conditionUnits.size() == 0){
            return null;
        }

        // 检查conditon

        List<Pair<String, String>> targetList = new ArrayList<Pair<String, String>>(1);

        for (PartitionRule rule : rules){
            ConditionUnit matchingConditon = null;
            for(ConditionUnit condition : conditionUnits){
                if (rule.getColumn().equals(condition.getColumn()) && logicTableName.equals(condition.getTable())){
                    if (matchingConditon != null){
                        throw new BayMaxException("有多个相同列的Condition : " + condition);
                    }else {
                        matchingConditon = condition;
                    }
                }
            }
            if (matchingConditon != null){
                if (matchingConditon.getOperator() == ConditionUnitOperator.EQUAL){
                    // values
                    executeRule(targetList, rule, rule.getColumn(), matchingConditon.getValues().get(0));
                }else if (matchingConditon.getOperator() == ConditionUnitOperator.IN){
                    for (Object obj : matchingConditon.getValues()){
                        executeRule(targetList, rule, rule.getColumn(), obj);
                    }
                }
            }
            // 以第一个有结果的规则为准
            if (targetList.size() != 0){
                break;
            }
        }
        return targetList;
    }

    private void executeRule(List<Pair<String, String>> targetList, PartitionRule rule, String column, Object value){
        if (rule.getColumnProcess() != null){
            value = rule.getColumnProcess().apply(value);
        }
        Pair<String/* targetDB */, String/* targetTable */> target = executeRule(rule, column, value);
        if (target != null && target.getObject1() == null && target.getObject2() != null){
            throw new BayMaxException(target.getObject2() + "没有对应的库");
        }
        if (target != null && target.getObject1() != null && target.getObject2() != null){
            targetList.add(target);
        }
    }

    private Pair<String/* targetDB */, String/* targetTable */> executeRule(PartitionRule rule, String column, Object value) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put(column, value);

        String targetDB = null;
        String targetTable = null;

        Object ruleResult = rule.execute(param, null);
        if (Integer.class == ruleResult.getClass() || Integer.class.isAssignableFrom(ruleResult.getClass())) {
            // Integer
            String suffix = super.getSuffix((Integer) ruleResult);
            targetTable = super.format(suffix);
            targetDB = getTargetPartition(suffix);
        } else {
            throw new BayMaxException("is the express can return integer only!" + rule);
        }
        return new Pair<String, String>(targetDB, targetTable);
    }

    /**
     * 返回所有分区-表的映射 格式: p1-order001 p1-order002 p2-order003 p3-order004
     *
     * @return
     */
    public List<Pair<String/* targetDB */, String/* targetTable */>> getAllTableNames() {
        List<Pair<String/* targetDB */, String/* targetTable */>> allTables = new ArrayList<Pair<String, String>>();
        Iterator<Map.Entry<String, String>> ite = super.tableMapping.entrySet().iterator();
        while(ite.hasNext()){
            Map.Entry<String, String> entry = ite.next();
            allTables.add(new Pair<String, String>(entry.getValue(), super.format(entry.getKey())));
        }
        return allTables;
    }

}
