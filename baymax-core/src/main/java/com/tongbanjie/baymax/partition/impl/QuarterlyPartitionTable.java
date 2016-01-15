package com.tongbanjie.baymax.partition.impl;

import java.text.ParseException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import com.tongbanjie.baymax.datasource.MultipleDataSource;
import com.tongbanjie.baymax.support.TableCreater;
import com.tongbanjie.baymax.utils.Pair;

/**
 * 按季度分表
 * <p>
 * 需要保证比当前时间多一张表,不然需要用的时候来不及创建
 * <p>
 * 表创建成功后需要把新表加进来
 * 
 * @author dawei
 *
 */
public class QuarterlyPartitionTable extends MonthlyPartitionTable {

	@Override
	public void initTableMapping(List<String> tableMappings) {
		for (String partition : tableMappings) {
			// TODO CHECK P1:20151-20152
			String[] str = partition.trim().split(":");
			String partitionDB = str[0].trim();
			String[] tableDiscripter = str[1].trim().split("-");
			String tableStart = tableDiscripter[0].trim();
			String tableEnd = tableDiscripter[1].trim();
			if (tableStart == null || tableStart.length() == 0 || tableEnd == null || tableEnd.length() == 0 || tableStart.length() != tableEnd.length()) {
				throw new RuntimeException("MonthlyTable tableMapping 配置有误" + super.logicTableName + "|" + partition);
			}
			if (super.suffixLength != tableStart.length()) {
				throw new RuntimeException("MonthlyTable tableMapping 配置有误 和parren模式长度不一致" + super.logicTableName + "|" + partition);
			}
			List<String> suffixs = listSuffix(tableStart, tableEnd, partition);
			for (String s : suffixs) {
				tableMapping.put(s, partitionDB);
			}
		}
	}

	/**
	 * 季度枚举
	 * <p>
	 * 20141-20154
	 * <p>
	 * 00000表示当前季度
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	protected List<String> listSuffix(String/* 20141 */start, String/*20154 00000*/ end, String partition) {
		Pair<Integer/*year*/, Integer/*quarter*/> startYear = getYear(start, partition);
		Pair<Integer/*year*/, Integer/*quarter*/> endYear = getYear(end, partition);
		
		List<String> suffixs = new LinkedList<String>();
		
		for(int year = startYear.getObject1(); year<= endYear.getObject1(); year++){
			for(int quarter = 1; quarter<=4 ; quarter++){
				if(year == startYear.getObject1() && quarter<startYear.getObject2()){
					continue;// 年=开始年 && 季度<开始季度	忽略 
				}
				suffixs.add(String.valueOf(year)+String.valueOf(quarter));
				if(year == endYear.getObject1() && quarter == endYear.getObject2()){
					break;// 错过截至季度	停止
				}
			}
		}
		return suffixs;
	}

	/**
	 * 获取year
	 * 
	 * @param str
	 * @return
	 */
	private Pair<Integer/* year */, Integer/* quarter */> getYear(String/*20154 00000*/ str, String partition) {
		if (str == null || str.trim().length() != 5) {
			throw new RuntimeException("config error!" + super.logicTableName + "|" + str);
		}
		boolean useDefault = true;
		for (int i = 0; i < str.length(); i++) {
			if (!"0".equals(String.valueOf(str.charAt(i)))) {
				useDefault = false;
			}
		}
		if (useDefault) {
			// 当前季度
			Calendar c = Calendar.getInstance();
			int quarter = getQuarter(c);
			if(partition == null){
				throw new RuntimeException("getYear parameter partition can't be null ,when dataStr is 00000");
			}
			super.autoCreatePartition = partition;
			return new Pair<Integer, Integer>(c.get(Calendar.YEAR), quarter);
		} else {
			int quarter = Integer.valueOf(str.substring(4));
			if(!(quarter >= 1 && quarter <= 4)){
				throw new RuntimeException("季度配置不对 " + super.logicTableName + "|" + str);
			}
			return new Pair<Integer, Integer>(Integer.valueOf(str.substring(0, 4)), quarter);
		}
	}

	/**
	 * 获取季度
	 * 
	 * @param c
	 * @return
	 */
	protected int getQuarter(Calendar c) {
		int currentMonth = c.get(Calendar.MONTH) + 1;
		if (currentMonth >= 1 && currentMonth <= 3) {
			return 1;
		} else if (currentMonth >= 4 && currentMonth <= 6) {
			return 2;
		} else if (currentMonth >= 7 && currentMonth <= 9) {
			return 3;
		} else if (currentMonth >= 10 && currentMonth <= 12) {
			return 4;
		} else {
			throw new RuntimeException("it can't be happen" + c);
		}
	}
	
	@Override
	public TableCreater getTableCreater() {
		if (likeTableName == null || likeTableName.trim().length() == 0 || autoCreatePartition == null || autoCreatePartition.trim().length() == 0) {
			return null;
		}
		tableCreater = new TableCreater() {

			@Override
			public void init(MultipleDataSource dataSource) {
				super.init(dataSource);
				initNextCreateTableSuffix(); // 初始化下张需要创建的表
			}

			private void initNextCreateTableSuffix() {
				if (tableCreater != null) {
					// 需要自动建表
					if (nextCreateTableSuffix == null) {
						// 启动的时候取当前时间下个季度
						nextCreateTableSuffix = getNextQuarterSuffix(Calendar.getInstance());
					} else {
						nextCreateTableSuffix = getNextQuarterSuffix(nextCreateTableSuffix);
					}
				}
			}

			@Override
			public void createTable(String suffix) {
				if (suffix.equals(nextCreateTableSuffix)) {
					String table = format(suffix);
					String partition = tableMapping.get(table);
					if (partition == null) {
						partition = autoCreatePartition; // 使用00000所在分区
					}
					boolean succ = createTableInDB(table, likeTableName, partition);
					if (succ) {
						// 创建成功或已经存在
						initNextCreateTableSuffix();
					}
				}
			}
		};
		return tableCreater;
	}
	
	private String getNextQuarterSuffix(Calendar c){
		return getNextQuarterSuffix(String.valueOf(c.get(Calendar.YEAR)) + getQuarter(c));
	}
	
	private String getNextQuarterSuffix(String suffix){
		Pair<Integer/* year */, Integer/* quarter */> yearQuarter = getYear(suffix, null);
		int year = yearQuarter.getObject1();
		int quarter = yearQuarter.getObject2();
		if(quarter < 4){
			quarter++;
		}else{
			quarter = 1;
			year++;
		}
		return String.valueOf(year) + String.valueOf(quarter);
	}
	
	public static void main(String[] args) throws ParseException {
		System.out.println(new QuarterlyPartitionTable().listSuffix("20142", "00000", null));
	}
}
