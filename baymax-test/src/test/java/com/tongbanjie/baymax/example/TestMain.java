package com.tongbanjie.baymax.example;

import java.util.List;

import org.junit.Test;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.stat.TableStat.Condition;
import com.tongbanjie.baymax.druid.AP;
import com.tongbanjie.baymax.parser.druid.impl.DefaultDruidSqlParser;
import com.tongbanjie.baymax.parser.druid.model.SqlParseResult;

public class TestMain {
	
	@Test
	public void t1() {

		SQLStatementParser parser = null;

		parser = new MySqlStatementParser("select * from t1 where t1.id = 1"); 
		

		MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
		SQLStatement statement = parser.parseStatement();
		
		Appendable ap = new AP();
		SQLASTOutputVisitor outv = new SQLASTOutputVisitor(ap);


//		DruidParser druidParser = DruidParserFactory.create(schema, statement, 
//				
//		
//		//设置为原始sql，如果有需要改写sql的，可以通过修改SQLStatement中的属性，然后调用SQLStatement.toString()得到改写的sql
//		ctx.setSql(originSql);
//		//通过visitor解析
//		visitorParse(rrs,stmt,schemaStatVisitor);
//		//通过Statement解析
//		statementParse(schema, rrs, stmt);
//		
//		//改写sql：如insert语句主键自增长的可以
//		changeSql(schema, rrs, stmt,cachePool);
		
		statement.accept(outv);
		List<Condition> conditions = visitor.getConditions();
		 
		System.out.println(ap.toString());
	}
	
	@Test
	public void t2(){
		DefaultDruidSqlParser parser = new DefaultDruidSqlParser();
		SqlParseResult result = parser.parse("select * from t1 where t1.id = 1");
		System.out.print(result);
	}
}
