package com.tongbanjie.baymax.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.tongbanjie.baymax.example.dao.ITradeOrderDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/spring-context.xml")
public class TestMain {
	
	@Autowired
	private ITradeOrderDao dao;
	
	
	@Test
	public void testGetById(){
	}
	
	
}
