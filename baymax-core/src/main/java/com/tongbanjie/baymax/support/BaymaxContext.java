package com.tongbanjie.baymax.support;

import com.tongbanjie.baymax.router.strategy.IPartitionTable;

import java.util.*;

/**
 * Created by sidawei on 16/1/29.
 */
public class BaymaxContext {

    /**
     * 上下文中所有的路由规则列表
     */
    private static List<IPartitionTable> partitionTables;

    /**
     * 上下文中所有路由规则的MAP，方便使用表名查找到对应的路由规则
     */
    private static Map<String/*TableName*/, IPartitionTable> tableRuleMapping = new HashMap<String, IPartitionTable>();

    /**
     * SQL解析器
     * 主要用来提取SQL中的表名,Where中的KEY=VALUE形式的参数
     */
    //private SqlParser parser = new DefaultSqlParser();

    private static Map<String, Function<?,?>> functionsMap = new HashMap<String, Function<?,?>>();

    public static boolean isPartitionTable(String logicTableName){
        return tableRuleMapping.containsKey(logicTableName);
    }

    public static IPartitionTable getPartitionTable(String logicTableName){
        return tableRuleMapping.get(logicTableName);
    }

    public static String[] getPartitionKeys(String logicTableName){
        return tableRuleMapping.get(logicTableName).getShardingKeys();
    }

    public static Map<String, Function<?,?>> getFunctionsMap() {
        return functionsMap;
    }

    /**
     * 初始化
     */
    public static void init() {
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

    /*------------------------------------------初始化必要参数-------------------------------------------*/

    public static void setPartitionTables(List<IPartitionTable> partitionTables) {
        BaymaxContext.partitionTables = partitionTables;
    }

    public static void setFunctions(List<Function<?,?>> functions) {
        if(functions == null){
            return;
        }
        for(Function<?,?> f : functions){
            BaymaxContext.functionsMap.put(f.getFunctionName(), f);
        }
    }
}
