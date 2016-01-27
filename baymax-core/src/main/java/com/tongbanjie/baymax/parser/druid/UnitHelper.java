package com.tongbanjie.baymax.parser.druid;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sidawei on 16/1/26.
 */
public class UnitHelper {

    private List<Unit> stack = new LinkedList<Unit>();

    private List<Unit> units = new LinkedList<Unit>();

    /**
     * 一个计算单元
     */
    private static class Unit{

        public SQLBinaryOpExpr name;

        /**
         * 单元内参数
         */
        public List<SQLBinaryOpExpr> conditions = new LinkedList<SQLBinaryOpExpr>();

    }

    private Unit newUnit(SQLBinaryOpExpr expr){
        Unit u = new Unit();
        u.name = expr;
        units.add(u);
        stack.add(u);

        return u;
    }

    private void endUnit(){
        if (stack.size() > 0){
            stack.remove(stack.size()-1);
        }
    }

    private Unit addCondition(SQLBinaryOpExpr expr){
        Unit u = stack.get(stack.size() - 1);
        u.conditions.add(expr);
        return u;
    }

    /**
     * 加入
     * @param x
     */
    public void holder(SQLBinaryOpExpr x){
        if (x.getParent() instanceof MySqlSelectQueryBlock){
            if (units.size() == 0) {
                throw new RuntimeException("这里不可能有值");
            }
            // 创建第一个计算单元
            newUnit(new SQLBinaryOpExpr(x.getLeft(), x.getOperator(), x.getRight(), x.getDbType()));
            return;
        }
        if (x.getOperator() == SQLBinaryOperator.BooleanOr){
            if (x.getParent() instanceof SQLBinaryOpExpr){
                if (((SQLBinaryOpExpr) x.getParent()).getOperator() == SQLBinaryOperator.BooleanAnd){
                    // 旋转
                    //SQLBinaryOpExpr parent = units.get(units.size() - 1);
//                    if (parent.getOperator() == SQLBinaryOperator.BooleanAnd){
//                        // 挖坑
//
//                        units.add(new SQLBinaryOpExpr(parent.getLeft(), SQLBinaryOperator.BooleanAnd, null));
//                        return true;
//                    }else {
//                        throw new RuntimeException("不可能");
//                    }
                }
            }else {
                throw new RuntimeException("不可能");
            }
        }
    }


}
