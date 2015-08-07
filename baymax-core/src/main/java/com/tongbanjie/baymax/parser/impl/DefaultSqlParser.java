package com.tongbanjie.baymax.parser.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.parser.SqlParser;
import com.tongbanjie.baymax.router.model.SqlArgEntity;
import com.tongbanjie.baymax.router.model.SqlType;
import com.tongbanjie.baymax.router.model.SqlArgEntity.CompareType;
import com.tongbanjie.baymax.utils.Pair;

/**
 * 使用SQL路由必须遵循以下限制,否则进行全表扫描. 1. SQL中不能有?符号. 2. 分区列只能是等值条件如where user_id =
 * '123'.且key,value都不能使用函数 3. value只能是Long,String,Date类型。 3.
 * 子查询中请不要出现分区列,否则会把他当成路由参数而出现路由结果错误!
 * 
 * SQL解析器
 * 主要用来提取SQL中的表名,Where中的KEY=VALUE形式的参数
 * @author dawei
 *
 */
public class DefaultSqlParser implements SqlParser{
	// TODO 1.TABLE_NAME,2.SQL_TYPE,3.INSER_VALUE
	private static Pattern kv_parameters = Pattern.compile("\\s+`*@*(\\w+){1}`*\\s*=\\s*'*(\\w|\\.|\\?)*'*(;|\\s*)");// where中的条件
	// private static Pattern kv_parameters = Pattern.compile("\\s+`*@*(\\w+){1}`*\\s*(=|>=|>|<=|<)\\s*'*(\\w|\\.|\\?)*'*(;|\\s*)");// where中的条件
	private static Pattern insertColumns = Pattern.compile("\\s*insert+\\s+(into)?\\s+@*(\\w)+\\s+\\((\\w|,|\\s)+\\)");//提取insert中的列名
	private static Pattern insertValues = Pattern.compile("\\s+values\\s+\\((\\w|,|\\s|\\?|\\(|\\))+\\)");//提取insert中的列值

	/**
	 * @return 返回sql中第一个表明的小写/sql类型
	 * 无需解析返回Null
	 * 返回第一个@@tableName中的tableName
	 * 类型暂时全部返回Other // TODO
	 */
	public Pair<String/*tableName*/, SqlType/*sqlType*/> findTableName(String sql) {
		if(sql == null || sql.length() == 0 || sql.indexOf("@@") == -1){
			return null; // null表示无需解析
		}
		sql = sql.trim();
		int start = sql.indexOf("@@");
		String tableNameStr = sql.substring(start + 2);
		int index = tableNameStr.indexOf(" ");
		if(index != -1){
			tableNameStr = tableNameStr.substring(0, index).trim();
		}
		
		while(true){
			if(sql.startsWith("(")){
				sql = sql.substring(1).trim();
			}else{
				break;
			}
		}
		sql = sql.toLowerCase();
		SqlType sqlType = SqlType.OTHER;
		if(sql.startsWith("select")){
			sqlType = SqlType.SELECT;
		}else if(sql.startsWith("update")){
			sqlType = SqlType.UPDATE;
		}else if(sql.startsWith("insert")){
			sqlType = SqlType.INSERT;
		}else if(sql.startsWith("delete")){
			sqlType = SqlType.DELETE;
		}
		
		return new Pair<String, SqlType>(tableNameStr, sqlType);
	}
	
	/**
	 * 替换SQL表名
	 * @param sql
	 * @param logicTableName 逻辑表名
	 * @param phyTableName 物理表名
	 * @return new Sql
	 */
	public String replaceTableName(String sql, String logicTableName, String phyTableName){
		return sql.replaceAll("@@"+logicTableName, phyTableName);
	}

	/**
	 * 在insert语句中提取参数
	 * INSERT INTO
		trade_order (
			id,
			product_id,
			amount,
			real_pay_amount,
			create_time,
			user_id,
			ta_id
		)
		VALUES
		(
			#{id},
			#{productId},
			#{amount},
			#{realPayAmount},
			#{createTime},
			#{userId},
			#{taId}
		)
	 * @param boundSql
	 * @param sqlType
	 * @return
	 */
	public List<SqlArgEntity> parseInsertSql(String sql, SqlType sqlType) {
		if (sql.endsWith(";")) {
			sql = sql.substring(0, sql.length() - 1);
		}
		sql = sql.toLowerCase() + " ";
		Matcher m = insertColumns.matcher(sql);
		String[] columns = null;
		String[] columnsValue = null;
		List<SqlArgEntity> args = new ArrayList<SqlArgEntity>();
		// 提取列名
		while(m.find()){
			String str = m.group();
			if(str != null && str.trim().length() != 0){
				columns = getColums(str);
				break;
			}
		}
		// 提取列值
		m = insertValues.matcher(sql);
		while(m.find()){
			String str = m.group();
			if(str != null && str.trim().length() != 0){
				columnsValue = getColums(str);
				break;
			}
		}
		if(columns != null){
			for(int i = 0; i< columns.length; i++){
				SqlArgEntity arg = new SqlArgEntity();
				arg.setKey(columns[i].trim());
				arg.setOriginalValue(columnsValue[i].trim());
				arg.setCompareType(CompareType.eq);
				args.add(arg);
			}
		}
		return args;
	}
	
	private String[] getColums(String str){
		str = str.substring(str.indexOf("(")+1, str.lastIndexOf(")"));
		return str.split(",");
	}
	
	/**
	 * 在where条件解析提取参数
	 * @param boundSql
	 * @param sqlType
	 * @return
	 */
	public List<SqlArgEntity> parseWhereSql(String sql, SqlType sqlType) {
		if (sql.endsWith(";")) {
			sql = sql.substring(0, sql.length() - 1);
		}
		Matcher m = kv_parameters.matcher(sql);// 提取where中的 key=value对
		List<SqlArgEntity> args = new ArrayList<SqlArgEntity>();
		while (m.find()) {
			String kvstr = m.group();
			String[/* key-value */] kv = kvstr.split("=");
			kv[0] = kv[0].trim();
			kv[1] = kv[1].trim();
			if (kv[0].startsWith("`") && kv[0].endsWith("`")) {
				kv[0] = kv[0].substring(1, kv[0].length() - 1);
				kv[0] = kv[0].trim();
			}
			SqlArgEntity arg = new SqlArgEntity();
			arg.setKey(kv[0]);
			arg.setOriginalValue(kv[1]);
			arg.setCompareType(CompareType.eq);
			args.add(arg);
		}
		return args;
	}
	
	/**
	 * SQL解析,提取条件中的KEY,VALUE
	 * 
	 * @param boundSql
	 * @return
	 */
	public List<SqlArgEntity> parse(String sql, SqlType sqlType, Map<Integer, ParameterCommand> parameterCommands, String[] shardingColumns) {
		List<SqlArgEntity> kvs = null;
		if(sqlType == SqlType.INSERT){
			kvs =  parseInsertSql(sql, sqlType);
		}else{
			kvs = parseWhereSql(sql, sqlType);
		}
		return buildDate(kvs, parameterCommands, shardingColumns);
	}

	/**
	 * 把SQL中提取的参数转化为对象,如果参数是?占位符号则从Mybats中获取这个占位符对应的参数对象.
	 * 
	 * TODO 取sql中的第一个参数
	 * @param columnNames
	 * @param columnValues
	 * @param parameterCommand
	 * @return
	 */
	private List<SqlArgEntity> buildDate(List<SqlArgEntity> args, Map<Integer, ParameterCommand> parameterCommands, String[] shardingColumns){
		List<SqlArgEntity> sqlParseDateList = new ArrayList<SqlArgEntity>();
		if(args == null || args.size() == 0){
			return sqlParseDateList;
		}
		List<String> shardingColumnsList = Arrays.asList(shardingColumns);
		for(int i = 0; i<args.size(); i++){
			SqlArgEntity arg = args.get(i);
			String originalValue = arg.getOriginalValue();
			if(!shardingColumnsList.contains(arg.getKey())){
				continue;// 只取需要的参数
			}
			/**
			 * 1. 不能对分区键使用函数
			 * 2. 只能是字符串 数字 时间 三种类型
			 * 3. 如果是字符串类型的时间 必须是yyyy-MM-dd HH:mm:ss的格式
			 */
			if (originalValue.startsWith("'") && originalValue.endsWith("'")) {
				// TODO ''内的也可能是时间,对时间怎么处理,统一转为String?
				arg.setValue(originalValue);// String
			} else if ("?".equals(originalValue)) {
				// 获取i之前已经有几个?号了
				// JDBC规范第一列是从1开始的
				ParameterCommand command = parameterCommands.get(getPlaceholderIndex(args, i) + 1);
				Object value = command == null ? null : command.getParttionArg();
				if(value instanceof String || value instanceof Integer || value instanceof Long){
					arg.setValue(value);
				}else if(value instanceof Date){
					java.util.Date date = new java.util.Date(((Date) value).getTime());
					arg.setValue(date);
				}
			} else {
				arg.setValue(Long.valueOf(originalValue));// 数字
			}
			sqlParseDateList.add(arg);
		}
		return sqlParseDateList;
	}
	
	/**
	 * 获取占位符下标
	 * 应为要从Mybatis的List<ParameterMapping>中获取参数的话,需要知道?是第几个参数
	 * @param sql
	 * @param str
	 * @return
	 */
	public int getPlaceholderIndex(List<SqlArgEntity> args, int thisIndex) {
		int count = 0;
		for(int i = 0; i<args.size() && i<thisIndex; i ++){
			if(args.get(i).getOriginalValue().equals("?")){
				count++;
			}
		}
		return count;
	}

	public static void main(String[] args) {
		DefaultSqlParser parser = new DefaultSqlParser();

		List<String> sqls = new ArrayList<String>();
//		sqls.add("	\r	\r\n \n   	update 	t_a$ble0 set a=1");
//		sqls.add("delete from t_a$ble0\r\n t where t.id = 0");
//		sqls.add("delete from T_$ble0");
//		sqls.add("insert into t_a$ble0 t values(?,?) where t.id = 0");
//		sqls.add("insert into t_a$ble0(col_a, col_b) values(?,?) where id = 0");
//		sqls.add("select count(*) from t_a$ble0");
//		sqls.add("select 1 from t_a$ble0 t where t.id=0");
//		sqls.add("select 1 from (select id from t_a$ble0) t where t.id = 5");
//		sqls.add("select 1 from(select id from t_a$ble0) t where t.id = 5");
//		sqls.add("select 1 from (select id from table2) t, t_a$ble0 a where t.id = a.id");
//		sqls.add("select 1 from t_a$ble0 a, (select id from table2) t where t.id = a.id");
//		sqls.add("select count(*) from CRM_KNOWLEDGE_DETAIL kc,CRM_KNOWLEDGE_BASE a where a.id=kc.KNOWLEDGE_ID");
//		sqls.add("SELECT * FROM (SELECT CAST(STR2NUMLIST(#in#) AS NUMTABLETYPE) FROM dual) WHERE rownum <= 200");
//		sqls.add("insert into ic_cache@lnk_icdb0 values (:b1 , sysdate) ");
//		sqls.add("select a ,r from icuser.tb0 where spu_id=:f1 and auction_type <> 'a' ");
//		sqls.add("select id from tb0 a, table(cast(str2numlist(:1) as numtabletype )) t where a.id=:2");
//		sqls.add("select id from table(cast(str2numlist(:1) as numtabletype )) t, tb0 a where a.id=:2");
//		sqls.add("select id from table(cast(str2numlist(:1) as numtabletype )) t, table(cast(str2numlist(:1) as numtabletype )) b, tb0 a where a.id=:2");
//		sqls.add("select id from table(cast(str2numlist(:1) as numtabletype )) t, (select col1 from tb2) b, tb0 a where a.id=:2");
//		sqls.add("select id from table(cast(str2numlist(:1) as numtabletype )) t, (select col1,col2 from tb2) b, tb0 a where a.id=:2");
//		sqls.add("select id from table(cast(str2numlist(:1) as numtabletype )) t, (select col1,col2 from tb2 where tb2.id=0) b, tb0 a where a.id=:2");
//		sqls.add("select max(mod(nvl(option$,0),2))from objauth$ where obj#=:1 group by grantee# order by grantee# ");
//		sqls.add("select output from table(dbms_workload_repository.awr_report_html(:dbid, :inst, :bid, :eid, :rpt_options))");
//		sqls.add("DELETE crm_adgroup_detail WHERE status = 1 AND adgroupno = :1");
//		sqls.add("SELECT * FROM \"ALIMM\".\"ADZONESCORE\"");
//		sqls.add("select nvl(min(ts#), -1) \"sysauxts#\" from sys.ts$ where name = 'sysaux'");
//		sqls.add("/* oracleoem */ select nvl(min(ts#), -1) \"sysauxts#\" from sys.ts$ where name = 'sysaux'");
//		sqls.add("/* oracleoem */ select /* sss */nvl(min(ts#), -1) \"sysauxts#\" from sys.ts$ where name = 'sysaux'"); // 多段hint
//		sqls.add("failed:select u.id from (table(str2numlist(:1))) n join et_airsupply_users u on n.column_value = u.id"); // join
//		sqls.add("replace into t (i,c,d,ui) values (?,?,?,?)");
//		sqls.add(" SELECT /*+ ordered use_nl(acc,rb) */ rb.ID,rb.USER_ID,rb.DATABASE_CODE,EVENT_EXTEND FROM (SELECT /*+index(crb,IDX_RA_SC_BILL_STAT) */ crb.USER_ID, min(crb.id) dt FROM RA_SC_BILL crb  WHERE crb.status = 1 and crb.process_mode = 0 and rownum <= 20000 and DATABASE_CODE in (1, 2, 3) GROUP BY crb.USER_ID) acc, RA_SC_BILL rb WHERE rb.Id = acc.dt  and rownum <= 123  and not exists (select 1 from RA_SC_BILL up where up.status = 2 and up.USER_ID = acc.USER_ID)");
		sqls.add("(SELECT "+ 
				"id, user_id, platform, style_type, bussniss_type, items, label, dead_line, create_time"+
			" FROM @@usermng_user_msg"+
			" WHERE 1=1"+
					" AND user_id = ? "+
			" order by create_time desc limit ?"+
		" )"+
		" UNION ALL"+
		" ("+
			" SELECT "+
		 		" id, user_id, platform, style_type, bussniss_type, items, label, dead_line, create_time"+
	        " FROM usermng_sys_msg"+
			" WHERE 1=1 "+
			" order by create_time desc limit ?"+
		" )"+
		" order by create_time desc limit ?");
		for (String sql : sqls) {
			System.out.println(parser.findTableName(sql).getObject1()+ parser.findTableName(sql).getObject2() + " <-- " + sql);
		}
	}
}
