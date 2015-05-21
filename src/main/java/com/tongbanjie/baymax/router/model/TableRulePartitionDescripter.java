package com.tongbanjie.baymax.router.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 对配置的解析</p>
 * 在一个RULE中,一个DB只能创建一个Descrepter
 * 
 * <property name="partition">
			<list>
				<value>p1:[0001-0010]</value>
				<value>p2:[0010-0020]</value>
			</list>
		</property>
 * 
 * @author dawei
 *
 */
public class TableRulePartitionDescripter {
	
	private String config;//原始配置 p1:[0001-0010]
	
	private String partitionName;//p1
	
	private String tablePatten;//[0001-0010]
	
	private int suffixStart;//0001
	
	private int suffixEnd;//0010
	
	private int suffixLength;//4
	
	private int suffixIncrementStep = 1;
	
	private List<String/*suffix*/> allTableNameSuffix;//所有分区-表名
	
	public String getSuffix(int suffix){
		String sfx = String.valueOf(suffix);
		if(sfx.length() > suffix){
			throw new RuntimeException("suffix is too long then config "+suffix+" "+config);
		}
		while(sfx.length() < suffixLength){
			sfx = "0"+sfx;
		}
		return sfx;
	}
	
	@Override
	public boolean equals(Object obj) {
		TableRulePartitionDescripter then = (TableRulePartitionDescripter)obj;
		if(this == then){
			return true;
		}
		if(this.partitionName.equals(then.partitionName)){
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.partitionName.hashCode();
	}
	
	public TableRulePartitionDescripter(String config/*p1:[0001-0010]*/){
		// TODO check whith whith zeng zhe.
		config = config.trim();
		String[] str = config.split(":");
		this.partitionName = str[0];
		this.tablePatten = str[1];
		
		String suffixStrConfig = this.tablePatten.substring(1,this.tablePatten.length() - 1);
		String[] suffixStr = suffixStrConfig.split("-");
		suffixStart = Integer.valueOf(suffixStr[0]);
		suffixEnd = Integer.valueOf(suffixStr[1]);
		
		if(suffixStr[0].length() != suffixStr[1].length()){
			throw new RuntimeException("the suffix in the partition config must has equal leng. " + config);
		}
		suffixLength = suffixStr[0].length();
		
		initAllTables();
	}
	
	public void initAllTables(){
		allTableNameSuffix = new ArrayList<String>();
		for(int i = suffixStart; i<=suffixEnd; i+=suffixIncrementStep){
			allTableNameSuffix.add(getSuffix(suffixStart));
		}
	}

	public String getPartitionName() {
		return partitionName;
	}

	public List<String> getAllTableNameSuffix() {
		return allTableNameSuffix;
	}
}
