package com.tongbanjie.baymax.router.strategy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 按月分表
 * <p>
 * 需要保证比当前时间多一张表,不然需要用的时候来不及创建
 * <p>
 * 表创建成功后自动把新表加进来
 * 
 * @author dawei
 *
 */
public class MonthlyPartitionTable extends ELPartitionTable {

	private final static Logger logger = LoggerFactory.getLogger(MonthlyPartitionTable.class);

	protected String likeTableName; 					// 自动创建表时使用哪个表作为模板
	protected volatile String nextCreateTableSuffix; 	// 下一个要自动创建表的后缀启动时赋值，当前时间的下一张表

	/**
	 * 把配置中的tableMapping转换为对象
	 * <p>
	 * 枚举出范围内的所有表
	 * <p>
	 * 000000代表当前时间
	 * <p>
	 * p1:201402-201502 p2:201503-000000
	 * <p>
	 * 程序启动时已经被枚举出的表不会被自动创建，所以必须保证000000（当前时间）之前的表必须被手工创建
	 * <p>
	 * 自动创建的表是当前时间的下一张表，以此类推
	 */
	@Override
	public void initTableMapping(List<String> tableMappings) {
		for (String partition : tableMappings) {
			// TODO CHECK P1:201501-201502
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
			Date start = parseToDate(tableStart, "yyyyMM", partition);
			Date end = parseToDate(tableEnd, "yyyyMM", partition);
			List<String> suffixs = listSuffix(start, end);
			for (String s : suffixs) {
				tableMapping.put(s, partitionDB);
			}
		}
	}

	/**
	 * 格式化时间
	 * <p>
	 * 201501==>Date 000000==>当前时间
	 * 
	 * @param dateStr
	 * @param format
	 * @param partition 如果时间格式为000000 则把这个partition设置为要自动建表的partition
	 * @return
	 */
	protected Date parseToDate(String dateStr, String format, String partition) {
		if (dateStr == null || dateStr.trim().length() == 0) {
			throw new RuntimeException("date must no empty!");
		}
		boolean useDefault = true;
		for (int i = 0; i < dateStr.length(); i++) {
			if (!"0".equals(String.valueOf(dateStr.charAt(i)))) {
				useDefault = false;
			}
		}
		if (useDefault) {
			if(partition == null){
				throw new RuntimeException("parseToDate parameter partition can't be null ,when dataStr is 000000");
			}
			super.autoCreatePartition = partition;
			return new Date();
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			try {
				return sdf.parse(dateStr);
			} catch (ParseException e) {
				logger.error("parse date error in partition config", e);
				throw new RuntimeException("parse date error in partition config", e);
			}
		}
	}

	/**
	 * 枚举出两个时间点之间的所有月
	 * <p>
	 * 
	 * @param
	 * @param end
	 * @return
	 */
	protected List<String> listSuffix(Date start, Date end) {
		if (start.after(end) || start.equals(end)) {
			throw new RuntimeException("开始时间必须比结束时间早 " + super.logicTableName);
		}
		Calendar tmp = Calendar.getInstance();

		Calendar c1 = Calendar.getInstance();
		tmp.clear();
		tmp.setTime(start);
		c1.set(tmp.get(Calendar.YEAR), tmp.get(Calendar.MONTH), 1);

		Calendar c2 = Calendar.getInstance();
		tmp.clear();
		tmp.setTime(end);
		c2.set(tmp.get(Calendar.YEAR), tmp.get(Calendar.MONTH), 1);

		List<String> suffixs = new LinkedList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		while (c1.before(c2)) {
			suffixs.add(sdf.format(c1.getTime()));
			c1.add(Calendar.MONTH, 1);
		}
		suffixs.add(sdf.format(c2.getTime()));

		return suffixs;
	}

	/**
	 * 当没有获取到suffix映射时，触发自动建表
	 */
	@Override
	protected String getTargetPartition(String suffix) {
		String partition = super.getTargetPartition(suffix);
		if (partition == null) {
			partition = super.getTargetPartition(suffix); 	// 重新获取
		}
		return partition;
	}

	protected String getTableLike() {
		return likeTableName;
	}

	protected void setTableLike(String tableLike) {
		this.likeTableName = tableLike;
	}

	public static void main(String[] args) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		Date start = sdf.parse("201405");
		Date end = new Date();
		System.out.println(new MonthlyPartitionTable().listSuffix(start, end));
	}
}
