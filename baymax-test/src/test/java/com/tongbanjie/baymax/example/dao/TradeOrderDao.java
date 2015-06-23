package com.tongbanjie.baymax.example.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.tongbanjie.baymax.example.vo.TradeOrder;

@Repository
public class TradeOrderDao extends SqlSessionDaoSupport implements ITradeOrderDao {
	
	public TradeOrder getById(String id){
		return (TradeOrder) getSqlSession().selectOne("example.getById", id);
	}
	
	@SuppressWarnings("unchecked")
	public List<TradeOrder> listByExample(TradeOrder order){
		return (List<TradeOrder>) getSqlSession().selectList("example.listByExample", order);
	}
	
	public void insertDatas(){
		System.out.println("----insert datas----");
		TradeOrder example = new TradeOrder();
		int count = 0;
		for(int i = 0; i < 2; i++){
			example.setId("000"+i);
			example.setProductId("xs14");
			example.setAmount(new BigDecimal(10.01));
			example.setRealPayAmount(new BigDecimal(10.02));
			example.setCreateTime(new Date());
			example.setUserId("990"+i);
			example.setTaId("t10001");
			example.setStatus(0);
			example.setType(0);
			count += insert(example);
		}
		System.out.println(count);
		
		for(int i = 2; i < 4; i++){
			example.setId("000"+i);
			example.setProductId("xs14");
			example.setAmount(new BigDecimal(10.01));
			example.setRealPayAmount(new BigDecimal(10.02));
			example.setCreateTime(new Date());
			example.setUserId("990"+i);
			example.setTaId("t10001");
			example.setStatus(0);
			example.setType(0);
			count += insert(example);
		}
		System.out.println(count);
	}
	@Transactional
	public int insert(TradeOrder order){
		return getSqlSession().insert("example.insert", order);
	}
	
	public int delete(TradeOrder order){
		return getSqlSession().delete("example.deleteByExample", order);
	}
	
	public int update(TradeOrder order){
		return getSqlSession().update("example.updateById", order);
	}
	
	public int getCount(){
		return (Integer) getSqlSession().selectOne("example.getCount");
	}

	@Override
	public int insetAuto(TradeOrder order) {
		return getSqlSession().insert("example.insertAuto", order);
	}
}
