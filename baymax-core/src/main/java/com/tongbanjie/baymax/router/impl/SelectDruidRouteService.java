package com.tongbanjie.baymax.router.impl;

import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.parser.druid.impl.DruidSelectParser;
import com.tongbanjie.baymax.parser.druid.model.ParseResult;
import com.tongbanjie.baymax.router.RouteService;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import com.tongbanjie.baymax.support.TableCreater;

import java.util.List;
import java.util.Map;

public abstract class SelectDruidRouteService implements RouteService{

    public ExecutePlan doRoute(String sql, Map<Integer, ParameterCommand> parameterCommand) {


        // select
        DruidSelectParser parser = new DruidSelectParser();

        return parser.parse(sql);

    }

    public List<TableCreater> getTableCreaters() {
        // TODO Auto-generated method stub
        return null;
    }

    public void init() {
        // TODO Auto-generated method stub

    }


}