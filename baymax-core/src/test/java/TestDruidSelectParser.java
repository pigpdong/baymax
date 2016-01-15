import com.tongbanjie.baymax.parser.druid.impl.DruidSelectParser;
import org.junit.Test;

/**
 * Created by sidawei on 16/1/15.
 */
public class TestDruidSelectParser {

    @Test
    public void testParse(){
        DruidSelectParser parser = new DruidSelectParser();
        parser.parse("select * from t1 where a = 1 and (b = 2 or c = 3)");
    }

}
