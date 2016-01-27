package com.tongbanjie.baymax.parser.druid;

import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.parser.druid.model.ParseResult;
import com.tongbanjie.baymax.router.model.ExecutePlan;

import java.util.Map;

public interface IDruidSqlParser {

    void init(String sql, Map<Integer, ParameterCommand> parameterCommand);
	
	void parse(ParseResult result);

	void changeSql(ParseResult result, ExecutePlan plan);

}
