package com.tongbanjie.baymax.router.strategy;

import java.util.List;

/**
 * Created by sidawei on 16/3/20.
 */
public class PartitionTableMetaData extends PartitionTableNodeMapping{

    /*---------------------------------------------------------------*/

    protected String logicTableName;		    // 逻辑表明

    protected String namePatten;

    protected boolean disableFullScan;		    // 关闭全表扫描

    protected List<PartitionRule> rules;		// 路由规则

    /*---------------------------------------------------------------*/
    protected String[] partitionColumns;		// 分区键

    protected String prefix;				    // 物理表明格式化模式trade_order_

    protected int suffixLength;				    // 后缀的位数

    /*---------------------------------------------------------------*/


    protected String getTargetPartition(String suffix){
        return tableMapping.get(suffix);
    }

    public String format(String suffix){
        return prefix + suffix;
    }

    /**
     * 根据int类型的suffix获取固定长度的完整表明后缀。
     * @param suffix
     * @return
     */
    public String getSuffix(int suffix){
        // TODO init suffixlength
        String sfx = String.valueOf(suffix);
        if(sfx.length() > suffixLength){
            throw new RuntimeException("suffix is too long then config "+suffix);
        }
        while(sfx.length() < suffixLength){
            sfx = "0"+sfx;
        }
        return sfx;
    }


    /*--------------------------------init-------------------------------*/

    //trade_order_{00}
    public void initPatten() {
        // TODO 校验
        int start = namePatten.indexOf("{");
        int end = namePatten.indexOf("}");
        this.prefix = namePatten.substring(0, start);
        this.suffixLength = namePatten.substring(start+1, end).length();
    }

    protected void initRules(){
        if(rules == null || rules.size() == 0){
            throw new RuntimeException(String.format("rules must not be empty! strategy{%s}", logicTableName));
        }
        partitionColumns = new String[rules.size()];
        for (int i = 0; i < rules.size(); i++) {
            partitionColumns[i] = rules.get(i).getColumn();
        }
        if(this.partitionColumns == null || this.partitionColumns.length == 0){
            throw new RuntimeException(String.format("partitionColumns must not be empty! strategy{%s}", this.logicTableName));
        }
    }

    /*---------------------------------get set------------------------------*/
    public String getLogicTableName() {
        return logicTableName;
    }

    public void setLogicTableName(String logicTableName) {
        this.logicTableName = logicTableName;
    }

    public String getNamePatten() {
        return namePatten;
    }

    public void setNamePatten(String namePatten) {
        this.namePatten = namePatten;
        // init
        initPatten();
    }

    public boolean isDisableFullScan() {
        return disableFullScan;
    }

    public void setDisableFullScan(boolean disableFullScan) {
        this.disableFullScan = disableFullScan;
    }

    public List<PartitionRule> getRules() {
        return rules;
    }

    public void setRules(List<PartitionRule> rules) {
        this.rules = rules;
        // init
        initRules();
    }

    public String[] getPartitionColumns() {
        return partitionColumns;
    }
}
