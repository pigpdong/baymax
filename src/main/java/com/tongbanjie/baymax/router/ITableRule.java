package com.tongbanjie.baymax.router;

import java.util.List;
import java.util.Map;

import com.tongbanjie.baymax.utils.Pair;

public abstract class ITableRule {

	/**
	 * 路由方法
	 * @param param 更具{@link shardingKeys}从SQL中提取的K=V参数
	 * @return
	 */
	public abstract Pair<String/*targetDB*/, String/*targetTable*/> executeRule(Map<?, ?> param);
	
	/**
	 * 获取所有分区-表的对应
	 * @return
	 */
	public abstract List<Pair<String, String>> getAllTableNames();
	
	//配置
	private String logicTableName;
	//配置
	private String physicsTablePrefix;
	//配置
	private List<String> partition;
	//配置
	private List<String> dbRuleArray;
	//配置
	private List<String> tbRuleArray;
	//配置
	private List<String> shardingKeys;//TODO for use
	
	public String getLogicTableName() {
		return logicTableName;
	}
	public void setLogicTableName(String logicTableName) {
		this.logicTableName = logicTableName;
	}
	public String getPhysicsTablePrefix() {
		return physicsTablePrefix;
	}
	public void setPhysicsTablePrefix(String physicsTablePrefix) {
		this.physicsTablePrefix = physicsTablePrefix;
	}
	public List<String> getPartition() {
		return partition;
	}
	public void setPartition(List<String> partition) {
		this.partition = partition;
	}
	public List<String> getDbRuleArray() {
		return dbRuleArray;
	}
	public void setDbRuleArray(List<String> dbRuleArray) {
		this.dbRuleArray = dbRuleArray;
	}
	public List<String> getTbRuleArray() {
		return tbRuleArray;
	}
	public void setTbRuleArray(List<String> tbRuleArray) {
		this.tbRuleArray = tbRuleArray;
	}
	public List<String> getShardingKeys() {
		return shardingKeys;
	}
	public void setShardingKeys(List<String> shardingKeys) {
		this.shardingKeys = shardingKeys;
	}
}
