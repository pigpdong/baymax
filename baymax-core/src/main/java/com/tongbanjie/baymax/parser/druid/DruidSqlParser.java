package com.tongbanjie.baymax.parser.druid;

import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.parser.druid.model.ParseResult;

import java.util.Map;

public interface DruidSqlParser {

    void initParse(String sql, Map<Integer, ParameterCommand> parameterCommand);
	
	void visitorParse(ParseResult result);

    void statementParse(ParseResult result);

	void changeSql(ParseResult result);

}
