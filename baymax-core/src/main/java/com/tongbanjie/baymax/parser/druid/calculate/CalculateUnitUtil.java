package com.tongbanjie.baymax.parser.druid.calculate;

import com.alibaba.druid.stat.TableStat;
import com.tongbanjie.baymax.parser.druid.model.CalculateUnit;
import com.tongbanjie.baymax.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sidawei on 16/1/25.
 */
public class CalculateUnitUtil {

    /**
     *
     * @param tableAliasMap
     * @param conditionList 这里的conditionList应该是已经用or切分好的计算单元
     * @return
     */
    public static List<CalculateUnit> buildCalculateUnits(Map<String, String> tableAliasMap, List<List<TableStat.Condition>> conditionList) {
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
                    String columnName = StringUtil.removeBackquote(condition.getColumn().getName().toLowerCase());
                    String tableName = StringUtil.removeBackquote(condition.getColumn().getTable().toLowerCase());

                    if(tableAliasMap != null && tableAliasMap.get(tableName) != null
                            && !tableAliasMap.get(tableName).equals(tableName)) {
                        tableName = tableAliasMap.get(tableName);
                    }

                    if(tableAliasMap != null && tableAliasMap.get(condition.getColumn().getTable().toLowerCase()) == null) {//子查询的别名条件忽略掉,不参数路由计算，否则后面找不到表
                        continue;
                    }

                    String operator = condition.getOperator();

                    //只处理between ,in和=3中操作符
//                    if(operator.equals("between")) {
//                        RangeValue rv = new RangeValue(values.get(0), values.get(1), RangeValue.EE);
//                        CalculateUnit.addShardingExpr(tableName.toUpperCase(), columnName, rv);
//                    } else
                    //|| operator.toLowerCase().equals("in")

                    // between暂时不支持 需要枚举出between之间的值
                    // in暂时不支持 a in (1,2,3)要转化为a=1 or a=2 or a=3会导致计算单元的增加
                    if(operator.equals("=")){
                        //只处理=号和in操作符,其他忽略
                        calculateUnit.addCondition(tableName.toLowerCase(), columnName, values.toArray());
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
