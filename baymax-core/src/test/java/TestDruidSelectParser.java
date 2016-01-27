import com.tongbanjie.baymax.router.impl.DruidSelectRouteService;
import org.junit.Test;

/**
 * Created by sidawei on 16/1/15.
 */
public class TestDruidSelectParser {

    @Test
    public void testParse(){
        DruidSelectRouteService routeService = new DruidSelectRouteService();
        routeService.doRoute("select * from t1 where a = 1 and (b = 2 or c = 3)", null);
    }

}
