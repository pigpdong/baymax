package com.tongbanjie.baymax.router.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.springframework.beans.factory.InitializingBean;

import com.tongbanjie.baymax.exception.BayMaxException;
import com.tongbanjie.baymax.router.PartitionMetaData;
import com.tongbanjie.baymax.router.TableRule;
import com.tongbanjie.baymax.support.Functions;
import com.tongbanjie.baymax.utils.Pair;

/**
 * 这个类是对XML配置文件解析结果的Model对象。 一个对象对应XML中一个逻辑表的路由信息。 他的父类{@link TableRule}
 * 定义了一些路由规则公用的基本属性。
 * 
 * @author dawei
 *
 */
public class DefaultTableRule extends TableRule implements InitializingBean {

	private static Map<String, Object> functionMap;

	// 配置
	protected String physicsTablePrefix;
	// 配置
	protected List<String> partition;
	// 配置
	protected List<String> dbRuleArray;
	// 配置
	protected List<String> tbRuleArray;
	// 配置
	protected String shardingColumns;// user_id,id

	/**
	 * 解析 对应{@link dbRuleArray}需要的分区列
	 * 
	 * @noset
	 */
	private List<List<String/* column */>> dbRuleArrayColumns;

	/**
	 * 解析 对应{@link tbRuleArray}需要的分区列
	 * 
	 * @noset
	 */
	private List<List<String/* column */>> tbRuleArrayColumns;

	static {
		if (functionMap == null) {
			functionMap = new HashMap<String, Object>();// TODO
			functionMap.put("func", new Functions());
		}
	}

	// 解析
	List<PartitionMetaData> partitionMetaData;

	/**
	 * 返回所有分区-表的映射 格式: p1-order001 p1-order002 p2-order003 p3-order004
	 * 
	 * @return
	 */
	@Override
	public List<Pair<String/* targetDB */, String/* targetTable */>> getAllTableNames() {
		List<Pair<String/* targetDB */, String/* targetTable */>> allTables = new ArrayList<Pair<String, String>>();
		for (PartitionMetaData descripter : partitionMetaData) {
			for (String suffix : descripter.getAllTableNameSuffix()) {
				allTables.add(new Pair<String, String>(descripter.getPartitionName(), physicsTablePrefix + suffix));
			}
		}
		return allTables;
	}

	@Override
	public Pair<String/* targetDB */, String/* targetTable */> executeRule(Map<?, ?> param) {
		String targetDB = null;
		String targetTable = null;
		PartitionMetaData targetDescrepter = null;

		// DB
		if (dbRuleArray != null) {
			for (int i = 0; i < dbRuleArray.size(); i++) {
				String dbRule = dbRuleArray.get(i);
				if (!parameterParepare(dbRuleArrayColumns.get(i), param)) {
					// 这个规则对应的EL表达式需要的参数不满足
					continue;
				}
				Double doubleResult = (Double) executeExpression(dbRule, param, Double.class);// 舍去小数,返回int
				if (doubleResult == null) {
					throw new RuntimeException("cat convert this type of el result : " + doubleResult);
				}
				Integer dbIndex = doubleResult.intValue();
				targetDescrepter = partitionMetaData.get((Integer) dbIndex);
				targetDB = targetDescrepter.getPartitionName();
			}
		}

		// TABLE
		if (tbRuleArray != null) {
			for (int i = 0; i < tbRuleArray.size(); i++) {
				String tbRule = tbRuleArray.get(i);
				if (!parameterParepare(tbRuleArrayColumns.get(i), param)) {
					// 这个规则对应的EL表达式需要的参数不满足
					continue;
				}
				Object ruleResult = executeExpression(tbRule, param, null);
				if (Boolean.class == ruleResult.getClass() || Boolean.class.isAssignableFrom(ruleResult.getClass())) {
					// boolean
					if ((Boolean) ruleResult == false) {
						continue;
					} else {
						throw new BayMaxException("is the express return type is boolean, it must be false!" + tbRule);
					}
				} else if (Integer.class == ruleResult.getClass() || Integer.class.isAssignableFrom(ruleResult.getClass())) {
					// Integer
					String suffix = targetDescrepter.getSuffix((Integer) ruleResult);
					targetTable = physicsTablePrefix + suffix;
					break;// 以第一个有结果的规则为准
				} else {
					throw new BayMaxException("is the express only can return boolean or integer !" + tbRule);
				}
			}
		}
		return new Pair<String, String>(targetDB, targetTable);
	}

	/**
	 * 执行EL表达式,结果只能返回Integer或Boolean类型
	 * 
	 * @param expression
	 * @param param
	 * @return
	 */
	private <T> Object executeExpression(String expression, Object param, Class<T> toType) {
		Map<String, Object> vrs = new HashMap<String, Object>();
		vrs.putAll(functionMap);// 拓展函数
		vrs.put("$ROOT", param);
		VariableResolverFactory vrfactory = new MapVariableResolverFactory(vrs);
		if (toType != null) {
			return MVEL.eval(expression, param, vrfactory, toType);
		} else {
			return MVEL.eval(expression, param, vrfactory);
		}
	}

	/**
	 * 判断param中是否包含一个EL表达式所需要的所有参数
	 * 
	 * @param ruleColumns
	 * @param param
	 * @return
	 */
	private boolean parameterParepare(List<String> ruleColumns, Map<?, ?> param) {
		for (String column : ruleColumns) {
			if (param.get(column) == null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void init() throws Exception {
		// 1. 创建每个数据分区的描述对象
		partitionMetaData = new ArrayList<PartitionMetaData>(partition.size());
		for (String pattern : partition) {
			PartitionMetaData descrepter = new PartitionMetaData(pattern);
			if (partitionMetaData.contains(descrepter)) {
				throw new BayMaxException("the table pattern in the rule whid dbIndex is dunplate : " + pattern);
			}
			partitionMetaData.add(descrepter);
		}
		// 2. 把shardingKeys转换为数组
		// TODO 校验,tirm()
		setShardingColumnsArray(shardingColumns.split(","));

		// 3. 初始化EL表达式需要的KEY
		initELColumns();

		// TODO 输入校验
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}

	/**
	 * 初始化EL表达式中需要的Columns
	 */
	public void initELColumns() {
		Arrays.sort(super.getShardingColumnsArray(), new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (o1.length() < o2.length()) {
					return 1;
				} else if (o1.length() > o2.length()) {
					return -1;
				} else {
					return 0;
				}
			}

		});// 排序,比较长的列先匹配,便于字符串匹配提取,user_id把id匹配掉了,再用id来匹配中不行了

		// 处理dbrule需要的列
		dbRuleArrayColumns = new ArrayList<List<String/* column */>>();
		initELColumns(dbRuleArray, dbRuleArrayColumns);

		// 处理tbrule需要的列
		tbRuleArrayColumns = new ArrayList<List<String/* column */>>();
		initELColumns(tbRuleArray, tbRuleArrayColumns);
	}

	/**
	 * 初始化一个EL表达式List对应的需要的Column的List
	 * 
	 * @param ruleArray
	 * @param ruleArrayColumns
	 */
	private void initELColumns(List<String> ruleArray, List<List<String>> ruleArrayColumns) {
		List<Pair<Integer/* startIndex */, Integer/* endIndex */>> usedColumnsIndex = new ArrayList<Pair<Integer, Integer>>();
		for (int i = 0; i < ruleArray.size(); i++) {
			// 1. 循环所有EL
			usedColumnsIndex.clear();
			String rule = ruleArray.get(i);
			List<String> ruleColumns = new ArrayList<String>();
			ruleArrayColumns.add(ruleColumns);
			for (String column : super.getShardingColumnsArray()) {
				// 2. 循环所有列
				boolean findColumn = findSubString(0, rule, column, usedColumnsIndex);
				if (findColumn) {
					ruleColumns.add(column);
				}
			}
		}
	}

	/**
	 * 判断一个column是否在一个EL表达式中被用到
	 * 
	 * @param startIndex
	 * @param source
	 * @param column
	 * @param usedColumnsIndex
	 * @return
	 */
	private boolean findSubString(int startIndex, String source, String column, List<Pair<Integer/* startIndex */, Integer/* endIndex */>> usedColumnsIndex) {
		boolean result = false;
		int index = source.indexOf(column, startIndex);
		int usedIndexEnd = 0;
		if (index != -1) {
			boolean isUsed = false;
			for (Pair<Integer, Integer> indexRange : usedColumnsIndex) {
				if (index >= indexRange.getObject1() && index <= indexRange.getObject2()) {
					// 3. 判断这个index是否已经被使用了
					isUsed = true;
					usedIndexEnd = indexRange.getObject2();
					break;
				}
			}
			if (isUsed && usedIndexEnd < source.length() - 1) {
				return findSubString(usedIndexEnd + 1, source, column, usedColumnsIndex);
			} else {
				// EL中需要这列
				result = true;
				// 这个列所在的范围标记为不可用
				usedColumnsIndex.add(new Pair<Integer, Integer>(index, index + column.length() - 1));
			}
		}
		return result;
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

	public String getShardingColumns() {
		return shardingColumns;
	}

	public void setShardingColumns(String shardingColumns) {
		this.shardingColumns = shardingColumns;
	}

	public List<List<String>> getDbRuleArrayColumns() {
		return dbRuleArrayColumns;
	}

	public void setDbRuleArrayColumns(List<List<String>> dbRuleArrayColumns) {
		this.dbRuleArrayColumns = dbRuleArrayColumns;
	}

	public List<List<String>> getTbRuleArrayColumns() {
		return tbRuleArrayColumns;
	}

	public void setTbRuleArrayColumns(List<List<String>> tbRuleArrayColumns) {
		this.tbRuleArrayColumns = tbRuleArrayColumns;
	}

	public List<PartitionMetaData> getPartitionMetaData() {
		return partitionMetaData;
	}

	public void setPartitionMetaData(List<PartitionMetaData> partitionMetaData) {
		this.partitionMetaData = partitionMetaData;
	}

}
