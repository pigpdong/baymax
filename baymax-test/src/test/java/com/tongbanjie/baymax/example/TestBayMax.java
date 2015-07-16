package com.tongbanjie.baymax.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tongbanjie.baymax.datasource.MultipleDataSource;
import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.jdbc.model.ParameterMethod;
import com.tongbanjie.baymax.router.model.ExecutePlan;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/spring-context.xml")
public class TestBayMax {
	
	@Autowired
	private MultipleDataSource dataSource;
	
	@Test
	public void test() throws SQLException, InterruptedException{
		Connection conn = dataSource.getConnection();
		PreparedStatement stmt = conn.prepareStatement("select * from @@trade_order where id=?");
		stmt.setString(1, "0003");
		ResultSet res = stmt.executeQuery();
		while(res.next()){
			String id = res.getString("id");
			String productId = res.getString("product_id");
			System.out.println(id);
			System.out.println(productId);
		}
		stmt.close();
		conn.close();
	}
	
	//@Test
	public void testRule(){
		Map<Integer, ParameterCommand> args = new HashMap<Integer, ParameterCommand>();
		ParameterCommand command = new ParameterCommand(ParameterMethod.setString, new Object[]{"10"}, 10);
		args.put(1, command);
		ExecutePlan plan = dataSource.getRouteService().doRoute("select * from @@trade_order where user_id=10", args);
		System.out.println(plan);
	}
}
