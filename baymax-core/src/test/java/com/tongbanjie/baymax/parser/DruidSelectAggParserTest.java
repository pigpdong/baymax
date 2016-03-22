package com.tongbanjie.baymax.parser;

import com.tongbanjie.baymax.parser.model.SqlType;
import com.tongbanjie.baymax.test.SelectTestSql;

/**
 * Created by sidawei on 16/3/18.
 */
public class DruidSelectAggParserTest extends DruidSelectParser implements SelectTestSql {

    IDruidSqlParser parser = DruidParserFactory.getParser(SqlType.SELECT);

    /*--------------------------------------------------single------------------------------------------------------*/



}
