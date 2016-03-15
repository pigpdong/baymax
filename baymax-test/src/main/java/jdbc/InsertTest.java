package jdbc;

import com.tongbanjie.baymax.datasource.BaymaxDataSource;
import jdbc.frame.Jdbc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/spring-context.xml")
public class InsertTest {
	
	@Autowired
	private BaymaxDataSource dataSource;

    @Test
    public void test0() throws SQLException, InterruptedException {
        test(1,1,1,"n1",1);
        test(2,2,1,"n1",1);
        test(3,3,1,"n1",1);
        test(4,4,1,"n1",1);

        test(5,1,1,"n1",1);
        test(6,2,1,"n1",1);
        test(7,3,1,"n1",1);
        test(8,4,1,"n1",1);

        test(9,9,1,"n1",1);
        test(10,10,1,"n1",1);
        test(11,11,1,"n1",1);
        test(12,12,1,"n1",1);
    }
	
	public void test(final int orderId, final int userId, final int productId, final String productName, final int status) throws SQLException, InterruptedException{

        int effctCount = new Jdbc(dataSource).executeUpdate("INSERT INTO `t_order`(order_id, user_id, product_id, product_name, status) VALUES (?, ?, ?, ?, ?);", new Jdbc.PrepareSetting() {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, orderId);
                statement.setInt(2, userId);
                statement.setInt(3, productId);
                statement.setString(4, productName);
                statement.setInt(5, status);
            }
        }).getEffectCount();


        System.out.println(effctCount);
	}
	
}
