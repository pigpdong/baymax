package com.tongbanjie.baymax.test;

import com.tongbanjie.baymax.router.strategy.PartitionRule;
import com.tongbanjie.baymax.router.strategy.PartitionTable;
import com.tongbanjie.baymax.router.strategy.rule.ELRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sidawei on 16/3/21.
 */
public class TableBuilder {
    List<PartitionTable>    tables          = new ArrayList<PartitionTable>();
    PartitionTable          concurrentTable = null;
    List<PartitionRule>     rules           = null;

    public TableBuilder appenTable(String tableName, String tableNamePatten){
        // table
        concurrentTable = new PartitionTable();
        concurrentTable.setLogicTableName(tableName);
        concurrentTable.setNamePatten(tableNamePatten);

        // rules
        rules = new ArrayList<PartitionRule>();

        //tables
        tables.add(concurrentTable);

        // node mapping
        List<String> nodeMapping = new ArrayList<String>();
        nodeMapping.add("p1:0");
        nodeMapping.add("p1:1");
        nodeMapping.add("p1:2");
        nodeMapping.add("p1:3");
        concurrentTable.setNodeMapping(nodeMapping);

        return this;
    }

    public TableBuilder appendELRule(String column, String elExpiress){
        ELRule rule = new ELRule();
        rule.setColumn(column);
        rule.setExpression(elExpiress);
        rules.add(rule);
        concurrentTable.setRules(rules);
        return this;
    }

    public PartitionTable toTable(){
        return this.tables.get(0);
    }

    public List<PartitionTable> toTables(){
        return this.tables;
    }

}
