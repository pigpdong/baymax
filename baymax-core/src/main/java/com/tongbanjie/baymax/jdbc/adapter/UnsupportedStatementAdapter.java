package com.tongbanjie.baymax.jdbc.adapter;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by sidawei on 16/1/26.
 */
public abstract class UnsupportedStatementAdapter implements Statement {

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw new UnsupportedOperationException("setEscapeProcessing");
    }

    @Override
    public void cancel() throws SQLException {
        throw new UnsupportedOperationException("cancel");
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        throw new UnsupportedOperationException("setCursorName");
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        throw new UnsupportedOperationException("getMoreResults");
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        throw new UnsupportedOperationException("getMoreResults");
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public boolean isPoolable() throws SQLException {
        throw new SQLException("not support exception");
    }
}
