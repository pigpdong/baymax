package example.dao;

import example.vo.TradeOrder;

import java.util.List;

public interface ITradeOrderDao{
	
	public TradeOrder getById(String id);
	
	public List<TradeOrder> listByExample(TradeOrder order);
	
	public int insert(TradeOrder order);
	
	public int delete(TradeOrder order);
	
	public int update(TradeOrder order);
	
	public int getCount();
	
	public void insertDatas();
	
	public int insetAuto(TradeOrder order);
}
