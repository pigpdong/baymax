package com.tongbanjie.baymax.jdbc;

import com.tongbanjie.baymax.jdbc.adapter.UnsupportedResultSetAdapter;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class TResultSet extends UnsupportedResultSetAdapter {

	List<ResultSet> resultSet;
	ResultSet currentResultSet;
	ResultSet metaDataResultSet;
	ResultSetMetaData metaData;
	boolean end;
	int index = 0;
	boolean isFirstSet; // 当前访问的结果是否为第一个结果集

	private TStatement statement;
	private boolean isClosed;

	public TResultSet(List<ResultSet> listResultSet, TStatement statement) {
		this.resultSet = listResultSet;
		if (listResultSet != null && listResultSet.size() > 0) {
			this.metaDataResultSet = listResultSet.get(0);
			this.currentResultSet = listResultSet.get(0);
			try {
				this.metaData = this.metaDataResultSet.getMetaData();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		this.statement = statement;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		try {
			return (T) this;
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.getClass().isAssignableFrom(iface);
	}

	@Override
	public boolean next() throws SQLException {
		if (resultSet == null || end) {
			return false;
		}
		if (index == resultSet.size()) {
			return false;
		}
		currentResultSet = resultSet.get(index);
		boolean next = currentResultSet.next();
		if (next) {
			return true;
		}else{
			index++;
		}
		return next();
	}

	@Override
	public void close() throws SQLException {
		if (isClosed) {
			return;
		}
		try {
			this.metaData = null;
			this.metaDataResultSet = null;
			SQLException exception = null; 
			if(resultSet != null){
				for(ResultSet set : resultSet){
					try{
						set.close();
					}catch(SQLException e){
						if(exception == null){
							exception = e;
						}else{
							exception.setNextException(e);
						}
					}
				}
			}
			if (exception != null) {
				throw exception;
			}
		} catch (Exception e) {
			throw new SQLException(e);

		} finally{
			isClosed = true;
		}
	}

	@Override
	public boolean wasNull() throws SQLException {
		return this.currentResultSet.wasNull();
	}

	/**
	 * TODO 暂时以第一个结果集的列为准
	 * 
	 * @return
	 */
	private boolean needEscape() {
		if (index > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 根据MetaData把columnIndex转义为columnLabel
	 * 
	 * @param columnIndex
	 * @return
	 * @throws SQLException
	 */
	private String escapeIndexColumn(int columnIndex) throws SQLException {
		return metaData.getColumnLabel(columnIndex);
	}

	@Override
	public String getString(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getString(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getString(columnIndex);
		}
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getBoolean(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getBoolean(columnIndex);
		}
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getByte(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getByte(columnIndex);
		}
	}

	@Override
	public short getShort(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getShort(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getShort(columnIndex);
		}
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getInt(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getInt(columnIndex);
		}
	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getLong(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getLong(columnIndex);
		}
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getFloat(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getFloat(columnIndex);
		}
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getDouble(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getDouble(columnIndex);
		}
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getBigDecimal(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getBigDecimal(columnIndex);
		}
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getBytes(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getBytes(columnIndex);
		}
	}

	@Override
	public Date getDate(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getDate(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getDate(columnIndex);
		}
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getTime(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getTime(columnIndex);
		}
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getTimestamp(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getTimestamp(columnIndex);
		}
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getAsciiStream(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getAsciiStream(columnIndex);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getUnicodeStream(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getUnicodeStream(columnIndex);
		}
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getBinaryStream(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getBinaryStream(columnIndex);
		}
	}

	@Override
	public String getString(String columnLabel) throws SQLException {
		return currentResultSet.getString(columnLabel);
	}

	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {
		return currentResultSet.getBoolean(columnLabel);
	}

	@Override
	public byte getByte(String columnLabel) throws SQLException {
		return currentResultSet.getByte(columnLabel);
	}

	@Override
	public short getShort(String columnLabel) throws SQLException {
		return currentResultSet.getShort(columnLabel);
	}

	@Override
	public int getInt(String columnLabel) throws SQLException {
		return currentResultSet.getInt(columnLabel);
	}

	@Override
	public long getLong(String columnLabel) throws SQLException {
		return currentResultSet.getLong(columnLabel);
	}

	@Override
	public float getFloat(String columnLabel) throws SQLException {
		return currentResultSet.getFloat(columnLabel);
	}

	@Override
	public double getDouble(String columnLabel) throws SQLException {
		return currentResultSet.getDouble(columnLabel);
	}

	@SuppressWarnings("deprecation")
	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
		return currentResultSet.getBigDecimal(columnLabel, scale);
	}

	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {
		return currentResultSet.getBytes(columnLabel);
	}

	@Override
	public Date getDate(String columnLabel) throws SQLException {
		return currentResultSet.getDate(columnLabel);
	}

	@Override
	public Time getTime(String columnLabel) throws SQLException {
		return currentResultSet.getTime(columnLabel);
	}

	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return currentResultSet.getTimestamp(columnLabel);
	}

	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		return currentResultSet.getAsciiStream(columnLabel);
	}

	@SuppressWarnings("deprecation")
	@Override
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		return currentResultSet.getUnicodeStream(columnLabel);
	}

	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		return currentResultSet.getBinaryStream(columnLabel);
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return this.currentResultSet.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		this.currentResultSet.clearWarnings();
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return this.metaData;
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getObject(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getObject(columnIndex);
		}
	}

	@Override
	public Object getObject(String columnLabel) throws SQLException {
		return currentResultSet.getObject(columnLabel);
	}

	@Override
	public int findColumn(String columnLabel) throws SQLException {
		// TODO logger
		return currentResultSet.findColumn(columnLabel);
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getCharacterStream(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getCharacterStream(columnIndex);
		}
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		return currentResultSet.getCharacterStream(columnLabel);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getBigDecimal(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getBigDecimal(columnIndex);
		}
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return currentResultSet.getBigDecimal(columnLabel);
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		if (direction != ResultSet.FETCH_FORWARD) {
			throw new UnsupportedOperationException(); // 如果不是默认方向 抛异常
		}
	}

	/**
	 * 获取此 ResultSet 对象的获取方向。
	 */
	@Override
	public int getFetchDirection() throws SQLException {
		return ResultSet.FETCH_FORWARD; // 默认方向
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		return;
	}

	/**
	 * 获取此 ResultSet 对象的获取大小
	 */
	@Override
	public int getFetchSize() throws SQLException {
		return 0; // 0 表示不做限制
	}

	/**
	 * 获取此 ResultSet 对象的类型。类型由创建结果集的 Statement 对象确定。
	 * ResultSet.TYPE_FORWARD_ONLY、 ResultSet.TYPE_SCROLL_INSENSITIVE 或
	 * ResultSet.TYPE_SCROLL_SENSITIVE
	 */
	@Override
	public int getType() throws SQLException {
		return currentResultSet.getType();
	}

	/**
	 * 获取此 ResultSet 对象的并发模式。使用的并发由创建结果集的 Statement 对象确定。 并发类型，
	 * ResultSet.CONCUR_READ_ONLY 或 ResultSet.CONCUR_UPDATABLE
	 */
	@Override
	public int getConcurrency() throws SQLException {
		return currentResultSet.getConcurrency();
	}

	/**
	 * 获取此 ResultSet 对象的可保存性 ResultSet.HOLD_CURSORS_OVER_COMMIT 或
	 * ResultSet.CLOSE_CURSORS_AT_COMMIT
	 */
	@Override
	public int getHoldability() throws SQLException {
		return currentResultSet.getHoldability();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return this.isClosed;
	}

	@Override
	public Statement getStatement() throws SQLException {
		return this.statement;
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getBigDecimal(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getBigDecimal(columnIndex);
		}
	}

	@Override
	public Ref getRef(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getRef(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getRef(columnIndex);
		}
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getBlob(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getBlob(columnIndex);
		}
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getClob(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getClob(columnIndex);
		}
	}

	@Override
	public Array getArray(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getArray(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getArray(columnIndex);
		}
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
		return currentResultSet.getObject(columnLabel, map);
	}

	@Override
	public Ref getRef(String columnLabel) throws SQLException {
		return currentResultSet.getRef(columnLabel);
	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
		return currentResultSet.getBlob(columnLabel);
	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {
		return currentResultSet.getClob(columnLabel);
	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {
		return currentResultSet.getArray(columnLabel);
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getDate(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getDate(columnIndex);
		}
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return currentResultSet.getDate(columnLabel, cal);
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getTime(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getTime(columnIndex);
		}
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		return currentResultSet.getTime(columnLabel, cal);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getTimestamp(escapeIndexColumn(columnIndex), cal);
		} else {
			return currentResultSet.getTimestamp(columnIndex, cal);
		}
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
		return currentResultSet.getTimestamp(columnLabel, cal);
	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getURL(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getURL(columnIndex);
		}
	}

	@Override
	public URL getURL(String columnLabel) throws SQLException {
		return currentResultSet.getURL(columnLabel);
	}

	/**
	 * SQL ROWID 值在 Java 编程语言中的表示形式（映射）。 SQL ROWID
	 * 是一种内置类型，其值可视为它标识的行在数据库表中的一个地址。 该地址是逻辑的还是物理的（在某些方面）取决于它的原始数据源。
	 */
	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		// TODO logger
		throw new UnsupportedOperationException();
	}

	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		// TODO what?
		throw new UnsupportedOperationException();
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getNClob(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getNClob(columnIndex);
		}
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		return currentResultSet.getNClob(columnLabel);
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getSQLXML(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getSQLXML(columnIndex);
		}
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return currentResultSet.getSQLXML(columnLabel);
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getNString(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getNString(columnIndex);
		}
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		return currentResultSet.getNString(columnLabel);
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		if (needEscape()) {
			return currentResultSet.getNCharacterStream(escapeIndexColumn(columnIndex));
		} else {
			return currentResultSet.getNCharacterStream(columnIndex);
		}
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return currentResultSet.getNCharacterStream(columnLabel);
	}
}
