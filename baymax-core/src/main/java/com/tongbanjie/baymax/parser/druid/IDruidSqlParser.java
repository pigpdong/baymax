package com.tongbanjie.baymax.parser.druid;

import com.tongbanjie.baymax.parser.druid.model.ParseResult;
import com.tongbanjie.baymax.router.model.ExecutePlan;

import java.util.List;

public interface IDruidSqlParser {

    void init(String sql, List<Object> parameters);
	
	void parse(ParseResult result);

	void changeSql(ParseResult result, ExecutePlan plan);

}
