package com.tongbanjie.baymax.parser;

import java.util.List;
import java.util.Map;
import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.router.model.SqlArgEntity;
import com.tongbanjie.baymax.router.model.SqlType;
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
public interface SqlParser {

	/**
	 * @return 返回sql中第一个表明的小写/sql类型
	 */
	public Pair<String/*tableName*/, SqlType/*sqlType*/> findTableName(String sql);
	
	/**
	 * 替换SQL表名
	 * @param sql
	 * @param logicTableName 逻辑表名
	 * @param phyTableName 物理表名
	 * @return new Sql
	 */
	public String replaceTableName(String sql, String logicTableName, String phyTableName);

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
	public List<SqlArgEntity> parseInsertSql(String sql, SqlType sqlType);
	
	
	/**
	 * 在where条件解析提取参数
	 * @param boundSql
	 * @param sqlType
	 * @return
	 */
	public List<SqlArgEntity> parseWhereSql(String sql, SqlType sqlType);
	
	/**
	 * SQL解析,提取条件中的KEY,VALUE
	 * 
	 * @param boundSql
	 * @return
	 */
	public List<SqlArgEntity> parse(String sql, SqlType sqlType, Map<Integer, ParameterCommand> parameterCommands, String[] shardingColumns);

}
