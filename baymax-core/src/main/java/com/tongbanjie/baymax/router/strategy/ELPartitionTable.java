package com.tongbanjie.baymax.router.strategy;

import java.util.*;
import java.util.Map.Entry;
import com.tongbanjie.baymax.exception.BayMaxException;
import com.tongbanjie.baymax.router.model.CalculateUnit;
import com.tongbanjie.baymax.router.strategy.model.ELRule;
import com.tongbanjie.baymax.utils.Pair;

/**
 * 简单表分区描述
 * 
 * @author dawei
 *
 */
public class ELPartitionTable extends AbstractPartitionTable {

	/**
	 * 返回所有分区-表的映射 格式: p1-order001 p1-order002 p2-order003 p3-order004
	 * 
	 * @return
	 */
	@Override
	public List<Pair<String/* targetDB */, String/* targetTable */>> getAllTableNames() {
		List<Pair<String/* targetDB */, String/* targetTable */>> allTables = new ArrayList<Pair<String, String>>();
		Iterator<Entry<String, String>> ite = super.tableMapping.entrySet().iterator();
		while(ite.hasNext()){
			Entry<String, String> entry = ite.next();
			allTables.add(new Pair<String, String>(entry.getValue(), super.format(entry.getKey())));
		}
		return allTables;
	}


	@Override
	public Pair<String/* targetDB */, String/* targetTable */> executeRule(Map<?, ?> param) {
		String targetDB = null;
		String targetTable = null;
		if (super.rules != null) {
			for (int i = 0; i < rules.size(); i++) {
				ELRule rule = rules.get(i);
				if(!rule.checkParam(param)){
					continue;
				}
				Object ruleResult = rule.execute(param, null, super.functionsMap);
				if (Boolean.class == ruleResult.getClass() || Boolean.class.isAssignableFrom(ruleResult.getClass())) {
					// boolean
					if ((Boolean) ruleResult == false) {
						continue;
					} else {
						throw new BayMaxException("is the express return type is boolean, it must be false!" + rule);
					}
				} else if (Integer.class == ruleResult.getClass() || Integer.class.isAssignableFrom(ruleResult.getClass())) {
					// Integer
					String suffix = getTargetSuffix(super.getSuffix((Integer) ruleResult));
					targetTable = super.format(suffix);
					targetDB = getTargetPartition(suffix);
					break;// 以第一个有结果的规则为准
				} else {
					throw new BayMaxException("is the express only can return boolean or integer !" + rule);
				}
			}
		}
		return new Pair<String, String>(targetDB, targetTable);
	}
	
	protected String getTargetSuffix(String suffix){
		return suffix;
	}
	
	
	/**
	 * 把配置中的tableMapping转换为对象
	 * <p>
	 * SimpleTable能接收的参数只能是
	 * p1:01
	 * p1:02
	 * p2:03
	 * p2:04
	 * 
	 */
	@Override
	public void initTableMapping(List<String> tableMappings) {
		for (String partition : tableMappings) {
			// TODO CHECK P1:01
			String[] str = partition.trim().split(":");
			tableMapping.put(str[1].trim(), str[0].trim());
		}
	}

    @Override
    public Pair<String, String> execute(CalculateUnit units) {
        return null;
    }
}
