package example;

import com.alibaba.fastjson.JSON;
import example.dao.ITradeOrderDao;
import example.vo.TradeOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 用来测试BayMax对MyBatis的兼容性
 * @author dawei
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/spring-context.xml")
public class TestBayMax_MyBatis {

	@Autowired
	private ITradeOrderDao dao;
	
	/**
	 * 测试根据ID查询单条
	 */
	@Test
	public void testGetById(){
		System.out.println("--test start--");
		TradeOrder orders = dao.getById("33");
		System.out.println(JSON.toJSONString(orders, true));
	}

}
