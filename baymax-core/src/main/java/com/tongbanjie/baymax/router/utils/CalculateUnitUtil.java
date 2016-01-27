package com.tongbanjie.baymax.router.utils;

import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.tongbanjie.baymax.router.model.CalculateUnit;
import com.tongbanjie.baymax.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sidawei on 16/1/25.
 */
public class CalculateUnitUtil {

    public static List<CalculateUnit> buildCalculateUnits(SchemaStatVisitor visitor, List<List<TableStat.Condition>> conditionList) {
        List<CalculateUnit> retList = new ArrayList<CalculateUnit>();
        //遍历condition ，找分片字段
        for(int i = 0; i < conditionList.size(); i++) {
            CalculateUnit calculateUnit = new CalculateUnit();
            for(TableStat.Condition condition : conditionList.get(i)) {
                List<Object> values = condition.getValues();
                if(values.size() == 0) {
                    break;
                }
                if(checkConditionValues(values)) {
                    String columnName = StringUtil.removeBackquote(condition.getColumn().getName().toUpperCase());
                    String tableName = StringUtil.removeBackquote(condition.getColumn().getTable().toUpperCase());

                    if(visitor.getAliasMap() != null && visitor.getAliasMap().get(tableName) != null
                            && !visitor.getAliasMap().get(tableName).equals(tableName)) {
                        tableName = visitor.getAliasMap().get(tableName);
                    }

                    if(visitor.getAliasMap() != null && visitor.getAliasMap().get(condition.getColumn().getTable().toUpperCase()) == null) {//子查询的别名条件忽略掉,不参数路由计算，否则后面找不到表
                        continue;
                    }

                    String operator = condition.getOperator();

                    //只处理between ,in和=3中操作符
//                    if(operator.equals("between")) {
//                        RangeValue rv = new RangeValue(values.get(0), values.get(1), RangeValue.EE);
//                        CalculateUnit.addShardingExpr(tableName.toUpperCase(), columnName, rv);
//                    } else
                    if(operator.equals("=") || operator.toLowerCase().equals("in")){
                        //只处理=号和in操作符,其他忽略
                        calculateUnit.addCondition(tableName.toUpperCase(), columnName, values.toArray());
                    }
                }
            }
            retList.add(calculateUnit);
        }
        return retList;
    }

    private static boolean checkConditionValues(List<Object> values) {
        for(Object value : values) {
            if(value != null && !value.toString().equals("")) {
                return true;
            }
        }
        return false;
    }

}
