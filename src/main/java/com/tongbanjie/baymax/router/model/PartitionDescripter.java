package com.tongbanjie.baymax.router.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 这个类主要描述了在一个表的路由信息中包含了哪些数据分区(DB)，每个数据分区上有哪些分片(Table)
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
public class PartitionDescripter {
	
	/**
	 * 分区的原始配置
	 * <value>p1:[0001-0010]</value>中的值。
	 */
	private String config;//原始配置 p1:[0001-0010]
	
	/**
	 * 分区名称，不同的数据分区对应不同的DataSource
	 */
	private String partitionName;//p1
	
	/**
	 * 表明后缀格式化Pattern
	 */
	private String tablePatten;//[0001-0010]
	
	/**
	 * 后缀的起始值
	 */
	private int suffixStart;//0001
	
	/**
	 * 后缀的结束值
	 * 和{@link suffixStart}，{@link suffixLength}一起可以计算出这个数据分区上所有的表后缀，可用于全表扫描。
	 */
	private int suffixEnd;//0010
	
	/**
	 * 后缀的长度
	 */
	private int suffixLength;//4
	
	/**
	 * 后缀增长步长，默认为1，以后可以拓展，如后缀为日期是，则根据日期的规则。
	 */
	private int suffixIncrementStep = 1;
	
	/**
	 * 存储这个数据分区上所有表名的后缀，Rule初始化时就生成了。用于全表扫描。
	 */
	private List<String/*suffix*/> allTableNameSuffix;//所有分区-表名
	
	/**
	 * 根据int类型的suffix获取固定长度的完整表明后缀。
	 * @param suffix
	 * @return
	 */
	public String getSuffix(int suffix){
		String sfx = String.valueOf(suffix);
		if(sfx.length() > suffixLength){
			throw new RuntimeException("suffix is too long then config "+suffix+" "+config);
		}
		while(sfx.length() < suffixLength){
			sfx = "0"+sfx;
		}
		return sfx;
	}
	
	@Override
	public boolean equals(Object obj) {
		PartitionDescripter then = (PartitionDescripter)obj;
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
	
	/**
	 * 初始化
	 * @param config
	 */
	public PartitionDescripter(String config/*p1:[0001-0010]*/){
		// TODO check whith whith zeng zhe.
		config = config.trim();
		this.config = config;
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
	
	/**
	 * 初始化所有表名的后缀
	 */
	public void initAllTables(){
		allTableNameSuffix = new ArrayList<String>();
		for(int i = suffixStart; i<=suffixEnd; i+=suffixIncrementStep){
			allTableNameSuffix.add(getSuffix(i));
		}
	}

	public String getPartitionName() {
		return partitionName;
	}

	public List<String> getAllTableNameSuffix() {
		return allTableNameSuffix;
	}
}
