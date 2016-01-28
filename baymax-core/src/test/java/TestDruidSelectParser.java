import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.tongbanjie.baymax.parser.druid.visitor.MycatSchemaStatVisitor;
import com.tongbanjie.baymax.parser.druid.visitor.ReplaceTableNameVisitor;
import com.tongbanjie.baymax.router.DruidRouteService;
import org.junit.Test;

/**
 * Created by sidawei on 16/1/15.
 */
public class TestDruidSelectParser {

    protected SQLStatementParser parser;
    protected SQLASTVisitor visitor;
    protected SQLStatement statement;

    @Test
    public void testParse(){
        DruidRouteService routeService = new DruidRouteService();
        routeService.doRoute("select * as id from t1 where a = 1 and (b = 2 or t1.c = 3) or b in (select b from t2 where x=123)", null);
    }

    @Test
    public void testVisitor(){
        parser 			= new MySqlStatementParser("select *,(select id from t2 where a=1) as id from t1 where a = 1 and (b = 2 or c = 3) or b in (select b from t2)");
        visitor 		= new ReplaceTableNameVisitor("t2", "ttb");
        statement 		= parser.parseStatement();

        statement.accept(visitor);

        StringBuffer sb = new StringBuffer();
        visitor = new MySqlOutputVisitor(sb);
        statement.accept(visitor);

        System.out.println(sb);
    }

}
