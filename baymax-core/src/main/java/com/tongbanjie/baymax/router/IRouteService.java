package com.tongbanjie.baymax.router;

import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.router.model.ExecutePlan;

import java.util.Map;

/**
 * SQL路由服务入口
 * @author dawei
 *
 */
public interface IRouteService {

	ExecutePlan doRoute(String sql, Map<Integer, ParameterCommand> parameterCommand);

}
