package com.tongbanjie.baymax.test;

import com.tongbanjie.baymax.router.ColumnProcess;
import com.tongbanjie.baymax.router.strategy.*;
import com.tongbanjie.baymax.router.strategy.function.ELFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sidawei on 16/3/21.
 */
public class TableBuilder {
    List<PartitionTable>    tables          = new ArrayList<PartitionTable>();
    PartitionTable          concurrentTable = null;
    PartitionTableRule      rule            = null;
    List<PartitionColumn>   columns         = null;


    public TableBuilder appenTable(String tableName, String tableNamePatten, String function){
        // table
        concurrentTable = new PartitionTable();
        concurrentTable.setLogicTableName(tableName);
        concurrentTable.setNamePatten(tableNamePatten);
        //tables
        tables.add(concurrentTable);

        // node mapping
        List<String> nodeMapping = new ArrayList<String>();
        nodeMapping.add("p1:0,1,2,3");
        concurrentTable.setNodeMapping(new SimpleTableNodeMapping(nodeMapping));

        rule = new PartitionTableRule();
        ELFunction func = new ELFunction();
        func.setExpression(function);
        rule.setFunction(func);
        concurrentTable.setRule(rule);

        columns = new ArrayList<PartitionColumn>();
        rule.setColumns(columns);

        return this;
    }

    public TableBuilder appendColumn(String column, ColumnProcess process){
        columns.add(new PartitionColumn(column, process));
        return this;
    }

    public PartitionTable toTable(){
        return this.tables.get(0);
    }

    public List<PartitionTable> toTables(){
        return this.tables;
    }

}
