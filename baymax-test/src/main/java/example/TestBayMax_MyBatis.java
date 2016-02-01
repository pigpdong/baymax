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
		TradeOrder orders = dao.getById("0003");
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
		example.setId("0003");
		example.setProductId("xs14");
		example.setAmount(new BigDecimal(10.01));
		example.setRealPayAmount(new BigDecimal(10.02));
		example.setCreateTime(new Date());
		example.setUserId("9903");
		example.setTaId("t10001");
		int count = dao.insert(example);
		System.out.println(count);
	} 
	
	@Test
	public void testInsertAuto(){
		TradeOrder example = new TradeOrder();
		example.setId("0005");
		example.setProductId("xs14");
		example.setAmount(new BigDecimal(10.01));
		example.setRealPayAmount(new BigDecimal(10.02));
		example.setCreateTime(new Date());
		example.setUserId("9905");
		example.setTaId("t10001");
		int count = dao.insetAuto(example);
		System.out.println("count"+count);
		System.out.println("auto"+example.getType());
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
	
	public static void main(String[] args) {
		System.out.println(1%4);
		System.out.println(2%4);
		System.out.println(3%4);
		System.out.println(4%4);
	}
	
}
