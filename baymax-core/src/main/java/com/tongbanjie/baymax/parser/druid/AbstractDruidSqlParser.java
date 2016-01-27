package com.tongbanjie.baymax.parser.druid;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.stat.TableStat.Condition;
import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.parser.druid.model.ParseResult;
import com.tongbanjie.baymax.parser.druid.visitor.MycatSchemaStatVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractDruidSqlParser implements IDruidSqlParser {

    protected SQLStatementParser        parser;
    protected MycatSchemaStatVisitor    visitor;
    protected SQLStatement              statement;

    @Override
    public void initParse(String sql, Map<Integer, ParameterCommand> parameterCommand) {
        parser 			= new MySqlStatementParser(sql);
        visitor 		= new MycatSchemaStatVisitor();
        statement 		= parser.parseStatement();
    }

    @Override
    public void parse(ParseResult result) {

        statement.accept(visitor);

        List<List<Condition>> mergedConditionList = new ArrayList<List<Condition>>();
        if (visitor.hasOrCondition()) {//包含or语句
            // TODO 拆分为(x and x and x) or (x and x) or x的模式
            // mergedConditionList = visitor.splitConditions();
            throw new RuntimeException("TODO 拆分为(x and x and x) or (x and x) or x的模式");
        } else {
            // 不包含OR语句
            mergedConditionList.add(visitor.getConditions());
        }

        alisMapFix(result);
        //result.setRouteCalculateUnits(RouteCalculateUnitUtil.buildRouteCalculateUnits(visitor, mergedConditionList));
    }

    /**
     * 表名统一格式
     * @param result
     */
    private void alisMapFix(ParseResult result){
        Map<String,String> tableAliasMap = new HashMap<String,String>();
        if (visitor.getAliasMap() != null) {
            for (Map.Entry<String, String> entry : visitor.getAliasMap().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null && key.indexOf("`") >= 0) {
                    key = key.replaceAll("`", "");
                }
                if (value != null && value.indexOf("`") >= 0) {
                    value = value.replaceAll("`", "");
                }
                //表名前面带database的，去掉
                if (key != null) {
                    int pos = key.indexOf(".");
                    if (pos > 0) {
                        key = key.substring(pos + 1);
                    }
                }

                if (key.equals(value)) {
                    result.addTable(key.toUpperCase());
                }
                tableAliasMap.put(key.toUpperCase(), value);
            }
            visitor.getAliasMap().putAll(tableAliasMap);
            result.setTableAliasMap(tableAliasMap);
        }
    }
}
