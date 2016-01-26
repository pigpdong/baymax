package com.tongbanjie.baymax.router.impl;

import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.parser.druid.impl.DruidSelectParser;
import com.tongbanjie.baymax.parser.druid.model.ParseResult;
import com.tongbanjie.baymax.router.RouteService;
import com.tongbanjie.baymax.router.model.ExecutePlan;

import java.util.List;
import java.util.Map;

public class DruidSelectRouteService implements RouteService{

    public ExecutePlan doRoute(String sql, Map<Integer, ParameterCommand> parameterCommand) {

        DruidSelectParser parser = new DruidSelectParser();

        ParseResult result = new ParseResult();

        parser.initParse(sql, parameterCommand);

        // 通过visitor解析
        parser.visitorParse(result);

        // 路由
        //ExecutePlan plan = route(result, statement);

        // 通过Statement解析
        parser.statementParse(result);

        // 改写sql：如insert语句主键自增长的可以
        parser.changeSql(result);

        //return plan;


        return null;

    }

    private ExecutePlan route(ParseResult parseResult){
        return null;
    }

    public void init() {
        // TODO Auto-generated method stub

    }


}