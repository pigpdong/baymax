package com.tongbanjie.baymax.parser;

import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.tongbanjie.baymax.jdbc.merge.MergeColumn;
import com.tongbanjie.baymax.parser.model.ParseResult;
import com.tongbanjie.baymax.parser.model.SqlType;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sidawei on 16/3/18.
 *
 * 聚合函数解析测试
 */
public class DruidSelectAggParserTest {

    public static String s1 = "select count(a),sum(a),avg(a),max(a),min(a) from table1 where a = 1";
    public static String s2 = "select count(a) as c1,sum(a) as c2,avg(a) as c3,max(a) as c4,min(a) as c5 from table1 where a = 1";
    public static String s3 = "select count(a) as c1,avg(a) as c2 from table1 where a = 1";


    DruidSelectParser parser = (DruidSelectParser) DruidParserFactory.getParser(SqlType.SELECT);

    /*--------------------------------------------------single------------------------------------------------------*/

    /**
     * 无别名测试
     */
    @Test
    public void parseAggregateTest(){

        ParseResult result = new ParseResult();
        ExecutePlan plan = new ExecutePlan();

        parser.init(s1, null);

        parser.parse(result);

        SQLSelectStatement stmt = (SQLSelectStatement) parser.statement;
        SQLSelectQuery query = stmt.getSelect().getQuery();

        parser.parseAggregate(result, plan, (MySqlSelectQueryBlock) query);

        System.out.println(plan.getMergeColumns());


        Map<String, MergeColumn.MergeType> mergeColumn = new HashMap<String, MergeColumn.MergeType>();
        mergeColumn.put("AVG2", MergeColumn.MergeType.MERGE_AVG);
        mergeColumn.put("SUM1", MergeColumn.MergeType.MERGE_SUM);
        mergeColumn.put("AVG2SUM", MergeColumn.MergeType.MERGE_SUM);
        mergeColumn.put("MIN4", MergeColumn.MergeType.MERGE_MIN);
        mergeColumn.put("MAX3", MergeColumn.MergeType.MERGE_MAX);
        mergeColumn.put("AVG2COUNT", MergeColumn.MergeType.MERGE_COUNT);
        mergeColumn.put("COUNT0", MergeColumn.MergeType.MERGE_COUNT);

        Assert.assertEquals(mergeColumn, plan.getMergeColumns());
    }

    /**
     * 有别名测试
     */
    @Test
    public void parseAggregateTest_s2(){
        ParseResult result = new ParseResult();
        ExecutePlan plan = new ExecutePlan();

        parser.init(s2, null);

        parser.parse(result);

        SQLSelectStatement stmt = (SQLSelectStatement) parser.statement;
        SQLSelectQuery query = stmt.getSelect().getQuery();

        parser.parseAggregate(result, plan, (MySqlSelectQueryBlock) query);

        System.out.println(plan.getMergeColumns());

        //"select count(a) as c1,sum(a) as c2,avg(a) as c3,max(a) as c4,min(a) as c5 from table1 where a = 1";

        Map<String, MergeColumn.MergeType> mergeColumn = new HashMap<String, MergeColumn.MergeType>();
        mergeColumn.put("c1", MergeColumn.MergeType.MERGE_COUNT);
        mergeColumn.put("c2", MergeColumn.MergeType.MERGE_SUM);
        mergeColumn.put("c3", MergeColumn.MergeType.MERGE_AVG);
        mergeColumn.put("c4", MergeColumn.MergeType.MERGE_MAX);
        mergeColumn.put("c5", MergeColumn.MergeType.MERGE_MIN);
        mergeColumn.put("c3SUM", MergeColumn.MergeType.MERGE_SUM);
        mergeColumn.put("c3COUNT", MergeColumn.MergeType.MERGE_COUNT);

        Assert.assertEquals(mergeColumn, plan.getMergeColumns());
    }

    // TODO select字段不包含
    @Test
    public void parseAggregateTest_s3(){

        System.out.println(Double.MAX_VALUE);

        ParseResult result = new ParseResult();
        ExecutePlan plan = new ExecutePlan();

        parser.init(s3, null);

        parser.parse(result);

        SQLSelectStatement stmt = (SQLSelectStatement) parser.statement;
        SQLSelectQuery query = stmt.getSelect().getQuery();

        parser.parseAggregate(result, plan, (MySqlSelectQueryBlock) query);

        System.out.println(plan.getMergeColumns());

        //"select count(a) as c1,avg(a) as c2 from table1 where a = 1";

        Map<String, MergeColumn.MergeType> mergeColumn = new HashMap<String, MergeColumn.MergeType>();
        mergeColumn.put("c1", MergeColumn.MergeType.MERGE_COUNT);
        mergeColumn.put("c2", MergeColumn.MergeType.MERGE_SUM);
        mergeColumn.put("c3", MergeColumn.MergeType.MERGE_AVG);
        mergeColumn.put("c4", MergeColumn.MergeType.MERGE_MAX);
        mergeColumn.put("c5", MergeColumn.MergeType.MERGE_MIN);
        mergeColumn.put("c3SUM", MergeColumn.MergeType.MERGE_SUM);
        mergeColumn.put("c3COUNT", MergeColumn.MergeType.MERGE_COUNT);

        Assert.assertEquals(mergeColumn, plan.getMergeColumns());
    }

}
