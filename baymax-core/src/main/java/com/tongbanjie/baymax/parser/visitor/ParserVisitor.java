package com.tongbanjie.baymax.parser.visitor;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.stat.TableStat.Mode;
import com.alibaba.druid.wall.spi.WallVisitorUtils;

import java.util.List;
import java.util.Map;

/**
 * Druid解析器中用来从ast语法中提取表名、条件、字段等的vistor
 * @author wang.dw
 *
 */
public class ParserVisitor extends MySqlSchemaStatVisitor {

	private boolean                 hasOrCondition          = false;

    public ParserVisitor(List<Object> parameters){
        setParameters(parameters);
    }

	public boolean hasOrCondition() {
		return hasOrCondition;
	}
	
    @Override
    public boolean visit(SQLSelectStatement x) {
        setAliasMap();
        return true;
    }

    /**
     *
     * @param x
     * @return
     */
    @Override
	public boolean visit(SQLBinaryOpExpr x) {
        //System.out.println(String.format("--[%s]:[%s]:[%s]--", new String[]{x.getLeft().toString(),x.getOperator().toString(), x.getRight().toString()}).replaceAll("\n",""));
        x.getLeft().setParent(x);
        x.getRight().setParent(x);

        switch (x.getOperator()) {
            case Equality:
            case LessThanOrEqualOrGreaterThan:
            case Is:
            case IsNot:
                // a=1 and a=2 or a=3
                handleCondition(x.getLeft(), x.getOperator().name, x.getRight());
                // a=b 转化为 b=a
                // a=1 不用转化
                handleCondition(x.getRight(), x.getOperator().name, x.getLeft());
                handleRelationship(x.getLeft(), x.getOperator().name, x.getRight());
                break;
            case BooleanOr:
                //永真条件，where条件抛弃
//                if(!isConditionAlwaysTrue(x)) {
//                    hasOrCondition = true;
//                    // TODO 暂时只支持作为第一条件
//
//                    WhereUnit whereUnit = null;
//                    if(conditions.size() > 0) {
//                        whereUnit = new WhereUnit();
//                        whereUnit.setFinishedParse(true);
//                        whereUnit.addOutConditions(getConditions());
//                        WhereUnit innerWhereUnit = new WhereUnit(x);
//                        whereUnit.addSubWhereUnit(innerWhereUnit);
//                    } else {
//                        whereUnit = new WhereUnit(x);
//                        whereUnit.addOutConditions(getConditions());
//                    }
//                    whereUnits.add(whereUnit);
//                    System.out.println("--{}");
//                }
                break;
            case Like:
            case NotLike:
            case NotEqual:
            case GreaterThan:
            case GreaterThanOrEqual:
            case LessThan:
            case LessThanOrEqual:
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean visit(MySqlDeleteStatement x) {
        setAliasMap();

        setMode(x, Mode.Delete);

        accept(x.getFrom());
        accept(x.getUsing());
        x.getTableSource().accept(this);

        if (x.getTableSource() instanceof SQLExprTableSource) {
            SQLName tableName = (SQLName) ((SQLExprTableSource) x.getTableSource()).getExpr();
            String ident = tableName.toString();
            setCurrentTable(x, ident);
            // 和父类只有这行不同
            TableStat stat = this.getTableStat(ident,ident);
            stat.incrementDeleteCount();
        }

        accept(x.getWhere());

        accept(x.getOrderBy());
        accept(x.getLimit());

        return false;
    }

    @Override
    public void endVisit(MySqlDeleteStatement x) {

    }

    @Override
    public boolean visit(SQLUpdateStatement x) {
        setAliasMap();

        setMode(x, Mode.Update);

        SQLName identName = x.getTableName();
        if (identName != null) {
            String ident = identName.toString();
            //
            String alias = x.getTableSource().getAlias();
            setCurrentTable(ident);

            TableStat stat = getTableStat(ident);
            stat.incrementUpdateCount();

            Map<String, String> aliasMap = getAliasMap();
            
            aliasMap.put(ident, ident);
            //
            if(alias != null) {
            	aliasMap.put(alias, ident);
            }
        } else {
            x.getTableSource().accept(this);
        }

        accept(x.getItems());
        accept(x.getWhere());

        return false;
    }

    /**
     * 判断条件是否永假的
     * @param expr
     * @return
     */
    public boolean isConditionAlwaysFalse(SQLExpr expr) {
        Object o = WallVisitorUtils.getValue(expr);
        if(Boolean.FALSE.equals(o)) {
            return true;
        }
        return false;
    }

    public boolean isConditionAlwaysTrue(SQLExpr expr) {
        Object o = WallVisitorUtils.getValue(expr);
        if(Boolean.TRUE.equals(o)) {
            return true;
        }
        return false;
    }
}
