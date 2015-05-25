package com.tongbanjie.baymax.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import com.tongbanjie.baymax.utils.ReflectionUtils;

/**
 * 使用SQL路由必须遵循以下限制,否则进行全表扫描. 1. SQL中不能有?符号. 2. 分区列只能是等值条件如where user_id =
 * '123'.且key,value都不能使用函数 3. value只能是Long,String,Date类型。 3.
 * 子查询中请不要出现分区列,否则会把他当成路由参数而出现路由结果错误!
 * 
 * @author dawei
 *
 */
public class SqlParser {
	// TODO 1.TABLE_NAME,2.SQL_TYPE,3.INSER_VALUE
	private static Pattern ptable = Pattern.compile("\\s+([a-z0-9_@\\.\"$]+)\\s+");
	private static Pattern pinsert_into = Pattern.compile("\\s+into\\s+([a-z0-9_@\\.\"$]+)[\\s(]+");
	private static Pattern pdelete_from = Pattern.compile("\\s+from\\s+([a-z0-9_@\\.\"$]+)\\s+");
	private static Pattern pselect_from = Pattern.compile("\\s+from\\s+([a-z0-9_@\\.\"$]+)[\\s)]+");
	private static Pattern preplace_from = Pattern.compile("\\s+into\\s+([a-z0-9_@\\.\"$]+)[\\s(]+");
	private static Pattern pfrom_where = Pattern.compile("\\s+from\\s+(.*)\\s+where\\s+"); // .*默认最大匹配
	private static String hintregx = "/\\*.*?\\*/"; // hint正则式，懒惰匹配(最短匹配)
	private static Pattern kv_parameters = Pattern.compile("\\s+`*(\\w+){1}`*\\s*=\\s*'*(\\w|\\.|\\?)*'*(;|\\s*)");// where中的查询条件

	/**
	 * @return 返回sql中第一个表明的小写
	 */
	public String findTableName(String sql) {
		if (sql == null)
			return null;
		sql = sql.trim(); // trim可以去掉\\s,包括换行符、制表符等
		if (sql.length() < 7) {
			return null;
		}

		if (sql.indexOf("/*") != -1) {
			// 去除hint
			// System.out.println("hint:"+sql0);
			sql = sql.replaceAll(hintregx, "").trim(); // 懒惰匹配(最短匹配)
			// System.out.println(sql0);
		}
		sql = sql.toLowerCase();
		sql = sql + " "; // 便于处理

		if (sql.startsWith("update")) {
			Matcher m = ptable.matcher(sql);
			if (m.find(6)) {
				return m.group(1);
			}
			return null;
		}

		if (sql.startsWith("delete")) {
			Matcher m = pdelete_from.matcher(sql);
			if (m.find(6)) {
				return m.group(1);
			}

			m = ptable.matcher(sql); // delete 可以没有from
			if (m.find(6)) {
				return m.group(1);
			}
			return null;
		}

		if (sql.startsWith("insert")) {
			Matcher m = pinsert_into.matcher(sql);
			if (m.find(6)) {
				return m.group(1);
			}
			return null;
		}

		if (sql.startsWith("replace")) {
			Matcher m = preplace_from.matcher(sql);
			if (m.find(6)) {
				return m.group(1);
			}
			return null;
		}

		if (!sql.startsWith("select")) {
			return null; // 不以update delete select开头的sql
		}

		Matcher m = pselect_from.matcher(sql);
		if (m.find(6)) {
			return m.group(1);
		}

		m = pfrom_where.matcher(sql);
		if (m.find(6)) {
			String from2where = m.group(1);
			// System.out.println(from2where);
			String[] tables = from2where.split(",");
			for (int i = 1; i < tables.length; i++) {
				// 因为第一个项已经搜索过了，所以从第二项开始
				if (tables[i].indexOf('(') == -1) {
					return tables[i].trim().split("\\s")[0];
				} else {
					String subTable = findTableName(tables[i]);
					if (subTable != null) {
						return subTable;
					}
				}
			}
		}

		// 考虑是否一开始就对所有的右括号前后加空格
		if (sql.indexOf(")from") != -1) {
			System.out.println(sql);
			sql = sql.replaceAll("\\)from", ") from");
			return findTableName(sql);
		}

		return null;
	}
	
	/**
	 * 替换SQL表名
	 * @param sql
	 * @param logicTableName 逻辑表名
	 * @param phyTableName 物理表名
	 * @return new Sql
	 */
	public String replaceTableName(String sql, String logicTableName, String phyTableName){
		return sql.replaceFirst(logicTableName, phyTableName);
	}

	/**
	 * SQL解析,提取WHERE条件中的KEY,VALUE
	 * 
	 * @param boundSql
	 * @return
	 */
	public List<SqlParseDate> parse(BoundSql boundSql) {
		String sql = boundSql.getSql().trim();
		if (sql.endsWith(";")) {
			sql = sql.substring(0, sql.length() - 1);
		}
		SqlParseDate data = new SqlParseDate();
		Matcher m = kv_parameters.matcher(sql);
		List<SqlParseDate> dataList = new ArrayList<SqlParseDate>();
		while (m.find()) {
			String kvstr = m.group();
			System.out.println("--" + kvstr);
			String[/* key-value */] kv = kvstr.split("=");
			kv[0] = kv[0].trim();
			kv[1] = kv[1].trim();
			if (kv[0].startsWith("`") && kv[0].endsWith("`")) {
				kv[0] = kv[0].substring(1, kv[0].length() - 1);
			}
			dataList.add(data);
			data.setKey(kv[0]);
			data.setOriginalValue(kv[1]);

			if (kv[1].startsWith("'") && kv[1].endsWith("'")) {
				data.setValue(kv[1]);
			} else if ("?".equals(kv[1])) {
				Object value = null;
				ParameterMapping mapping = boundSql.getParameterMappings().get(getPlaceholderIndex(sql, kvstr));
				Class<?> javaType = mapping.getJavaType();
				if (javaType == Byte.class || javaType == Short.class || javaType == Integer.class || javaType == Long.class
						|| javaType == Float.class || javaType == Double.class) {
					value = ReflectionUtils.getFieldValue(boundSql.getParameterObject(), "longValue");
				} else if (javaType == String.class) {
					value = boundSql.getParameterObject();
				} else if (javaType == Boolean.class) {
					throw new RuntimeException("Boolean type can't used for partition key!");
				} else {
					// 对象类型
					value = ReflectionUtils.getFieldValue(boundSql.getParameterObject(), mapping.getProperty());
					// TODO 需要测试下SQL配置中,对象嵌套的情况
				}
				data.setValue(value);
			} else {
				data.setValue(Double.valueOf(kv[1]));
			}
		}
		return dataList;
	}

	/**
	 * 获取占位符下标
	 * 
	 * @param sql
	 * @param str
	 * @return
	 */
	public int getPlaceholderIndex(String sql, String str) {
		int end = sql.indexOf(str);
		sql = sql.substring(0, end);
		int index = 0;
		for (int i = 0; i < sql.length(); i++) {
			if ("?".equals(sql)) {
				index++;
			}
		}
		return index;
	}

	public static void main(String[] args) {
		SqlParser parser = new SqlParser();

		List<String> sqls = new ArrayList<String>();
		sqls.add("	\r	\r\n \n   	update 	t_a$ble0 set a=1");
		sqls.add("delete from t_a$ble0\r\n t where t.id = 0");
		sqls.add("delete from T_$ble0");
		sqls.add("insert into t_a$ble0 t values(?,?) where t.id = 0");
		sqls.add("insert into t_a$ble0(col_a, col_b) values(?,?) where id = 0");
		sqls.add("select count(*) from t_a$ble0");
		sqls.add("select 1 from t_a$ble0 t where t.id=0");
		sqls.add("select 1 from (select id from t_a$ble0) t where t.id = 5");
		sqls.add("select 1 from(select id from t_a$ble0) t where t.id = 5");
		sqls.add("select 1 from (select id from table2) t, t_a$ble0 a where t.id = a.id");
		sqls.add("select 1 from t_a$ble0 a, (select id from table2) t where t.id = a.id");
		sqls.add("select count(*) from CRM_KNOWLEDGE_DETAIL kc,CRM_KNOWLEDGE_BASE a where a.id=kc.KNOWLEDGE_ID");
		sqls.add("SELECT * FROM (SELECT CAST(STR2NUMLIST(#in#) AS NUMTABLETYPE) FROM dual) WHERE rownum <= 200");
		sqls.add("insert into ic_cache@lnk_icdb0 values (:b1 , sysdate) ");
		sqls.add("select a ,r from icuser.tb0 where spu_id=:f1 and auction_type <> 'a' ");
		sqls.add("select id from tb0 a, table(cast(str2numlist(:1) as numtabletype )) t where a.id=:2");
		sqls.add("select id from table(cast(str2numlist(:1) as numtabletype )) t, tb0 a where a.id=:2");
		sqls.add("select id from table(cast(str2numlist(:1) as numtabletype )) t, table(cast(str2numlist(:1) as numtabletype )) b, tb0 a where a.id=:2");
		sqls.add("select id from table(cast(str2numlist(:1) as numtabletype )) t, (select col1 from tb2) b, tb0 a where a.id=:2");
		sqls.add("select id from table(cast(str2numlist(:1) as numtabletype )) t, (select col1,col2 from tb2) b, tb0 a where a.id=:2");
		sqls.add("select id from table(cast(str2numlist(:1) as numtabletype )) t, (select col1,col2 from tb2 where tb2.id=0) b, tb0 a where a.id=:2");
		sqls.add("select max(mod(nvl(option$,0),2))from objauth$ where obj#=:1 group by grantee# order by grantee# ");
		sqls.add("select output from table(dbms_workload_repository.awr_report_html(:dbid, :inst, :bid, :eid, :rpt_options))");
		sqls.add("DELETE crm_adgroup_detail WHERE status = 1 AND adgroupno = :1");
		sqls.add("SELECT * FROM \"ALIMM\".\"ADZONESCORE\"");
		sqls.add("select nvl(min(ts#), -1) \"sysauxts#\" from sys.ts$ where name = 'sysaux'");
		sqls.add("/* oracleoem */ select nvl(min(ts#), -1) \"sysauxts#\" from sys.ts$ where name = 'sysaux'");
		sqls.add("/* oracleoem */ select /* sss */nvl(min(ts#), -1) \"sysauxts#\" from sys.ts$ where name = 'sysaux'"); // 多段hint
		sqls.add("failed:select u.id from (table(str2numlist(:1))) n join et_airsupply_users u on n.column_value = u.id"); // join
		sqls.add("replace into t (i,c,d,ui) values (?,?,?,?)");
		sqls.add(" SELECT /*+ ordered use_nl(acc,rb) */ rb.ID,rb.USER_ID,rb.DATABASE_CODE,EVENT_EXTEND FROM (SELECT /*+index(crb,IDX_RA_SC_BILL_STAT) */ crb.USER_ID, min(crb.id) dt FROM RA_SC_BILL crb  WHERE crb.status = 1 and crb.process_mode = 0 and rownum <= 20000 and DATABASE_CODE in (1, 2, 3) GROUP BY crb.USER_ID) acc, RA_SC_BILL rb WHERE rb.Id = acc.dt  and rownum <= 123  and not exists (select 1 from RA_SC_BILL up where up.status = 2 and up.USER_ID = acc.USER_ID)");
		for (String sql : sqls) {
			System.out.println(parser.findTableName(sql) + " <-- " + sql);
		}
	}
}
