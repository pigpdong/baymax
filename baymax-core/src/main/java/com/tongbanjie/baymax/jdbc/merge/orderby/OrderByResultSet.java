package com.tongbanjie.baymax.jdbc.merge.orderby;

import com.tongbanjie.baymax.jdbc.TStatement;
import com.tongbanjie.baymax.jdbc.merge.iterator.IteratorResultSetGetterAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by sidawei on 16/1/31.
 */
public class OrderByResultSet extends IteratorResultSetGetterAdapter {

    public OrderByResultSet(List<ResultSet> listResultSet, TStatement statement) {
        super(listResultSet, statement);
    }

    @Override
    public boolean next() throws SQLException {
        return false;
    }

    @Override
    public boolean needEscape() {
        return false;
    }
}
