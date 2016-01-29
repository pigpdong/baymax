package com.tongbanjie.baymax.parser.druid;

import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnionQuery;
import com.tongbanjie.baymax.jdbc.mearge.MergeCol;
import com.tongbanjie.baymax.parser.druid.model.ParseResult;
import com.tongbanjie.baymax.router.model.ExecutePlan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sidawei on 16/1/15.
 */
public class DruidSelectParser extends AbstractDruidSqlParser {

    @Override
    public void changeSql(ParseResult result, ExecutePlan plan) {
        parseStatement(result, plan, (SQLSelectStatement)statement);
        super.changeSql(result, plan);
    }

    private void parseStatement(ParseResult result, ExecutePlan plan, SQLSelectStatement statement){
        // 只有一个目标sql
        if (plan.getSqlList().size() <= 1){
            return;
        }

        SQLSelectQuery sqlSelectQuery = statement.getSelect().getQuery();
        if(sqlSelectQuery instanceof MySqlSelectQueryBlock) {
            // mysql查询
            parseMysqlQueary(result, plan, (MySqlSelectQueryBlock) sqlSelectQuery);
        } else if (sqlSelectQuery instanceof MySqlUnionQuery) {
            // TODO 测试
			MySqlUnionQuery unionQuery = (MySqlUnionQuery)sqlSelectQuery;
			MySqlSelectQueryBlock left = (MySqlSelectQueryBlock)unionQuery.getLeft();
			MySqlSelectQueryBlock right = (MySqlSelectQueryBlock)unionQuery.getLeft();
            if (left.getFrom().getAlias().equalsIgnoreCase(plan.getSqlList().get(0).getLogicTableName())){
                parseMysqlQueary(result, plan, left);
            }
            if (right.getFrom().getAlias().equalsIgnoreCase(plan.getSqlList().get(0).getLogicTableName())){
                parseMysqlQueary(result, plan, right);
            }
        }
    }

    private void parseMysqlQueary(ParseResult result, ExecutePlan plan, MySqlSelectQueryBlock mysqlSelectQuery){
        Map<String, String>     aliaColumns         = new HashMap<String, String>();
        Map<String, Integer>    aggrColumns         = new HashMap<String, Integer>();
        List<SQLSelectItem>     selectList          = mysqlSelectQuery.getSelectList();
        boolean                 isNeedChangeSql     = false;
        int                     size                = selectList.size();
        boolean                 isDistinct          = mysqlSelectQuery.getDistionOption() == 2;

        for (int i = 0; i < size; i++){
            SQLSelectItem item = selectList.get(i);
            if (item.getExpr() instanceof SQLAggregateExpr){
                SQLAggregateExpr expr = (SQLAggregateExpr) item.getExpr();
                String method = expr.getMethodName();

                //只处理有别名的情况，无别名添加别名，否则某些数据库会得不到正确结果处理
                int mergeType = MergeCol.getMergeType(method);

                if (MergeCol.MERGE_AVG == mergeType){

                }else if (MergeCol.MERGE_UNSUPPORT != mergeType){
                    if (item.getAlias() != null && item.getAlias().length() > 0){
                        aggrColumns.put(item.getAlias(), mergeType);
                    } else{
                        //如果不加，jdbc方式时取不到正确结果   ;修改添加别名
                        item.setAlias(method + i);
                        aggrColumns.put(method + i, mergeType);
                        isNeedChangeSql = true;
                    }
                }
            }else {

            }
        }

        plan.setMergeColumns(aggrColumns);
    }

    private void parseAggregate(){

    }

    private void parseGroupBy(){

    }

    private void parseLimit(){

    }

    private void parseReadWrite(){
        //更改canRunInReadDB属性
//            if ((mysqlSelectQuery.isForUpdate() || mysqlSelectQuery.isLockInShareMode()) && !rrs.isAutocommit())
//                rrs.setCanRunInReadDB(false);
//            }
    }
}
