package example.dao;

import example.vo.TradeOrder;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Repository
public class TradeOrderDao extends SqlSessionDaoSupport implements ITradeOrderDao {
	
	public TradeOrder getById(String id){
		return (TradeOrder) getSqlSession().selectOne("jpaexample.getById", id);
	}

    @Override
    public List<TradeOrder> listByExample(TradeOrder order) {
        return null;
    }

    @Override
    public int insert(TradeOrder order) {
        return 0;
    }

    @Override
    public int delete(TradeOrder order) {
        return 0;
    }

    @Override
    public int update(TradeOrder order) {
        return 0;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public void insertDatas() {

    }

    @Override
    public int insetAuto(TradeOrder order) {
        return 0;
    }
}
