package com.tongbanjie.baymax.router;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tongbanjie.baymax.router.model.Rule;
import com.tongbanjie.baymax.support.Function;
import com.tongbanjie.baymax.support.TableCreater;
import com.tongbanjie.baymax.utils.Pair;

/**
 * 基础Table描述
 * <p>
 * 子类必须实现{@code executeRule()} {@code getAllTableNames()}方法
 * <p>
 * 这个类描述了对某个特定逻辑表的分区规则
 * @author dawei
 *
 */
public abstract class TableRule {
	
	protected ConfigHolder configHolder = new ConfigHolder();

	protected String logicTableName;		// 逻辑表明

	protected String[] shardingKeys;		// 分区键
		
	protected String prefix;				// 物理表明格式化模式trade_order_
	
	protected int suffixLength;				// 后缀的位数 
	
	protected boolean disableFullScan;		// 关闭全表扫描
	
	protected List<Rule> rules;				// 路由规则
	
	protected Map<String/*suffix*/, String/*partition*/> tableMapping = new ConcurrentHashMap<String, String>(); 	// 所有表到分区的映射
	
	protected String autoCreatePartition;	// 自动建表分区 000000所在分区
	
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
	
	protected Map<String, Function<?,?>> functionsMap;
	
	protected String getTargetPartition(String suffix){
		return tableMapping.get(suffix);
	}
	
	public String format(String suffix){
		return prefix + suffix;
	}

	public String getLogicTableName() {
		return logicTableName;
	}

	public void setLogicTableName(String logicTableName) {
		this.logicTableName = logicTableName;
	}

	public String[] getShardingKeys() {
		return shardingKeys;
	}

	public void setShardingKeys(String shardingKeysStr) {
		// TODO 校验
		this.shardingKeys = shardingKeysStr.split(",");
	}

	//trade_order_{00}
	public void setPatten(String patten) {
		// TODO 校验
		int start = patten.indexOf("{");
		int end = patten.indexOf("}");
		this.prefix = patten.substring(0, start);
		this.suffixLength = patten.substring(start+1, end).length();
	}

	public boolean isDisableFullScan() {
		return disableFullScan;
	}

	public void setDisableFullScan(boolean disableFullScan) {
		this.disableFullScan = disableFullScan;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<String> rules) {
		configHolder.rules = rules;
	}

	public void setTableMapping(List<String> tableMappings){
		configHolder.tableMappings = tableMappings;
	}
	
	public void init(Map<String, Function<?,?>> functionsMap) {
		/*------------------------------init------------------------------*/
		this.functionsMap = functionsMap;
		init();
	}
	
	public void init(){
		initRules(configHolder.rules);
		initTableMapping(configHolder.tableMappings);
	}
	
	protected void initRules(List<String> rules){
		if(rules == null || rules.size() == 0){
			throw new RuntimeException(String.format("rules must not be empty! table{%s}", logicTableName));
		}
		this.rules = new ArrayList<Rule>(rules.size());
		if(this.shardingKeys == null || this.shardingKeys.length == 0){
			throw new RuntimeException(String.format("shardingKeys must not be empty! table{%s}", this.logicTableName));
		}
		Arrays.sort(this.shardingKeys, new Comparator<String>() {
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

		for(String ruleStr : rules){
			this.rules.add(new Rule(ruleStr, this.shardingKeys));
		}
	}
	
	protected abstract void initTableMapping(List<String> rules);
	
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
	
	public TableCreater getTableCreater(){
		return null;
	}

	
	private class ConfigHolder{
		public List<String> rules;
		public List<String> tableMappings;
	}
}
