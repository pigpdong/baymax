package com.tongbanjie.baymax.router;

import java.util.List;
import java.util.Map;

import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.parser.druid.model.ParseResult;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import com.tongbanjie.baymax.support.TableCreater;

/**
 * SQL路由服务
 * @author dawei
 *
 */
public interface RouteService {

	ExecutePlan doRoute(String sql, Map<Integer, ParameterCommand> parameterCommand);

	List<TableCreater> getTableCreaters();

	void init();

}
