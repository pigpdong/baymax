package com.tongbanjie.baymax.parser.druid;

import com.tongbanjie.baymax.router.model.ExecutePlan;


public interface DruidSqlParser {

    ExecutePlan parse(String sql);
	
}
