package jdbc;

import com.tongbanjie.baymax.datasource.BaymaxDataSource;
import jdbc.frame.Jdbc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.ResultSet;
import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/spring-context.xml")
public class OrderbyTest {
	
	@Autowired
	private BaymaxDataSource dataSource;

    @Test
	public void test() throws SQLException, InterruptedException{

       new Jdbc(dataSource)
        .executeSelect("select order_id from t_order t order by t.order_id desc")
               .printSet(new Jdbc.Print() {
                   @Override
                   public Object print(ResultSet set) throws SQLException {
                       return set.getString("order_id");
                   }
        }).close();
	}

    @Test
    public void test_0() throws SQLException, InterruptedException{

        new Jdbc(dataSource)
                .executeSelect("select order_id from t_order t order by status desc,user_id desc")
                .printSet(new Jdbc.Print() {
                    @Override
                    public Object print(ResultSet set) throws SQLException {
                        return set.getString("user_id")+"|"+set.getString("status");
                    }
                }).close();
    }


}
