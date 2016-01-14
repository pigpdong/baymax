package com.tongbanjie.baymax.parser.druid;

import com.tongbanjie.baymax.parser.druid.model.SqlParseResult;


public interface DruidSqlParser {

	SqlParseResult parse(String sql);
	
}
