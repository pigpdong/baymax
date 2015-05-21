package com.tongbanjie.baymax.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;
import com.tongbanjie.baymax.BayMaxContext;
import com.tongbanjie.baymax.example.dao.TradeOrderDao;
import com.tongbanjie.baymax.example.vo.TradeOrder;
import com.tongbanjie.baymax.router.ITableRule;
import com.tongbanjie.baymax.utils.Pair;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/spring-context.xml")
public class TestMain {
	
	@Autowired
	private TradeOrderDao dao;
	
	@Autowired
	private BayMaxContext context;
	
	@Test
	public void testMyBatis(){
		System.out.println("--test start--");
		List<TradeOrder> orders = dao.list();
		System.out.println(JSON.toJSONString(orders, true));
	}
	
	//@Test
	public void testRouteService(){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userId", 3);
		param.put("id", 10);
		ITableRule rule = context.getTableRules().get(0);
		Pair<String, String> result = rule.executeRule(param);
		System.out.println("----" + result.getObject1() + " " + result.getObject2());
	}
}
