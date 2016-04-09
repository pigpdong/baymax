package example;

import com.tongbanjie.baymax.datasource.BaymaxDataSource;
import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.jdbc.model.ParameterMethod;
import com.tongbanjie.baymax.router.RouteService;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/spring-context.xml")
public class TestBayMax {
	
	@Autowired
	private BaymaxDataSource dataSource;
	
	@Test
	public void test() throws SQLException, InterruptedException{
		Connection conn = dataSource.getConnection();
		PreparedStatement stmt = conn.prepareStatement("select *,count(*) as c from trade_order");
		//stmt.setString(1, "0003");
		ResultSet res = stmt.executeQuery();
		while(res.next()){
			String id = res.getString("id");
			Long c = res.getLong("c");
			System.out.println("---"+id+","+c+"--");
		}
		stmt.close();
		conn.close();
	}
	
	//@Test
	public void testRule(){
		Map<Integer, ParameterCommand> args = new HashMap<Integer, ParameterCommand>();
		ParameterCommand command = new ParameterCommand(ParameterMethod.setString, new Object[]{"10"}, 10);
		args.put(1, command);
		ExecutePlan plan = new RouteService().doRoute("select * from @@trade_order where user_id=10", args);
		System.out.println(plan);
	}
}
