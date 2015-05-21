package com.tongbanjie.baymax.router.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.mapping.BoundSql;
import com.tongbanjie.baymax.datasources.DataSourceDispatcher;
import com.tongbanjie.baymax.model.RouteResult;
import com.tongbanjie.baymax.model.RouteResultType;
import com.tongbanjie.baymax.model.Sql;
import com.tongbanjie.baymax.parser.SqlParseDate;
import com.tongbanjie.baymax.parser.SqlParser;
import com.tongbanjie.baymax.router.IRouteService;
import com.tongbanjie.baymax.router.ITableRule;
import com.tongbanjie.baymax.utils.Pair;

/**
 * 路由器
 * @author dawei
 *
 */
public class DefaultRouteService implements IRouteService{
	
	private List<ITableRule> tableRules;
	
	private Map<String, ITableRule> tableRuleMapping = new HashMap<String, ITableRule>();
	
	private DataSourceDispatcher dataSourceDispatcher;
	
	private SqlParser parser = new SqlParser();
	
	/**
	 * 构造
	 * @param tableRules
	 * @param dataSourceDispatcher
	 */
	public DefaultRouteService(List<ITableRule> tableRules, DataSourceDispatcher dataSourceDispatcher){
		this.tableRules = tableRules;
		this.dataSourceDispatcher = dataSourceDispatcher;
		for(ITableRule rule : tableRules){
			if(!tableRuleMapping.containsKey(rule.getLogicTableName())){
				tableRuleMapping.put(rule.getLogicTableName(), rule);
			}else{
				throw new RuntimeException("不能对同一个逻辑表明配置过个路由规则！：" + rule.getLogicTableName());
			}
		}
	}
	
	/**
	 * Mybatis结合专用
	 * @param boundSql
	 * @return
	 */
	@Override
	public RouteResult doRoute(BoundSql boundSql){
		String sql = boundSql.getSql();
		String tableName = parser.findTableName(sql);
		ITableRule rule = tableRuleMapping.get(tableName);
		if(rule == null){
			// 不需要路由
			RouteResult routeResult = new RouteResult();
			routeResult.setResultType(RouteResultType.NO);
			return routeResult;
		}else{
			// 需要路由
			// SQL解析
			List<SqlParseDate> sqlParserDate = parser.parse(boundSql);//SQL中提取的KV参数
			Map<String, Object> param = new HashMap<String, Object>();//SQL中提取的KV参数,放到MAP中,方便rule中EL调用
			boolean shardingKeyTouch = false;//
			for(String shardingKey : rule.getShardingKeys()){
				for(SqlParseDate sqlDate : sqlParserDate){
					if(shardingKey.equals(sqlDate.getKey())){
						param.put(shardingKey, sqlDate.getValue());
						shardingKeyTouch = true;
						break;
					}
				}
			}
			if(shardingKeyTouch == false){
				// 没有命中的shardingKey,则全表扫描
				RouteResult routeResult = new RouteResult();
				List<Pair<String/*partion*/, String/*table*/>> mappings = rule.getAllTableNames();
				if(mappings != null && mappings.size() > 0){
					for(Pair<String/*partion*/, String/*table*/> pt : mappings){
						/**
						 * 全表扫描：SQL对象只存储targetDB,targetTableName;执行时不改SQL,不改参数.
						 */
						Sql actionSql = new Sql();
						actionSql.setDataSource(dataSourceDispatcher.getDataSourceWhithParttionName(pt.getObject1()));
						actionSql.setPartition(pt.getObject1());
						actionSql.setLogicTableName(pt.getObject2());
						routeResult.addSql(actionSql);
					}
					routeResult.setResultType(RouteResultType.ALL);
				}else{
					routeResult.setResultType(RouteResultType.NO);
				}
				return routeResult;
				
			}else{
				// TODO 有shardingKey命中,目前只支持路由到一个分区,一个表.以后改进.比如 whre id in()
				Pair<String/*targetDB*/, String/*targetTable*/> target = rule.executeRule(param);
				RouteResult routeResult = new RouteResult();
				if(target == null || target.getObject1() == null || target.getObject2() == null){
					// TODO 日志记录
					routeResult.setResultType(RouteResultType.NO);
				}
				routeResult.setResultType(RouteResultType.PARTITION);
				Sql actionSql = new Sql();
				actionSql.setDataSource(dataSourceDispatcher.getDataSourceWhithParttionName(target.getObject1()));
				actionSql.setLogicTableName(tableName);
				actionSql.setSqlReWrite(true);
				actionSql.setOriginalSql(null);
				actionSql.setTargetSql(null);//TODO 替换表名
				actionSql.setTargetTableName(target.getObject2());
				actionSql.setReWriteParameter(null); // TODO 暂时不实现参数重写,所以这里继续使用Mybatis的参数
				routeResult.addSql(actionSql);
				return routeResult;
			}
		}
	}
	
	public List<ITableRule> getTableRules() {
		return tableRules;
	}

	public void setTableRules(List<ITableRule> tableRules) {
		this.tableRules = tableRules;
	}

	public DataSourceDispatcher getDataSourceDispatcher() {
		return dataSourceDispatcher;
	}

	public void setDataSourceDispatcher(DataSourceDispatcher dataSourceDispatcher) {
		this.dataSourceDispatcher = dataSourceDispatcher;
	}

	@Override
	public RouteResult doRoute(String statement, Object parameter) {
		throw new RuntimeException("not support now!");
	}
}
