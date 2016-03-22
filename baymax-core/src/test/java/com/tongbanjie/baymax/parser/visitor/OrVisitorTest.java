package com.tongbanjie.baymax.parser.visitor;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.stat.TableStat;
import com.tongbanjie.baymax.test.SelectTestSql;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sidawei on 16/3/16.
 *
 * or解析器
 */
public class OrVisitorTest implements SelectTestSql {

    @Test
    public void testGetOrConditions() throws Exception {

        MySqlStatementParser parser = new MySqlStatementParser(single1);

        SQLStatement statemen = parser.parseStatement();

        List<Object> parameters = new ArrayList<Object>();

        parameters.add(10);

        OrVisitor visitor = new OrVisitor();
        OrVisitor.OrEntity orEntity = new OrVisitor.OrEntity(visitor, statemen);
        List<List<TableStat.Condition>> conditions = orEntity.getOrConditions();
        System.out.println(conditions);
    }
}