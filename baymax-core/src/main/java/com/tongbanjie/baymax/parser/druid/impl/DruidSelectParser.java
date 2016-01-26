package com.tongbanjie.baymax.parser.druid.impl;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.tongbanjie.baymax.parser.druid.AbstractDruidSqlParser;
import com.tongbanjie.baymax.parser.druid.model.ParseResult;
import com.tongbanjie.baymax.router.model.ExecutePlan;

/**
 * Created by sidawei on 16/1/15.
 */
public class DruidSelectParser extends AbstractDruidSqlParser {

    @Override
    public void changeSql(ParseResult result) {

    }

    @Override
    public void statementParse(ParseResult result) {

    }
}
