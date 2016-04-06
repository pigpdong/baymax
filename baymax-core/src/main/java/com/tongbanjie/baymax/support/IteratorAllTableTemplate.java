package com.tongbanjie.baymax.support;

import com.tongbanjie.baymax.utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sidawei on 16/4/5.
 */
public class IteratorAllTableTemplate {

    public static void iterator(String tableName, Callbal call){
        iterator(tableName, call, 1);
    }

    public static void iterator(String tableName, Callbal call, int iteratorTimes){
        List<Pair<String/* targetDB */, String/* targetTable */>> tables = ManualRoute.getAllTables(tableName);
        if (tables == null || tables.size() == 0){
            return;
        }
        // 获取所有表名
        for (int time = 0; time < iteratorTimes; time++) {
            for (int i = 0; i < tables.size(); i++) {
                Pair<String/* targetDB */, String/* targetTable */> table = tables.get(i);
                // TODO PUT TO THREAD LOCAL
                call.call(table.getObject2());
                // TODO CLEAR THREAD LOCAL
            }
        }
    }


    interface Callbal{
        void call(String tableName);
    }

    public void test(){
        final List<Object> result = new ArrayList<Object>();
        IteratorAllTableTemplate.iterator("trade_order", new Callbal() {
            @Override
            public void call(String tableName) {
                Object data = null; // select * from trade_order where xxxx
                result.add(data);
            }
        });
    }
}




