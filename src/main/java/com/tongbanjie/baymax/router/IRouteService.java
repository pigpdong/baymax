package com.tongbanjie.baymax.router;

import org.apache.ibatis.mapping.BoundSql;

import com.tongbanjie.baymax.model.RouteResult;

/**
 * SQL路由服务
 * @author dawei
 *
 */
public interface IRouteService {

	/**
	 * Mybatis路由专用
	 * @param boundSql
	 * @return
	 */
	RouteResult doRoute(BoundSql boundSql);
	
}
