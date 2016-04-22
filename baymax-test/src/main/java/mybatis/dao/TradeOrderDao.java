package mybatis.dao;

import com.tbjfund.framework.tpa.TpaSupportDao;
import mybatis.vo.TradeOrder;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TradeOrderDao extends TpaSupportDao<TradeOrder, Integer> implements ITradeOrderDao {

}
