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
	
	@SuppressWarnings("unchecked")
	public List<TradeOrder> listByExample(TradeOrder order){
		return (List<TradeOrder>) getSqlSession().selectList("example.listByExample", order);
	}
	
	public int insert(TradeOrder order){
		return getSqlSession().insert("example.insert", order);
	}
	
	public int delete(TradeOrder order){
		return getSqlSession().delete("example.deleteByExample", order);
	}
	
	public int update(TradeOrder order){
		return getSqlSession().update("example.updateById", order);
	}
}
