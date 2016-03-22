package com.tongbanjie.baymax.support;

import com.tongbanjie.baymax.exception.BayMaxException;
import com.tongbanjie.baymax.router.strategy.PartitionTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sidawei on 16/1/29.
 */
public class BaymaxContext {

    private static boolean isInit = false;

    /**
     * 上下文中所有的路由规则列表
     */
    private static List<PartitionTable> partitionTables;

    /**
     * 上下文中所有路由规则的MAP，方便使用表名查找到对应的路由规则
     */
    private static Map<String/*TableName*/, PartitionTable> tableRuleMapping = new HashMap<String, PartitionTable>();

    /**
     * SQL解析器
     * 主要用来提取SQL中的表名,Where中的KEY=VALUE形式的参数
     */
    //private SqlParser parser = new DefaultSqlParser();

    //private static Map<String, ElFunction<?,?>> functionsMap = new HashMap<String, ElFunction<?,?>>();

    public static boolean isPartitionTable(String logicTableName){
        return tableRuleMapping.containsKey(logicTableName);
    }

    public static PartitionTable getPartitionTable(String logicTableName){
        return tableRuleMapping.get(logicTableName);
    }

    public static String[] getPartitionColumns(String logicTableName){
        return tableRuleMapping.get(logicTableName).getPartitionColumns();
    }

    /**
     * 初始化
     */
    public static synchronized void init() {
        if (isInit){
            throw new BayMaxException("Baymax Has been initialized");
        }
        // 1. 初始化需要被路由的表Map<String/*TableName*/, TableRule>
        // 2. 初始化自动建表程序
        for(PartitionTable table : partitionTables){
            if(!tableRuleMapping.containsKey(table.getLogicTableName())){
                tableRuleMapping.put(table.getLogicTableName(), table);
            }else{
                throw new RuntimeException("不能对同一个逻辑表明配置过个路由规则！：" + table.getLogicTableName());
            }
        }
        isInit = true;
    }

    /*------------------------------------------初始化必要参数-------------------------------------------*/

    public static void setTables(List<PartitionTable> partitionTables) {
        BaymaxContext.partitionTables = partitionTables;
    }

}
