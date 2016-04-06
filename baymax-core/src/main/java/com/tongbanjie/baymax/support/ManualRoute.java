package com.tongbanjie.baymax.support;

import com.tongbanjie.baymax.utils.Pair;

import java.util.List;
import java.util.Map;

/**
 * Created by sidawei on 16/4/2.
 *
 * 这个类用来实现Dao层的半自动路由
 *
 */
public class ManualRoute {

    /**
     * 返回路由结果
     * @param tableName
     * @param parameters
     * @return
     */
    public static List<Pair<String/* targetDB */, String/* targetTable */>> route(String tableName, Map<String, Object> parameters){
        return null;
    }

    /**
     *
     * 返回所有的表
     * @param tableName
     * @return
     */
    public static List<Pair<String/* targetDB */, String/* targetTable */>> getAllTables(String tableName){
        return null;
    }

}
