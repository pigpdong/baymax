package com.tongbanjie.baymax.router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;

import com.tongbanjie.baymax.support.Function;
import com.tongbanjie.baymax.utils.Pair;

/**
 * 这个类描述了一个EL表达式，和运行这个表达式所需要的参数
 * @author dawei
 *
 */
public class Rule {

	private String el;
	
	private List<String> argNames;
	
	/**
	 * 或取一个EL使用了ShardingKeys中的哪些字段
	 * 
	 * 1. 循环表内所有EL
	 * 2. 在一个EL中循环所有ShardingKeys
	 * 3. 如果一个ShardingKey在EL中被使用到，则加入这个EL的参数名列表
	 * 
	 * @param ruleArray
	 * @param ruleArrayColumns
	 */
	public Rule(String el, String[] shardingKesy) {
		this.el = el;
		
		argNames = new ArrayList<String>(shardingKesy.length);
		List<Pair<Integer/* startIndex */, Integer/* endIndex */>> usedColumnsIndex = new ArrayList<Pair<Integer, Integer>>();
		for (String key : shardingKesy) {
			if (findSubString(0, el, key, usedColumnsIndex)) {
				argNames.add(key);
			}
		}
	}

	/**
	 * 判断一个EL是否需要这个KEY作为参数
	 * 
	 * @param startIndex	开始搜索的下标
	 * @param source		被搜索的字符串
	 * @param column		需要命中的key
	 * @param usedColumnsIndex	这个字符串中被命中key的开始下标和结束下标
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
					// 判断这个index是否已经被使用了
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
	
	/**
	 * 判断参数是否满足运行条件
	 * @param param
	 * @return
	 */
	public boolean checkParam(Map<?, ?> param) {
		for (String column : argNames) {
			if (param.get(column) == null) {
				return false;
			}
		}
		return true;
	}

	
	/**
	 * 执行EL表达式,结果只能返回Integer或Boolean类型
	 * 
	 * @param expression	EL表达式
	 * @param param			EL表达式参数
	 * @param functionMap	函数 
	 * @return
	 */
	public <T> Object execute(Object param, Class<T> toType, Map<String, Function<?,?>> functionMap) {
		Map<String, Object> vrs = new HashMap<String, Object>();
		vrs.putAll(functionMap);// 拓展函数
		vrs.put("$ROOT", param);
		VariableResolverFactory vrfactory = new MapVariableResolverFactory(vrs);
		if (toType != null) {
			return MVEL.eval(el, param, vrfactory, toType);
		} else {
			return MVEL.eval(el, param, vrfactory);
		}
	}
	
	@Override
	public String toString() {
		return super.toString();
		// TODO
	}

	protected String getEl() {
		return el;
	}

	protected void setEl(String el) {
		this.el = el;
	}

	protected List<String> getArgNames() {
		return argNames;
	}

	protected void setArgNames(List<String> argNames) {
		this.argNames = argNames;
	}
}
