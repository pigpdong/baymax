package com.tongbanjie.baymax.parser.druid;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat.Condition;
import com.tongbanjie.baymax.parser.druid.DruidSqlParser;
import com.tongbanjie.baymax.parser.druid.impl.MycatSchemaStatVisitor;
import com.tongbanjie.baymax.parser.druid.model.RangeValue;
import com.tongbanjie.baymax.parser.druid.model.RouteCalculateUnit;
import com.tongbanjie.baymax.parser.druid.model.ParseResult;
import com.tongbanjie.baymax.partition.PartitionCaculate;
import com.tongbanjie.baymax.router.model.ExecutePlan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractDruidSqlParser implements DruidSqlParser{
	
	private Map<String,String> tableAliasMap = new HashMap<String,String>();

	@Override
	public ExecutePlan parse(String sql) {
		
		SQLStatementParser 		parser 			= new MySqlStatementParser(sql); 
		MycatSchemaStatVisitor  visitor 		= new MycatSchemaStatVisitor();
		SQLStatement 			statement 		= parser.parseStatement();
		
		ParseResult             result 			= new ParseResult();
		
		// 通过visitor解析
		visitorParse(statement, visitor, result);

        // 路由
        ExecutePlan plan = route(result, statement);

		// 通过Statement解析
		statementParse(statement, result);

		// 改写sql：如insert语句主键自增长的可以
		changeSql(result, statement);
		
		return plan;
		
		//statement.accept(outv);
		//List<Condition> conditions = visitor.getConditions();
	}

	protected abstract void changeSql(ParseResult result, SQLStatement statement);

    protected ExecutePlan route(ParseResult result, SQLStatement statement){
        return PartitionCaculate.caculate(result);
    }

    protected abstract void statementParse(SQLStatement statement, ParseResult result);

	private void visitorParse(SQLStatement stmt, MycatSchemaStatVisitor visitor, ParseResult result) {

		stmt.accept(visitor);
		
		List<List<Condition>> mergedConditionList = new ArrayList<List<Condition>>();
		if(visitor.hasOrCondition()) {//包含or语句
			//TODO
			//根据or拆分
			mergedConditionList = visitor.splitConditions();
		} else {//不包含OR语句
			mergedConditionList.add(visitor.getConditions());
		}
		
		if(visitor.getAliasMap() != null) {
			for(Map.Entry<String, String> entry : visitor.getAliasMap().entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if(key != null && key.indexOf("`") >= 0) {
					key = key.replaceAll("`", "");
				}
				if(value != null && value.indexOf("`") >= 0) {
					value = value.replaceAll("`", "");
				}
				//表名前面带database的，去掉
				if(key != null) {
					int pos = key.indexOf(".");
					if(pos> 0) {
						key = key.substring(pos + 1);
					}
				}
				
				if(key.equals(value)) {
					result.addTable(key.toUpperCase());
				} 
// 原
//				else {
//					tableAliasMap.put(key, value);
//				}
				tableAliasMap.put(key.toUpperCase(), value);
			}
			visitor.getAliasMap().putAll(tableAliasMap);
			result.setTableAliasMap(tableAliasMap);
		}
		result.setRouteCalculateUnits(this.buildRouteCalculateUnits(visitor, mergedConditionList));
		
	}
	
	private List<RouteCalculateUnit> buildRouteCalculateUnits(SchemaStatVisitor visitor, List<List<Condition>> conditionList) {
		List<RouteCalculateUnit> retList = new ArrayList<RouteCalculateUnit>();
		//遍历condition ，找分片字段
		for(int i = 0; i < conditionList.size(); i++) {
			RouteCalculateUnit routeCalculateUnit = new RouteCalculateUnit();
			for(Condition condition : conditionList.get(i)) {
				List<Object> values = condition.getValues();
				if(values.size() == 0) {
					break;
				}
				if(checkConditionValues(values)) {
					String columnName = removeBackquote(condition.getColumn().getName().toUpperCase());
					String tableName = removeBackquote(condition.getColumn().getTable().toUpperCase());
					
					if(visitor.getAliasMap() != null && visitor.getAliasMap().get(tableName) != null 
							&& !visitor.getAliasMap().get(tableName).equals(tableName)) {
						tableName = visitor.getAliasMap().get(tableName);
					}
					
					if(visitor.getAliasMap() != null && visitor.getAliasMap().get(condition.getColumn().getTable().toUpperCase()) == null) {//子查询的别名条件忽略掉,不参数路由计算，否则后面找不到表
						continue;
					}
					
					String operator = condition.getOperator();
					
					//只处理between ,in和=3中操作符
					if(operator.equals("between")) {
						RangeValue rv = new RangeValue(values.get(0), values.get(1), RangeValue.EE);
								routeCalculateUnit.addShardingExpr(tableName.toUpperCase(), columnName, rv);
					} else if(operator.equals("=") || operator.toLowerCase().equals("in")){ //只处理=号和in操作符,其他忽略
								routeCalculateUnit.addShardingExpr(tableName.toUpperCase(), columnName, values.toArray());
					}
				}
			}
			retList.add(routeCalculateUnit);
		}
		return retList;
	}
	
	private boolean checkConditionValues(List<Object> values) {
		for(Object value : values) {
			if(value != null && !value.toString().equals("")) {
				return true;
			}
		}
		return false;
	}
	
	public static String removeBackquote(String str){
		//删除名字中的`tablename`和'value'
		if (str.length() > 0) {
			StringBuilder sb = new StringBuilder(str);
			if (sb.charAt(0) == '`'||sb.charAt(0) == '\'') {
				sb.deleteCharAt(0);
			}
			if (sb.charAt(sb.length() - 1) == '`'||sb.charAt(sb.length() - 1) == '\'') {
				sb.deleteCharAt(sb.length() - 1);
			}
			return sb.toString();
		}
		return "";
	}

}
