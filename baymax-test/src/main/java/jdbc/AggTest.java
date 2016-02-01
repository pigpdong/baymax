package jdbc;

import com.tongbanjie.baymax.datasource.MultipleDataSource;
import jdbc.frame.Jdbc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/spring-context.xml")
public class AggTest {
	
	@Autowired
	private MultipleDataSource dataSource;

    @Test
	public void test() throws SQLException, InterruptedException{

       new Jdbc(dataSource)
        .executeSelect("select count(order_id) as agg_c,sum(order_id) as agg_s,min(order_id) as agg_min,max(order_id) as agg_max,user_id from t_order group by user_id")
               .printSet(new Jdbc.Print() {
                   @Override
                   public Object print(ResultSet set) throws SQLException {
                       return ""+set.getLong("agg_c")
                              + " | " + set.getLong("agg_s")
                              + " | " + set.getLong("agg_min")
                              + " | "+ set.getLong("agg_max");
                   }
        }).close();
	}
	
}
