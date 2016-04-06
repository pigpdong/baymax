package com.tongbanjie.baymax.router;

import com.tongbanjie.baymax.exception.BayMaxException;
import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.parser.DruidParserFactory;
import com.tongbanjie.baymax.parser.IDruidSqlParser;
import com.tongbanjie.baymax.parser.model.CalculateUnit;
import com.tongbanjie.baymax.parser.model.ParseResult;
import com.tongbanjie.baymax.parser.model.SqlType;
import com.tongbanjie.baymax.parser.utils.SqlTypeUtil;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import com.tongbanjie.baymax.router.model.ExecuteType;
import com.tongbanjie.baymax.router.model.TargetSql;
import com.tongbanjie.baymax.router.strategy.PartitionTable;
import com.tongbanjie.baymax.support.BaymaxContext;
import com.tongbanjie.baymax.utils.Pair;

import java.util.*;

public class DruidRouteService implements IRouteService {

    public ExecutePlan doRoute(String sql, Map<Integer, ParameterCommand> parameterCommand) {

        SqlType sqlType = SqlTypeUtil.getSqlType(sql);

        IDruidSqlParser parser = DruidParserFactory.getParser(sqlType);

        if (parser == null){
            return buildExecutePlanTypeNo(sql, null, sqlType);
        }

        // 解析结果
        ParseResult result = new ParseResult();

        // 初始化解析器
        parser.init(sql, buildParameters(parameterCommand));

        // 解析
        parser.parse(result);

        // 路由
        ExecutePlan plan = route(result, sqlType);

        // 改写sql
        parser.changeSql(result, plan);

        return plan;
    }

    private ExecutePlan route(ParseResult result, SqlType sqlType){
        List<String> tables = result.getTables();
        // 判断是否解析到表名
        if (tables == null || tables.size() == 0){
            return buildExecutePlanTypeNo(result.getSql(), null, sqlType);
        }
        // 查找逻辑表对应的分区规则
        PartitionTable partitionTable = null;
        for (String tableName : tables){
            if (BaymaxContext.isPartitionTable(tableName)){
                if (partitionTable != null){
                    throw new BayMaxException("sql中包含了两个分区表");
                }else {
                    partitionTable = BaymaxContext.getPartitionTable(tableName);
                }
            }
        }
        // 没有规则 无需路由
        if (partitionTable == null){
            return buildExecutePlanTypeNo(result.getSql(), null, sqlType);
        }

        // 没有计算单元 全表扫描
        if (result.getCalculateUnits() == null || result.getCalculateUnits().size() == 0){
            return buildExecutePlanTypeAll(result.getSql(), partitionTable, sqlType);
        }

        // 路由单元计算-合并
        Set<Pair<String/* targetDB */, String/* targetTable */>> nodeSet = new LinkedHashSet<Pair<String, String>>();
        for (CalculateUnit unit : result.getCalculateUnits()) {
            List<Pair<String/* targetDB */, String/* targetTable */>> temp = partitionTable.execute(unit);
            if (temp == null || temp.size() == 0){
                // 这个单元没有路由结果 需要全表扫描
                return buildExecutePlanTypeAll(result.getSql(), partitionTable, sqlType);
            }else {
                nodeSet.addAll(temp);
            }
        }

        if (nodeSet.size() == 0){
            return buildExecutePlanTypeNo(result.getSql(), partitionTable.getLogicTableName(), sqlType);
        }

        return buildExecutePlanTypePartition(result.getSql(), partitionTable, sqlType, nodeSet);
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
     * @param partitionTable
     * @param sqlType
     * @return
     */
    private ExecutePlan buildExecutePlanTypeAll(String sql, PartitionTable partitionTable, SqlType sqlType){
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
                actionSql.setLogicTableName(partitionTable.getLogicTableName());
                actionSql.setTargetTableName(pt.getObject2());
                plan.addSql(actionSql);
            }
            plan.setExecuteType(ExecuteType.ALL);
        }else{
            plan.setExecuteType(ExecuteType.NO);
        }
        return plan;
    }

    /**
     * 创建分区执行计划
     * @param sql
     * @param partitionTable
     * @param sqlType
     * @param nodeSet
     * @return
     */
    private ExecutePlan buildExecutePlanTypePartition(String sql, PartitionTable partitionTable, SqlType sqlType, Set<Pair<String/* targetDB */, String/* targetTable */>> nodeSet) {
        ExecutePlan routeResult = new ExecutePlan();
        routeResult.setExecuteType(ExecuteType.PARTITION);
        for (Pair<String/* targetDB */, String/* targetTable */> node : nodeSet){
            TargetSql actionSql = new TargetSql();
            actionSql.setSqlType(sqlType);
            actionSql.setPartition(node.getObject1());
            actionSql.setLogicTableName(partitionTable.getLogicTableName());
            actionSql.setTargetTableName(node.getObject2());
            routeResult.addSql(actionSql);
        }
        return routeResult;
    }

    /**
     * 参数排序
     * @param commonds
     * @return
     */
    public List<Object> buildParameters(Map<Integer, ParameterCommand> commonds){

        if (commonds == null || commonds.size() == 0){
            return null;
        }

        // 排序
        SortedSet<Map.Entry<Integer, ParameterCommand>> set = new TreeSet<Map.Entry<Integer, ParameterCommand>>(new Comparator<Map.Entry<Integer, ParameterCommand>>() {
            @Override
            public int compare(Map.Entry<Integer, ParameterCommand> e1, Map.Entry<Integer, ParameterCommand> e2) {
                return e1.getKey().compareTo(e2.getKey());
            }
        });

        for (Map.Entry entry : commonds.entrySet()){
            set.add(entry);
        }

        List<Object> result = new ArrayList<Object>(commonds.size());
        for (Map.Entry<Integer, ParameterCommand> entry : set){
            result.add(entry.getValue().getParttionArg());
        }

        return result;
    }
}