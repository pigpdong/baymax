package parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.parser.druid.visitor.ReplaceTableNameVisitor;
import com.tongbanjie.baymax.router.DruidRouteService;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sidawei on 16/1/15.
 */
public class DruidInsertParserTest {

    protected SQLStatementParser parser;
    protected SQLASTVisitor visitor;
    protected SQLStatement statement;

    //String sql = "select * as id from t1 where a = 1 and (b = 2 or t1.c = 3) or b in (select b from t2 where x=123)";
    //String sql = "select *,(select id from t2 where a=1) as id from t1 where a = 1 and (b = 2 or c = 3) or b in (select b from t2)";
    String sql = "select * from t where a=? and b=? or c=?";

    @Test
    public void testParse(){
        DruidRouteService routeService = new DruidRouteService();

        Map<Integer, ParameterCommand> commands = new HashMap<Integer, ParameterCommand>();
        commands.put(2, new ParameterCommand(null, null, 2));
        commands.put(1, new ParameterCommand(null, null, 1));
        commands.put(3, new ParameterCommand(null, null, "3"));
        routeService.doRoute(sql, commands);
    }

    @Test
    public void testVisitor(){
        parser 			= new MySqlStatementParser(sql);
        visitor 		= new ReplaceTableNameVisitor("t2", "ttb");
        statement 		= parser.parseStatement();

        statement.accept(visitor);

        StringBuffer sb = new StringBuffer();
        visitor = new MySqlOutputVisitor(sb);
        statement.accept(visitor);

        System.out.println(sb);
    }

}
