package com.tongbanjie.baymax.jdbc.adapter;

import com.tongbanjie.baymax.jdbc.TConnection;
import com.tongbanjie.baymax.jdbc.TStatement;
import com.tongbanjie.baymax.jdbc.model.StatementCreateCommand;

import java.io.InputStream;
import java.io.Reader;
import java.sql.*;

/**
 * Created by sidawei on 16/1/26.
 */
public abstract class UnsupportedPreparedStatementAdapter extends TStatement implements PreparedStatement {

    public UnsupportedPreparedStatementAdapter(TConnection connection, StatementCreateCommand statementCreateCommand) {
        super(connection, statementCreateCommand);
    }
    // ----------------- 不支持的方法 ------------------

    /**
     * 不支持
     */
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        throw new UnsupportedOperationException("getMetaData");
    }

    @Override
    public boolean isClosed() throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public boolean isPoolable() throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        throw new UnsupportedOperationException("getParameterMetaData");
    }

}
