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
import com.tongbanjie.baymax.router.model.PartitionDescripter;
import com.tongbanjie.baymax.utils.Pair;

/**
 * 这个类是对XML配置文件解析结果的Model对象。
 * 一个对象对应XML中一个逻辑表的路由信息。
 * 他的父类{@link ITableRule}定义了一些路由规则公用的基本属性。
 * @author dawei
 *
 */
public class DefaultRule extends ITableRule implements InitializingBean{

	private static Map<String, Object> functionMap = new HashMap<String, Object>();// TODO
	
	//解析
	List<PartitionDescripter> partitionDescripters;
	
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
		for(PartitionDescripter descripter : partitionDescripters){
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
		PartitionDescripter targetDescrepter = null;
		
		List<String> dbRuleArray = getDbRuleArray();
		List<String> tbRuleArray = getTbRuleArray();
		
		//DB
		if(dbRuleArray != null){
			for(int i = 0; i<dbRuleArray.size(); i++){
				String dbRule = dbRuleArray.get(i);
				if(!parameterParepare(super.getTbRuleArrayColumns().get(i), param)){
					// 这个规则对应的EL表达式需要的参数不满足
					continue;
				}
				Double doubleResult = (Double) executeExpression(dbRule, param, Double.class);// 舍去小数,返回int
				if(doubleResult == null){
					throw new RuntimeException("cat convert this type of el result : " + doubleResult);
				}
				Integer dbIndex = doubleResult.intValue();
				targetDescrepter = partitionDescripters.get((Integer)dbIndex);
				targetDB = targetDescrepter.getPartitionName();
			}
		}
		
		//TABLE
		if(tbRuleArray != null){
			for(int i = 0; i<tbRuleArray.size(); i++){
				String tbRule = tbRuleArray.get(i);
				if(!parameterParepare(super.getTbRuleArrayColumns().get(i), param)){
					// 这个规则对应的EL表达式需要的参数不满足
					continue;
				}
				Object ruleResult = executeExpression(tbRule, param, null);
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
					break;//以第一个有结果的规则为准
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
	private <T> Object executeExpression(String expression, Object param, Class<T> toType){
		Map<String, Object> vrs = new HashMap<String, Object>();
		vrs.putAll(functionMap);//拓展函数
		vrs.put("$ROOT", param); 
		VariableResolverFactory vrfactory = new MapVariableResolverFactory(vrs);
		if(toType != null){
			return MVEL.eval(expression, param, vrfactory, toType);			
		}else{
			return MVEL.eval(expression, param, vrfactory);
		}
	}
	
	/**
	 * 判断param中是否包含一个EL表达式所需要的所有参数
	 * @param ruleColumns
	 * @param param
	 * @return
	 */
	private boolean parameterParepare(List<String> ruleColumns, Map<?,?> param){
		for(String column : ruleColumns){
			if(param.get(column) == null){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// 1. 创建每个数据分区的描述对象
		partitionDescripters = new ArrayList<PartitionDescripter>(getPartition().size());
		for(String pattern : getPartition()){
			PartitionDescripter descrepter = new PartitionDescripter(pattern);
			if(partitionDescripters.contains(descrepter)){
				throw new BayMaxException("the table pattern in the rule whid dbIndex is dunplate : " + pattern);
			}
			partitionDescripters.add(descrepter);
		}
		// 2. 把shardingKeys转换为数组
		// TODO 校验,tirm()
		setShardingColumnsArray(getShardingColumns().split(","));
		
		//3. 初始化EL表达式需要的KEY
		initELColumns();
		
		//TODO 输入校验
	}
	
	public static void main(String[] args) {
		System.out.println(new Double(0.9).intValue());
		System.out.println(new Double(1.9).intValue());
	}
	
}
