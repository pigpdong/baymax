package com.tongbanjie.baymax.example.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tongbanjie.baymax.BayMaxSqlSessionDaoSupport;
import com.tongbanjie.baymax.example.vo.TradeOrder;

@Repository
public class TradeOrderDao extends BayMaxSqlSessionDaoSupport {
	
	public TradeOrder getById(String id){
		return (TradeOrder) getSqlSession().selectOne("example.getById", id);
	}
	
	public TradeOrder get(TradeOrder order){
		return (TradeOrder) getSqlSession().selectOne("example.getById", order);
	}

	public List<TradeOrder> list() {
		TradeOrder order = new TradeOrder();
		order.setUserId("xxoo");
		return (List<TradeOrder>) getSqlSession().selectList("example.getById", order);
	}

}
