package com.tongbanjie.baymax.parser.druid;

import com.tongbanjie.baymax.parser.druid.model.ParseResult;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import com.tongbanjie.baymax.router.model.ExecuteType;

/**
 * Created by sidawei on 16/1/15.
 */
public class DruidSelectParser extends AbstractDruidSqlParser {

    @Override
    public void changeSql(ParseResult result, ExecutePlan plan) {

        if (plan.getExecuteType() == ExecuteType.PARTITION || plan.getExecuteType() == ExecuteType.ALL){
            // 改写SQL
        }

    }
}
