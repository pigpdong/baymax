package com.tongbanjie.baymax.router;

import org.apache.ibatis.mapping.BoundSql;

import com.tongbanjie.baymax.model.RouteResult;

public interface IRouteService {

	RouteResult doRoute(String statement, Object parameter);

	/**
	 * Mybatis路由专用
	 * @param boundSql
	 * @return
	 */
	RouteResult doRoute(BoundSql boundSql);

}
