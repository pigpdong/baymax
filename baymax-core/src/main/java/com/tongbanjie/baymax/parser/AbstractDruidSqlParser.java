package com.tongbanjie.baymax.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.stat.TableStat.Condition;
import com.tongbanjie.baymax.parser.calculate.CalculateUnitUtil;
import com.tongbanjie.baymax.parser.model.ParseResult;
import com.tongbanjie.baymax.parser.visitor.ParserVisitor;
import com.tongbanjie.baymax.parser.visitor.ReplaceTableNameVisitor;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import com.tongbanjie.baymax.router.model.ExecuteType;
import com.tongbanjie.baymax.router.model.TargetSql;

import java.util.*;

public abstract class AbstractDruidSqlParser implements IDruidSqlParser {

    protected SQLStatementParser        parser;
    protected ParserVisitor visitor;
    protected SQLStatement              statement;
    protected String                    sql;
    protected List<Object>              parameters;

    @Override
    public void init(String sql, List<Object> parameters) {
        this.parser		    = new MySqlStatementParser(sql);
        this.visitor 		= new ParserVisitor(parameters);
        this.statement 		= parser.parseStatement();
        this.parameters     = parameters;
        this.sql            = sql;
    }

    /**
     * 默认通过visitor解析 之类可以覆盖
     *
     * 限制:分表的where中不能出现or,分表key只能出现一次且必须是a=1的类型
     * @param result
     */
    @Override
    public void parse(ParseResult result) {

        statement.accept(visitor);

        List<List<Condition>> mergedConditionList = new ArrayList<List<Condition>>();
        if (visitor.hasOrCondition()) {
            //包含or语句
            // TODO 拆分为(x and x and x) or (x and x) or x的模式
            // mergedConditionList = visitor.splitConditions();
            throw new RuntimeException("TODO 拆分为(x and x and x) or (x and x) or x的模式");
        } else {
            // 不包含OR语句
            mergedConditionList.add(visitor.getConditions());
        }

        alisMapFix(result);
        result.setSql(sql);
        result.setCalculateUnits(CalculateUnitUtil.buildCalculateUnits(result.getTableAliasMap(), mergedConditionList));
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
                    result.addTable(key.toLowerCase());
                }
                tableAliasMap.put(key.toLowerCase(), value);
            }
            visitor.getAliasMap().putAll(tableAliasMap);
            result.setTableAliasMap(tableAliasMap);
        }
    }

    @Override
    public void changeSql(ParseResult result, ExecutePlan plan) {
        if (ExecuteType.NO == plan.getExecuteType()){
            for (TargetSql sql : plan.getSqlList()){
                sql.setOriginalSql(result.getSql());
                sql.setTargetSql(result.getSql());
            }
        }else {
            for (TargetSql sql : plan.getSqlList()){
                ReplaceTableNameVisitor replaceVisitor = new ReplaceTableNameVisitor(sql.getLogicTableName(), sql.getTargetTableName());
                StringBuilder out = new StringBuilder();
                MySqlOutputVisitor outPutVisitor = new MySqlOutputVisitor(out);
                // 替换表名
                statement.accept(replaceVisitor);
                // 输出sql
                statement.accept(outPutVisitor);
                sql.setOriginalSql(result.getSql());
                sql.setTargetSql(out.toString());
                // 输出sql后要还原statement以便下次替换表名
                replaceVisitor.reset();
            }
        }
    }
}
