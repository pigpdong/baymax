package com.tongbanjie.baymax.router;

import com.tongbanjie.baymax.exception.BayMaxException;
import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.parser.SqlParser;
import com.tongbanjie.baymax.parser.druid.DruidParserFactory;
import com.tongbanjie.baymax.parser.druid.DruidSelectParser;
import com.tongbanjie.baymax.parser.druid.IDruidSqlParser;
import com.tongbanjie.baymax.parser.druid.model.ParseResult;
import com.tongbanjie.baymax.parser.def.DefaultSqlParser;
import com.tongbanjie.baymax.parser.model.SqlType;
import com.tongbanjie.baymax.parser.utils.SqlTypeUtil;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import com.tongbanjie.baymax.router.model.ExecuteType;
import com.tongbanjie.baymax.router.model.TargetSql;
import com.tongbanjie.baymax.support.Function;
import com.tongbanjie.baymax.utils.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DruidRouteService implements IRouteService {

    /**
     * 上下文中所有的路由规则列表
     */
    private List<PartitionTable> partitionTables;

    /**
     * 上下文中所有路由规则的MAP，方便使用表名查找到对应的路由规则
     */
    private Map<String/*TableName*/, PartitionTable> tableRuleMapping = new HashMap<String, PartitionTable>();

    /**
     * SQL解析器
     * 主要用来提取SQL中的表名,Where中的KEY=VALUE形式的参数
     */
    private SqlParser parser = new DefaultSqlParser();

    private Map<String, Function<?,?>> functionsMap = new HashMap<String, Function<?,?>>();


    public ExecutePlan doRoute(String sql, Map<Integer, ParameterCommand> parameterCommand) {

        SqlType sqlType = SqlTypeUtil.getSqlType(sql);

        IDruidSqlParser parser = DruidParserFactory.getParser(null);

        if (parser == null){
            return buildExecutePlanTypeNo(sql, null, sqlType);
        }

        ParseResult result = new ParseResult();

        // 初始化
        parser.initParse(sql, parameterCommand);

        // 解析
        parser.parse(result);

        // 路由
        ExecutePlan plan = route(result);

        // 改写sql：如insert语句主键自增长的可以
        parser.changeSql(result, plan);

        return plan;

    }

    private ExecutePlan route(ParseResult result){
        List<String> tables = result.getTables();
        // 判断是否解析到表名
        if (tables == null || tables.size() == 0){
            // TODO SqlType.SELECT
            return buildExecutePlanTypeNo(result.getSql(), null, SqlType.SELECT);
        }
        // 查找逻辑表对应的分区规则
        PartitionTable partitionTable = null;
        for (String tableName : tables){
            if (tableRuleMapping.get(tableName) != null){
                if (partitionTable == null){
                    partitionTable = tableRuleMapping.get(tableName);
                }else {
                    throw new BayMaxException("sql中包含了两个分区表");
                }
            }
        }
        // 没有规则 无需路由
        if (partitionTable == null){
            // TODO SqlType.SELECT
            return buildExecutePlanTypeNo(result.getSql(), null, SqlType.SELECT);
        }
        // 查到规则 需要路由

        return null;
    }

    /**
     * 创建无路由执行计划
     * @param sql
     * @param tableName
     * @param sqlType
     * @return
     */
    private ExecutePlan buildExecutePlanTypeNo(String sql, String tableName, SqlType sqlType){
        // 不需要路由
        ExecutePlan plan = new ExecutePlan();
        plan.setExecuteType(ExecuteType.NO);
        TargetSql actionSql = new TargetSql();
        actionSql.setSqlType(sqlType);
        actionSql.setPartition(null);
        actionSql.setLogicTableName(tableName);
        actionSql.setOriginalSql(sql);
        actionSql.setTargetSql(sql);
        actionSql.setTargetTableName(tableName);
        plan.addSql(actionSql);
        return plan;
    }

    /**
     * 创建全表扫描执行计划
     * TODO 考虑聚合函数
     * @param sql
     * @param logicTableName
     * @param partitionTable
     * @param sqlType
     * @return
     */
    private ExecutePlan buildExecutePlanTypeAll(String sql, String logicTableName, PartitionTable partitionTable, SqlType sqlType){
        // 没有命中的shardingKey,则全表扫描
        ExecutePlan plan = new ExecutePlan();
        List<Pair<String/*partion*/, String/*table*/>> mappings = partitionTable.getAllTableNames();
        if(mappings != null && mappings.size() > 0){
            for(Pair<String/*partion*/, String/*table*/> pt : mappings){
                /**
                 * 全表扫描：SQL对象只存储targetDB,targetTableName;执行时不改SQL,不改参数.
                 */
                TargetSql actionSql = new TargetSql();
                actionSql.setSqlType(sqlType);
                actionSql.setPartition(pt.getObject1());
                actionSql.setLogicTableName(logicTableName);
                actionSql.setOriginalSql(sql);
                actionSql.setTargetSql(parser.replaceTableName(actionSql.getOriginalSql(), logicTableName, pt.getObject2()));//逻辑表名替换为实际表名
                actionSql.setTargetTableName(pt.getObject2());
                plan.addSql(actionSql);
            }
            plan.setExecuteType(ExecuteType.ALL);
        }else{
            plan.setExecuteType(ExecuteType.NO);
        }
        return plan;
    }

    public void init() {
        // TODO Auto-generated method stub

    }


}