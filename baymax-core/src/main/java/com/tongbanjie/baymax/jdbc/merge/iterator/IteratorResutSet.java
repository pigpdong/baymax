package com.tongbanjie.baymax.jdbc.merge.iterator;

import com.tongbanjie.baymax.jdbc.TStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by sidawei on 16/1/31.
 *
 * Forward_Only
 */
public class IteratorResutSet extends IteratorResultSetGetterAdapter {

    protected int       index = 0;
    protected boolean   end;

    public IteratorResutSet(List<ResultSet> listResultSet, TStatement statement) {
        super(listResultSet, statement);
    }

    @Override
    public boolean next() throws SQLException {
        if (getResultSet() == null || end) {
            return false;
        }
        if (index == getResultSet().size()) {
            return false;
        }
        currentResultSet = getResultSet().get(index);
        boolean next = currentResultSet.next();
        if (next) {
            return true;
        }else{
            index++;
        }
        return next();
    }

    /**
     * TODO 暂时以第一个结果集的列为准
     *
     * @return
     */
    public boolean needEscape() {
        if (index > 0) {
            return true;
        } else {
            return false;
        }
    }
}
