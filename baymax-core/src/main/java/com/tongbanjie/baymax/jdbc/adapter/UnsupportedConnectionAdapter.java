package com.tongbanjie.baymax.jdbc.adapter;

import java.sql.*;
import java.util.Map;
import java.util.Properties;

/**
 * Created by sidawei on 16/1/26.
 */
public abstract class UnsupportedConnectionAdapter implements Connection{

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        throw new RuntimeException("not support exception");
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        throw new RuntimeException("not support exception");
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw new SQLException("not support exception");
    }


    @Override
    public Clob createClob() throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public Blob createBlob() throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public NClob createNClob() throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        throw new UnsupportedOperationException("getTypeMap");
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throw new UnsupportedOperationException("setTypeMap");
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        throw new UnsupportedOperationException("setHoldability");
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        throw new UnsupportedOperationException("setSavepoint");
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        throw new UnsupportedOperationException("setSavepoint");
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        throw new UnsupportedOperationException("rollback savepoint");
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw new UnsupportedOperationException("releaseSavepoint");
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        throw new UnsupportedOperationException("nativeSQL");
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        throw new UnsupportedOperationException("setCatalog");
    }

    @Override
    public String getCatalog() throws SQLException {
        throw new UnsupportedOperationException("getCatalog");
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new UnsupportedOperationException();
    }
}
