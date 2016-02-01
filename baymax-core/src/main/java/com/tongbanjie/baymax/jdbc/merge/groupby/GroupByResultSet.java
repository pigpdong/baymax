package com.tongbanjie.baymax.jdbc.merge.groupby;

import com.tongbanjie.baymax.jdbc.TStatement;
import com.tongbanjie.baymax.jdbc.merge.iterator.IteratorResultSetGetterAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by sidawei on 16/1/31.
 *
 *
 * groupby + orderby
 *
 * agg + groupby + orderby
 *
 * agg + groupby
 *
 * groupby
 *
 * orderby
 *
 * agg
 *
 * none
 *
 *
 * 注意：暂时不考虑having中有聚合函数的请款,因为需要合并聚合函数的值,然后重新计算表达式
 *
 */
public class GroupByResultSet extends IteratorResultSetGetterAdapter {

    public GroupByResultSet(List<ResultSet> listResultSet, TStatement statement) {
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
