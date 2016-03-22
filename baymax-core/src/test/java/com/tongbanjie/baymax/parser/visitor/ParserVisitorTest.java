package com.tongbanjie.baymax.parser.visitor;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sidawei on 16/3/15.
 */
public class ParserVisitorTest {

    @Test
    public void test_0() throws Exception {

        String sql = "SELECT id, name FROM table1 where id in (select id from users u where id = ? limit 10)";

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement statemen = statementList.get(0);

        Assert.assertEquals(1, statementList.size());

        List<Object> parameters = new ArrayList<Object>();
        parameters.add(10);

        //MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        ParserVisitor visitor = new ParserVisitor(parameters);
        statemen.accept(visitor);

        System.out.println(sql);
        System.out.println("Tables : " + visitor.getTables());
        System.out.println("fields : " + visitor.getColumns());
        System.out.println("alias : " + visitor.getAliasMap());
        System.out.println("conditions : " + visitor.getConditions());
        System.out.println("columns : " + visitor.getColumns());

        Assert.assertEquals(1, visitor.getTables().size());
        Assert.assertEquals(true, visitor.containsTable("users"));
        Assert.assertEquals(22, visitor.getConditions().get(0).getValues().get(0));

        Assert.assertEquals(2, visitor.getColumns().size());
        //Assert.assertEquals(true, visitor.getColumns().contains(new Column("users", "id")));
        //Assert.assertEquals(true, visitor.getColumns().contains(new Column("users", "name")));

    }

    @Test
    public void test_1() throws Exception {

        String sql = "select * from table1 where id in(1,2,3) or id = 4 or id = 5";

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement statemen = statementList.get(0);

        Assert.assertEquals(1, statementList.size());

        List<Object> parameters = new ArrayList<Object>();
        parameters.add(10);

        //MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        ParserVisitor visitor = new ParserVisitor(parameters);
        statemen.accept(visitor);

        System.out.println(sql);
        System.out.println("Tables : " + visitor.getTables());
        System.out.println("fields : " + visitor.getColumns());
        System.out.println("alias : " + visitor.getAliasMap());
        System.out.println("conditions : " + visitor.getConditions());
        System.out.println("columns : " + visitor.getColumns());

        Assert.assertEquals(1, visitor.getTables().size());
        Assert.assertEquals(true, visitor.containsTable("users"));
        Assert.assertEquals(22, visitor.getConditions().get(0).getValues().get(0));

        Assert.assertEquals(2, visitor.getColumns().size());
        //Assert.assertEquals(true, visitor.getColumns().contains(new Column("users", "id")));
        //Assert.assertEquals(true, visitor.getColumns().contains(new Column("users", "name")));

    }

}