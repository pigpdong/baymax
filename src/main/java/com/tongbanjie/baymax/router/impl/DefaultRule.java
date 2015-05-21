package com.tongbanjie.baymax.router.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.springframework.beans.factory.InitializingBean;

import com.tongbanjie.baymax.exception.BayMaxException;
import com.tongbanjie.baymax.router.ITableRule;
import com.tongbanjie.baymax.router.model.TableRulePartitionDescripter;
import com.tongbanjie.baymax.utils.Pair;

public class DefaultRule extends ITableRule implements InitializingBean{

	private static Map<String, Object> functionMap = new HashMap<String, Object>();// TODO
	
	//解析
	List<TableRulePartitionDescripter> partitionDescripters;
	
	/**
	 * 返回所有分区-表的映射
	 * 格式:
	 * p1-order001
	 * p1-order002
	 * p2-order003
	 * p3-order004
	 * 
	 * @return
	 */
	@Override
	public List<Pair<String/*targetDB*/, String/*targetTable*/>> getAllTableNames(){
		List<Pair<String/*targetDB*/, String/*targetTable*/>> allTables = new ArrayList<Pair<String,String>>();
		for(TableRulePartitionDescripter descripter : partitionDescripters){
			for(String suffix : descripter.getAllTableNameSuffix()){
				allTables.add(new Pair<String, String>(descripter.getPartitionName(), this.getPhysicsTablePrefix() + suffix));
			}
		}
		return allTables;
	}
	
	@Override
	public Pair<String/*targetDB*/,String/*targetTable*/> executeRule(Map<?,?> param){
		String targetDB = null;
		String targetTable = null;
		TableRulePartitionDescripter targetDescrepter = null;
		
		List<String> dbRuleArray = getDbRuleArray();
		List<String> tbRuleArray = getTbRuleArray();
		
		//DB
		if(dbRuleArray != null){
			for(String dbRule : dbRuleArray){
				Integer dbIndex = Integer.valueOf(String.valueOf(executeExpression(dbRule, param)));
				if(dbIndex == null){
					throw new RuntimeException("cat convert this type of el result : " + dbIndex);
				}
				targetDescrepter = partitionDescripters.get((Integer)dbIndex);
				targetDB = targetDescrepter.getPartitionName();
			}
		}
		
		//TABLE
		if(tbRuleArray != null){
			for(String tbRule : tbRuleArray){
				Object ruleResult = executeExpression(tbRule, param);
				if(Boolean.class == ruleResult.getClass() || Boolean.class.isAssignableFrom(ruleResult.getClass())){
					//boolean
					if((Boolean)ruleResult == false){
						continue;
					}else{
						throw new BayMaxException("is the express return type is boolean, it must be false!" + tbRule);
					}
				}else if(Integer.class == ruleResult.getClass() || Integer.class.isAssignableFrom(ruleResult.getClass())){
					//Integer
					String suffix = targetDescrepter.getSuffix((Integer)ruleResult);
					targetTable = getPhysicsTablePrefix() + suffix;
				}else{
					throw new BayMaxException("is the express only can return boolean or integer !" + tbRule);
				}
			}
		}
		return new Pair<String, String>(targetDB, targetTable);
	}
	
	/**
	 * 执行EL表达式,结果只能返回Integer或Boolean类型
	 * @param expression
	 * @param param
	 * @return
	 */
	private Object executeExpression(String expression, Object param){
		Map<String, Object> vrs = new HashMap<String, Object>();
		vrs.putAll(functionMap);//拓展函数
		vrs.put("$ROOT", param); 
		VariableResolverFactory vrfactory = new MapVariableResolverFactory(vrs);
		return MVEL.eval(expression, param, vrfactory);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		partitionDescripters = new ArrayList<TableRulePartitionDescripter>(getPartition().size());
		for(String pattern : getPartition()){
			TableRulePartitionDescripter descrepter = new TableRulePartitionDescripter(pattern);
			if(partitionDescripters.contains(descrepter)){
				throw new BayMaxException("the table pattern in the rule whid dbIndex is dunplate : " + pattern);
			}
			partitionDescripters.add(descrepter);
		}
		//TODO 输入校验
	}
	
}
