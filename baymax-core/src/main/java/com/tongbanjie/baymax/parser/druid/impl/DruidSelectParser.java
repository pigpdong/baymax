package com.tongbanjie.baymax.parser.druid.impl;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.tongbanjie.baymax.parser.druid.AbstractDruidSqlParser;
import com.tongbanjie.baymax.parser.druid.model.ParseResult;
import com.tongbanjie.baymax.router.model.ExecutePlan;

/**
 * Created by sidawei on 16/1/15.
 */
public class DruidSelectParser extends AbstractDruidSqlParser {

    @Override
    protected ExecutePlan route(ParseResult result, SQLStatement statement){

        return null;
    }

    @Override
    protected void statementParse(SQLStatement statement, ParseResult result) {

    }

    @Override
    protected void changeSql(ParseResult result, SQLStatement statement) {

    }

}
