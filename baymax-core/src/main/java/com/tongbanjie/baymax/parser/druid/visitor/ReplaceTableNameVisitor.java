package com.tongbanjie.baymax.parser.druid.visitor;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import com.tongbanjie.baymax.exception.BayMaxException;

/**
 * Created by sidawei on 16/1/27.
 */
public class ReplaceTableNameVisitor extends SQLASTVisitorAdapter {

    private String originalName;

    private String newName;

    private boolean isReplase = false;

    public ReplaceTableNameVisitor(String originalName, String newName){
        if (originalName == null || originalName.length() == 0
                || newName == null || newName.length() == 0){
            throw new BayMaxException("替换表名不能为空:" + originalName + "," +newName);
        }

        this.originalName = originalName;
        this.newName = newName;
    }
    @Override
    public boolean visit(SQLExprTableSource astNode) {
        if (astNode.toString().equals(originalName)){
            if (isReplase){
                throw new BayMaxException("分区表名在一个Sql中只能出现一次:" + originalName + "," +newName);
            }else {
                ((SQLIdentifierExpr)astNode.getExpr()).setName(newName);
                isReplase = true;
            }
        }
        return true;
    }
}
