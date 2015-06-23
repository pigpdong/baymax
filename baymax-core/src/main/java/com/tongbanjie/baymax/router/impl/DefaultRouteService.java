package com.tongbanjie.baymax.router.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.tongbanjie.baymax.datasource.MultipleDataSource;
import com.tongbanjie.baymax.exception.BayMaxException;
import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.parser.SqlParser;
import com.tongbanjie.baymax.router.RouteService;
import com.tongbanjie.baymax.router.TableRule;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import com.tongbanjie.baymax.router.model.ExecuteType;
import com.tongbanjie.baymax.router.model.ParameterEntity;
import com.tongbanjie.baymax.router.model.SqlType;
import com.tongbanjie.baymax.router.model.TargetSql;
import com.tongbanjie.baymax.utils.Pair;

/**
 * 路由器
 * 给出SQL，返回这个SQL的路由结果。
 * 路由结果又可能是:{@link ExecuteType.NO}{@link ExecuteType.PARTITION}{@link ExecuteType.ALL}
 * 分别表示：无需路由，路由到某个或某几个数据分区，全表扫描
 * @author dawei
 *
 */
public class DefaultRouteService implements RouteService{
	
	/**
	 * 上下文中所有的路由规则列表
	 */
	private List<TableRule> tableRules;
	
	/**
	 * 上下文中所有路由规则的MAP，方便使用表名查找到对应的路由规则
	 */
	private Map<String/*TableName*/, TableRule> tableRuleMapping = new HashMap<String, TableRule>();
	
	/**
	 * 数据源分发器,可用过数据源的唯一标识名称获取对应的数据源实例
	 */
	private MultipleDataSource dataSourceDispatcher;
	
	/**
	 * SQL解析器
	 * 主要用来提取SQL中的表名,Where中的KEY=VALUE形式的参数
	 */
	private SqlParser parser = new SqlParser();
	
	/**
	 * 构造
	 * @param tableRules
	 * @param dataSourceDispatcher
	 */
	public DefaultRouteService(List<TableRule> tableRules, MultipleDataSource dataSourceDispatcher){
		this.tableRules = tableRules;
		this.dataSourceDispatcher = dataSourceDispatcher;
		// 初始化路由规则的MAP,表名为KEY
		for(TableRule rule : tableRules){
			if(!tableRuleMapping.containsKey(rule.getLogicTableName())){
				tableRuleMapping.put(rule.getLogicTableName(), rule);
			}else{
				throw new RuntimeException("不能对同一个逻辑表明配置过个路由规则！：" + rule.getLogicTableName());
			}
		}
	}
	
	/**
	 * 对一条SQL进行路由运算,返回路由结果
	 * @param boundSql
	 * @return
	 */
	@Override
	public ExecutePlan doRoute(String sql, Map<Integer, ParameterCommand> parameterCommand){
		Pair<String/*tableName*/, SqlType/*sqlType*/> sqlParseDate = parser.findTableName(sql); 
		if(sqlParseDate == null || sqlParseDate.getObject1() == null || sqlParseDate.getObject2() == null){
			throw new BayMaxException("not support sqlType:" + sql);//不支持的SQL类型
		}
		String logicTableName = sqlParseDate.getObject1();
		SqlType sqlType = sqlParseDate.getObject2();
		TableRule rule = tableRuleMapping.get(logicTableName);
		if(rule == null){
			// 不需要路由
			ExecutePlan plan = new ExecutePlan();
			plan.setExecuteType(ExecuteType.NO);
			TargetSql actionSql = new TargetSql();
			actionSql.setSqlType(sqlType);
			actionSql.setPartition(null);
			actionSql.setDataSource(dataSourceDispatcher.getDefaultDataSource());
			actionSql.setLogicTableName(logicTableName);
			actionSql.setSqlReWrite(true);
			actionSql.setOriginalSql(sql);
			actionSql.setTargetSql(sql);
			actionSql.setTargetTableName(logicTableName);
			actionSql.setReWriteParameter(null);
			plan.addSql(actionSql);
			return plan;
		}else{
			// 需要路由
			// SQL解析
			List<ParameterEntity> sqlParserDate = parser.parse(sql, sqlType, parameterCommand);//SQL中提取的KV参数
			Map<String, Object> param = new HashMap<String, Object>();//SQL中提取的KV参数,放到MAP中,方便rule中EL调用
			boolean shardingKeyTouch = false;
			for(String shardingKey : rule.getShardingColumnsArray()){
				for(ParameterEntity sqlDate : sqlParserDate){
					if(shardingKey.equals(sqlDate.getKey())){
						param.put(shardingKey, sqlDate.getValue());
						shardingKeyTouch = true;//只要有一个KEY匹配则为true,用于是否全表扫描的判断.
						break;//一个KEY只要匹配到一次就好了,匹配下一个KEY
					}
				}
			}
			if(shardingKeyTouch == false){
				// 没有命中的shardingKey,则全表扫描
				ExecutePlan plan = new ExecutePlan();
				List<Pair<String/*partion*/, String/*table*/>> mappings = rule.getAllTableNames();
				if(mappings != null && mappings.size() > 0){
					for(Pair<String/*partion*/, String/*table*/> pt : mappings){
						/**
						 * 全表扫描：SQL对象只存储targetDB,targetTableName;执行时不改SQL,不改参数.
						 */
						TargetSql actionSql = new TargetSql();
						actionSql.setSqlType(sqlType);
						actionSql.setPartition(pt.getObject1());
						actionSql.setDataSource(dataSourceDispatcher.getDataSourceByName(pt.getObject1()));
						actionSql.setLogicTableName(logicTableName);
						actionSql.setSqlReWrite(true);
						actionSql.setOriginalSql(sql);
						actionSql.setTargetSql(sql.replaceFirst(logicTableName, pt.getObject2()));//逻辑表名替换为实际表名
						actionSql.setTargetTableName(pt.getObject2());
						actionSql.setReWriteParameter(null);
						plan.addSql(actionSql);
					}
					plan.setExecuteType(ExecuteType.ALL);
				}else{
					plan.setExecuteType(ExecuteType.NO);
				}
				return plan;
				
			}else{
				// TODO 有shardingKey命中,目前只支持路由到一个分区,一个表.以后改进.比如 whre id in()
				// 后面的执行器已经支持执行多条SQL了,要支持路由打多个分区只要返回多个target就行了.
				Pair<String/*targetDB*/, String/*targetTable*/> target = rule.executeRule(param);
				ExecutePlan routeResult = new ExecutePlan();
				if(target == null || target.getObject1() == null || target.getObject2() == null){
					// TODO print param
					throw new BayMaxException("路由结果不正确  targetDB:" + target.getObject1() +" targetTable:" + target.getObject2() + " SQL:" + sql + " PARAM:" + param.toString());
				}
				routeResult.setExecuteType(ExecuteType.PARTITION);
				TargetSql actionSql = new TargetSql();
				actionSql.setSqlType(sqlType);
				actionSql.setPartition(target.getObject1());
				actionSql.setDataSource(dataSourceDispatcher.getDataSourceByName(target.getObject1()));
				actionSql.setLogicTableName(logicTableName);
				actionSql.setSqlReWrite(true);
				actionSql.setOriginalSql(sql);
				actionSql.setTargetSql(sql.replaceFirst(logicTableName, target.getObject2()));//逻辑表名替换为实际表名
				actionSql.setTargetTableName(target.getObject2());
				actionSql.setReWriteParameter(null); // TODO 暂时不实现参数重写,所以这里继续使用Mybatis的参数，只有一条SQL被路由到不同的分区才需要参数重写.
				routeResult.addSql(actionSql);
				return routeResult;
			}
		}
	}
	
	public List<TableRule> getTableRules() {
		return tableRules;
	}

	public void setTableRules(List<TableRule> tableRules) {
		this.tableRules = tableRules;
	}

	public MultipleDataSource getDataSourceDispatcher() {
		return dataSourceDispatcher;
	}

	public void setDataSourceDispatcher(MultipleDataSource dataSourceDispatcher) {
		this.dataSourceDispatcher = dataSourceDispatcher;
	}

}
