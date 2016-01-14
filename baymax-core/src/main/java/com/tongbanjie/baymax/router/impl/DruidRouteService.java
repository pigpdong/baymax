package com.tongbanjie.baymax.router.impl;

import java.util.List;
import java.util.Map;

import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.router.RouteService;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import com.tongbanjie.baymax.support.TableCreater;

public class DruidRouteService implements RouteService{

	@Override
	public ExecutePlan doRoute(String sql, Map<Integer, ParameterCommand> parameterCommand) {
		
		return null;
	}

	@Override
	public List<TableCreater> getTableCreaters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

}
