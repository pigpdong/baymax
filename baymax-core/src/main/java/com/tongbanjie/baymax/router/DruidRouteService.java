package com.tongbanjie.baymax.router;

import com.tongbanjie.baymax.exception.BayMaxException;
import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.parser.druid.DruidParserFactory;
import com.tongbanjie.baymax.parser.druid.IDruidSqlParser;
import com.tongbanjie.baymax.parser.druid.model.ParseResult;
import com.tongbanjie.baymax.parser.model.SqlType;
import com.tongbanjie.baymax.parser.utils.SqlTypeUtil;
import com.tongbanjie.baymax.router.model.CalculateUnit;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import com.tongbanjie.baymax.router.model.ExecuteType;
import com.tongbanjie.baymax.router.model.TargetSql;
import com.tongbanjie.baymax.router.strategy.IPartitionTable;
import com.tongbanjie.baymax.support.Function;
import com.tongbanjie.baymax.utils.Pair;

import java.util.*;

public class DruidRouteService implements IRouteService {

    /**
     * 上下文中所有的路由规则列表
     */
    private List<IPartitionTable> partitionTables;

    /**
     * 上下文中所有路由规则的MAP，方便使用表名查找到对应的路由规则
     */
    private Map<String/*TableName*/, IPartitionTable> tableRuleMapping = new HashMap<String, IPartitionTable>();

    /**
     * SQL解析器
     * 主要用来提取SQL中的表名,Where中的KEY=VALUE形式的参数
     */
    //private SqlParser parser = new DefaultSqlParser();

    private Map<String, Function<?,?>> functionsMap = new HashMap<String, Function<?,?>>();

    public ExecutePlan doRoute(String sql, Map<Integer, ParameterCommand> parameterCommand) {

        SqlType sqlType = SqlTypeUtil.getSqlType(sql);

        IDruidSqlParser parser = DruidParserFactory.getParser(sqlType);

        if (parser == null){
            return buildExecutePlanTypeNo(sql, null, sqlType);
        }

        ParseResult result = new ParseResult();

        // 初始化
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
        IPartitionTable partitionTable = null;
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
            return buildExecutePlanTypeNo(result.getSql(), null, sqlType);
        }

        // 没有计算单元 全表扫描
        if (result.getCalculateUnits() == null || result.getCalculateUnits().size() == 0){
            return buildExecutePlanTypeAll(result.getSql(), partitionTable, sqlType);
        }

        // 路由单元计算-合并
        Set<Pair<String/* targetDB */, String/* targetTable */>> nodeSet = new LinkedHashSet<Pair<String, String>>();
        for (CalculateUnit unit : result.getCalculateUnits()) {
            Pair<String/* targetDB */, String/* targetTable */> temp = partitionTable.execute(unit);
            if (temp == null){
                // 这个单元没有路由结果 需要全表扫描
                return buildExecutePlanTypeAll(result.getSql(), partitionTable, sqlType);
            }else {
                // TODO 测试相同的是否会覆盖
                nodeSet.add(temp);
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
    private ExecutePlan buildExecutePlanTypeAll(String sql, IPartitionTable partitionTable, SqlType sqlType){
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
    private ExecutePlan buildExecutePlanTypePartition(String sql, IPartitionTable partitionTable, SqlType sqlType, Set<Pair<String/* targetDB */, String/* targetTable */>> nodeSet) {
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
     * 参数排序 TODO 测试
     * @param commonds
     * @return
     */
    private List<Object> buildParameters(Map<Integer, ParameterCommand> commonds){

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

    @Override
    public void init() {
        // 1. 初始化需要被路由的表Map<String/*TableName*/, TableRule>
        // 2. 初始化自动建表程序
        for(IPartitionTable table : partitionTables){
            table.init(functionsMap);
            if(!tableRuleMapping.containsKey(table.getLogicTableName())){
                tableRuleMapping.put(table.getLogicTableName(), table);
            }else{
                throw new RuntimeException("不能对同一个逻辑表明配置过个路由规则！：" + table.getLogicTableName());
            }
        }
    }

    public void setPartitionTables(List<IPartitionTable> partitionTables) {
        this.partitionTables = partitionTables;
    }

    public void setFunctions(List<Function<?,?>> functions) {
        if(functions == null){
            return;
        }
        for(Function<?,?> f : functions){
            functionsMap.put(f.getFunctionName(), f);
        }
    }

    public Map<String, Function<?,?>> getFunctionsMap() {
        return functionsMap;
    }

}