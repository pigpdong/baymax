package com.tongbanjie.baymax.jdbc.explan;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by sidawei on 16/4/15.
 *
 * TODO ExplanResultSet对应的MetaData
 */
public class ExplanResultSetMetaDate implements ResultSetMetaData {
    @Override
    public int getColumnCount() throws SQLException {
        return 0;
    }

    @Override
    public boolean isAutoIncrement(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean isCaseSensitive(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean isSearchable(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean isCurrency(int i) throws SQLException {
        return false;
    }

    @Override
    public int isNullable(int i) throws SQLException {
        return 0;
    }

    @Override
    public boolean isSigned(int i) throws SQLException {
        return false;
    }

    @Override
    public int getColumnDisplaySize(int i) throws SQLException {
        return 0;
    }

    @Override
    public String getColumnLabel(int i) throws SQLException {
        return null;
    }

    @Override
    public String getColumnName(int i) throws SQLException {
        return null;
    }

    @Override
    public String getSchemaName(int i) throws SQLException {
        return null;
    }

    @Override
    public int getPrecision(int i) throws SQLException {
        return 0;
    }

    @Override
    public int getScale(int i) throws SQLException {
        return 0;
    }

    @Override
    public String getTableName(int i) throws SQLException {
        return null;
    }

    @Override
    public String getCatalogName(int i) throws SQLException {
        return null;
    }

    @Override
    public int getColumnType(int i) throws SQLException {
        return 0;
    }

    @Override
    public String getColumnTypeName(int i) throws SQLException {
        return null;
    }

    @Override
    public boolean isReadOnly(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean isWritable(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean isDefinitelyWritable(int i) throws SQLException {
        return false;
    }

    @Override
    public String getColumnClassName(int i) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return false;
    }
}
