package com.tongbanjie.baymax.router;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.tongbanjie.baymax.utils.Pair;

/**
 * 路由规则的公共抽象
 * {@link DefaultRule}默认实现了基于Spring XML配置文件配置的路由规则.
 * 如果你的路由规则很复杂,不能再XML配置文件中定义,那么你可以继承这个类并实现抽象方法,让后把你实现了路由规则器注册到Spring上下文（可用配置或@Service等）。
 * BayMax上下文会自动发现你注册的路由规则器并在执行对应TableName的SQL时使用你配置的路由规则器。
 * 一个TableName只能注册一个对应的路由规则器，后注册的会把先注册的替换掉，应为规则器会使用TableName作为KEY保存在HashMap中。、
 * 
 * @author dawei
 *
 */
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
	private String shardingColumns;//user_id,id
	
	/**
	 * 解析
	 * 对应{@link dbRuleArray}需要的分区列
	 * @noset
	 */
	private List<List<String/*column*/>> dbRuleArrayColumns;
	
	/**
	 * 解析
	 * 对应{@link tbRuleArray}需要的分区列
	 * @noset
	 */
	private List<List<String/*column*/>> tbRuleArrayColumns;
	
	/**
	 * 解析{@link shardingKeys}
	 */
	private String[] shardingColumnsArray;
	
	/**
	 * 初始化EL表达式中需要的Columns
	 */
	public void initELColumns(){
		Arrays.sort(shardingColumnsArray, new Comparator<String>(){
			@Override
			public int compare(String o1, String o2) {
				if(o1.length() < o2.length()){
					return 1;
				}else if(o1.length() > o2.length()){
					return -1;
				}else{
					return 0;
				}
			}
			
		});//排序,比较长的列先匹配
		
		//处理dbrule
		dbRuleArrayColumns = new ArrayList<List<String/*column*/>>();
		initELColumns(dbRuleArray, dbRuleArrayColumns);
		
		//处理tbrule
		tbRuleArrayColumns = new ArrayList<List<String/*column*/>>();
		initELColumns(tbRuleArray, tbRuleArrayColumns);
	}
	
	/**
	 * 初始化一个EL表达式List对应的需要的Column的List
	 * @param ruleArray
	 * @param ruleArrayColumns
	 */
	private void initELColumns(List<String> ruleArray, List<List<String>> ruleArrayColumns){
		List<Pair<Integer/*startIndex*/, Integer/*endIndex*/>> usedColumnsIndex = new ArrayList<Pair<Integer, Integer>>();
		for(int i = 0; i< ruleArray.size(); i++){
			// 1. 循环所有EL
			usedColumnsIndex.clear();
			String rule = ruleArray.get(i);
			List<String> ruleColumns = new ArrayList<String>();
			ruleArrayColumns.add(ruleColumns);
			for(String column : shardingColumnsArray){
				// 2. 循环所有列
				boolean findColumn = findSubString(0, rule, column, usedColumnsIndex);
				if(findColumn){
					ruleColumns.add(column);
				}
			}
		}
	}
	
	/**
	 * 判断一个column是否在一个EL表达式中被用到
	 * @param startIndex
	 * @param source
	 * @param column
	 * @param usedColumnsIndex
	 * @return
	 */
	private boolean findSubString(int startIndex, String source, String column, List<Pair<Integer/*startIndex*/, Integer/*endIndex*/>> usedColumnsIndex){
		boolean result = false;
		int index = source.indexOf(column, startIndex);
		int usedIndexEnd = 0;
		if(index != -1){
			boolean isUsed = false;
			for(Pair<Integer, Integer> indexRange : usedColumnsIndex){
				if(index >= indexRange.getObject1() && index <= indexRange.getObject2()){
					// 3. 判断这个index是否已经被使用了
					isUsed = true;
					usedIndexEnd = indexRange.getObject2();
					break;
				}
			}
			if(isUsed && usedIndexEnd < source.length() - 1){
				return findSubString(usedIndexEnd+1, source, column, usedColumnsIndex);
			}else{
				// EL中需要这列
				result = true;
				// 这个列所在的范围标记为不可用
				usedColumnsIndex.add(new Pair<Integer, Integer>(index, index+column.length()-1));
			}
		}
		return result;
	}
	
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

	public String getShardingColumns() {
		return shardingColumns;
	}

	public void setShardingColumns(String shardingColumns) {
		this.shardingColumns = shardingColumns;
	}

	public String[] getShardingColumnsArray() {
		return shardingColumnsArray;
	}

	public void setShardingColumnsArray(String[] shardingColumnsArray) {
		this.shardingColumnsArray = shardingColumnsArray;
	}

	public List<List<String>> getDbRuleArrayColumns() {
		return dbRuleArrayColumns;
	}

	public List<List<String>> getTbRuleArrayColumns() {
		return tbRuleArrayColumns;
	}

}
