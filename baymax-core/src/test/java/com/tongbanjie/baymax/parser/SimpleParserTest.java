package com.tongbanjie.baymax.parser;

import com.tongbanjie.baymax.test.PrintUtil;
import com.tongbanjie.baymax.parser.model.ParseResult;
import com.tongbanjie.baymax.parser.model.SqlType;
import org.junit.Test;

/**
 * Created by sidawei on 16/3/21.
 */
public class SimpleParserTest {

    IDruidSqlParser selectParser = DruidParserFactory.getParser(SqlType.SELECT);

    @Test
    public void test_0(){

    }

    private void test(String sql){
        ParseResult result = new ParseResult();
        selectParser.init(sql, null);
        selectParser.parse(result);
        PrintUtil.printCalculates(result.getCalculateUnits());
    }

}
