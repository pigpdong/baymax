package com.tongbanjie.baymax.example;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.tongbanjie.baymax.BayMaxContext;
import com.tongbanjie.baymax.example.dao.ITradeOrderDao;
import com.tongbanjie.baymax.example.vo.TradeOrder;
import com.tongbanjie.baymax.router.ITableRule;
import com.tongbanjie.baymax.utils.Pair;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/spring-context.xml")
public class TestMain {
	
	@Autowired
	private ITradeOrderDao dao;
	
	@Autowired
	private BayMaxContext context;
	
	/**
	 * 测试根据ID查询单条
	 */
	@Test
	public void testGetById(){
		System.out.println("--test start--");
		TradeOrder orders = dao.getById("000138");
		System.out.println(JSON.toJSONString(orders, true));
	}
	
	/**
	 * 根据UserId查询列表
	 */
	@Test
	public void testListByExample() {
		TradeOrder example = new TradeOrder();
		example.setUserId("9901");
		List<TradeOrder> orders = dao.listByExample(example);
		System.out.println(JSON.toJSONString(orders, true));
	}
	
	/**
	 * 根据UserId,ID查询列表
	 */
	@Test
	public void testListByExampleWithId() {
		TradeOrder example = new TradeOrder();
		example.setUserId("990138");
		example.setId("000138");
		List<TradeOrder> orders = dao.listByExample(example);
		System.out.println(JSON.toJSONString(orders, true));
	}
	
	/**
	 * 更具productId查询列表[全表扫描]
	 */
	@Test
	public void testListByExampleAllTableScan() {
		TradeOrder example = new TradeOrder();
		//example.setProductId("xs14");
		List<TradeOrder> orders = dao.listByExample(example);
		System.out.println(JSON.toJSONString(orders, true));
	}
	
	/**
	 * Insert测试
	 */
	@Test
	public void testInsert() {
		TradeOrder example = new TradeOrder();
		example.setId("0009");
		example.setProductId("xs14");
		example.setAmount(new BigDecimal(10.01));
		example.setRealPayAmount(new BigDecimal(10.02));
		example.setCreateTime(new Date());
		example.setUserId("9909");
		example.setTaId("t10001");
		int count = dao.insert(example);
		System.out.println(count);
	} 
	
	/**
	 * Detele测试ById
	 */
	@Test
	public void testDelete() {
		TradeOrder example = new TradeOrder();
		example.setId("0001");
		int count = dao.delete(example);
		System.out.println(count);
	}
	
	/**
	 * Detele测试全表
	 */
	@Test
	public void testDeleteAll() {
		TradeOrder example = new TradeOrder();
		int count = dao.delete(example);
		System.out.println(count);
	}
	
	/**
	 * Update测试
	 */
	@Test
	public void testUpdate() {
		TradeOrder example = new TradeOrder();
		example.setId("0001");
		example.setAmount(new BigDecimal(1000));
		int count = dao.update(example);
		System.out.println(count);
	}
	
	/**
	 * Update全表扫描测试
	 */
	@Test
	public void testUpdateAll() {
		TradeOrder example = new TradeOrder();
		example.setAmount(new BigDecimal(1000));
		int count = dao.update(example);
		System.out.println(count);
	}
	
	@Test
	public void testGetCount(){
		System.out.println(dao.getCount());
	}
	
	@Test
	public void insertDatas(){
		dao.insertDatas();
	}
	
	@Test
	public void testTransaction(){
		testDeleteAll();
		testInsert();
		insertDatas();
		
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
	
	@Autowired
	List<ITableRule> tableRules;
	//@Test
	public void testConfig(){
		System.out.println(JSON.toJSONString(tableRules, true));
	}
	
	public static void main(String[] args) {
		System.out.println(1%4);
		System.out.println(2%4);
		System.out.println(3%4);
		System.out.println(4%4);
		
	}
	
}
