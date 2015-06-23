package com.tongbanjie.baymax.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tongbanjie.baymax.datasource.MultipleDataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/spring-context.xml")
public class TestBayMax {
	
	@Autowired
	private MultipleDataSource dataSource;
	
	@Test
	public void testGetById() throws SQLException, InterruptedException{
		Connection c = dataSource.getConnection();
		Statement stmt = c.createStatement();
		ResultSet res = stmt.executeQuery("select * from trade_order");
		while(res.next()){
			String id = res.getString(1);
			System.out.println(id);
		}
	}
}
