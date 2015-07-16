package com.tongbanjie.baymax.router.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 在{@link SimpleTable}之上映射一层虚拟表
 * 
 * @author dawei
 *
 */
public class VirtualTable extends SimpleTable{
	
	private final static Logger logger = LoggerFactory.getLogger(VirtualTable.class);
	
	private Map<String/*virtualSuffix*/, String/*phySuffix*/> virtualTables;// 所有虚拟表 el表达式直接匹配的表
	
	@Override
	public void init() {
		super.init();
		
		virtualTables = new HashMap<String, String>();
		
		List<TableNode> phyTables = new LinkedList<TableNode>();// 物理表列表用于计算
		
		Iterator<Entry<String, String>> ite = super.tableMapping.entrySet().iterator();
		while(ite.hasNext()){
			Entry<String, String> entry = ite.next();
			phyTables.add(new TableNode(Integer.valueOf(entry.getKey()), entry.getValue()));// 构物理表列表
		}
		// 物理表排序
		Collections.sort(phyTables, new Comparator<TableNode>() {
			@Override
			public int compare(TableNode o1, TableNode o2) {
				if(o1.suffix > o2.suffix){
					return 1;
				}else if(o1.suffix < o2.suffix){
					return -1;
				}else{
					return 0;
				}
			}
		});
		
		// 构建虚拟表
		int i = 0;
		for(TableNode node : phyTables){
			for(; i<=node.suffix; i++){
				virtualTables.put(super.getSuffix(i), super.getSuffix(node.suffix));
			}
		}
		if(logger.isDebugEnabled()){
			logger.debug("==> VirtualTable("+super.logicTableName+"):" + virtualTables);
		}
	}
	
	/**
	 * 虚拟表后缀转换为物理表后缀
	 */
	@Override
	protected String getTargetSuffix(String suffix) {
		return virtualTables.get(suffix);
	}
	
	/**
	 * 物理表节点
	 * @author dawei
	 *
	 */
	public static class TableNode{
		public int suffix;
		public String partition;
		
		public TableNode(int suffix, String partition){
			this.suffix = suffix;
			this.partition = partition;
		}
	}
}
