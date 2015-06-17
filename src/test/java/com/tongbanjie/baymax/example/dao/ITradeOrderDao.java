package com.tongbanjie.baymax.example.dao;

import java.util.List;
import com.tongbanjie.baymax.example.vo.TradeOrder;

public interface ITradeOrderDao{
	
	public TradeOrder getById(String id);
	
	public List<TradeOrder> listByExample(TradeOrder order);
	
	public int insert(TradeOrder order);
	
	public int delete(TradeOrder order);
	
	public int update(TradeOrder order);
	
	public int getCount();
	
	public void insertDatas();
}
