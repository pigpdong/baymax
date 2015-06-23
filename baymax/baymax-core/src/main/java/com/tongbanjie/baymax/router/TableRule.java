package com.tongbanjie.baymax.router;

import java.util.List;
import java.util.Map;

import com.tongbanjie.baymax.utils.Pair;

/**
 * 路由规则的公共抽象 {@link DefaultTableRule}默认实现了基于Spring XML配置文件配置的路由规则.
 * 如果你的路由规则很复杂,不能再XML配置文件中定义
 * ,那么你可以继承这个类并实现抽象方法,让后把你实现了路由规则器注册到Spring上下文（可用配置或@Service等）。
 * BayMax上下文会自动发现你注册的路由规则器并在执行对应TableName的SQL时使用你配置的路由规则器。
 * 一个TableName只能注册一个对应的路由规则器，后注册的会把先注册的替换掉，应为规则器会使用TableName作为KEY保存在HashMap中。、
 * 
 * @author dawei
 *
 */
public abstract class TableRule {

	// 配置
	protected String logicTableName;

	/**
	 * 解析{@link shardingKeys}
	 */
	private String[] shardingColumnsArray;

	/**
	 * 路由方法
	 * 
	 * @param param
	 *            更具{@link shardingKeys}从SQL中提取的K=V参数
	 * @return
	 */
	public abstract Pair<String/* targetDB */, String/* targetTable */> executeRule(Map<?, ?> param);

	/**
	 * 获取所有分区-表的对应
	 * 
	 * @return
	 */
	public abstract List<Pair<String/* partion */, String/* table */>> getAllTableNames();

	public String getLogicTableName() {
		return logicTableName;
	}

	public void setLogicTableName(String logicTableName) {
		this.logicTableName = logicTableName;
	}

	public String[] getShardingColumnsArray() {
		return shardingColumnsArray;
	}

	public void setShardingColumnsArray(String[] shardingColumnsArray) {
		this.shardingColumnsArray = shardingColumnsArray;
	}

	public void init() throws Exception {
		
	}

}
