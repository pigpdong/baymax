package com.tongbanjie.baymax.parser;

import com.tongbanjie.baymax.parser.model.SqlType;

/**
 * Created by sidawei on 16/1/27.
 */
public class DruidParserFactory {

    public static IDruidSqlParser getParser(SqlType sqlType){

        if (sqlType == null)
            return null;

        switch (sqlType){
            case SELECT:
                return new DruidSelectParser();
            case INSERT:
                return new DruidInsertParser();
            case UPDATE:
                return new DruidUpdateParser();
            case DELETE:
                return new DruidDeleteParser();
            default:
                return null;
        }
    }

}
