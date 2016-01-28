package com.tongbanjie.baymax.parser.druid;

import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.parser.model.SqlType;

import java.util.Map;

/**
 * Created by sidawei on 16/1/27.
 */
public class DruidParserFactory {

    public static IDruidSqlParser getParser(SqlType sqlType){

        if (sqlType == null){
            return null;
        }

        switch (sqlType){
            case INSERT:
                return new DruidInsertParser();
            case DELETE:
                return new DruidDeleteParser();
            case UPDATE:
                return new DruidUpdateParser();
            case SELECT:
                return new DruidSelectParser();
        }
        return null;
    }

}
