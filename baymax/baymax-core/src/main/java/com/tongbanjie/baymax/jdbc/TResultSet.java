package com.tongbanjie.baymax.jdbc;

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

public class TResultSet implements ResultSet {

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
		index++;
		boolean next = currentResultSet.next();
		if (next) {
			return true;
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
	public String getCursorName() throws SQLException {
		throw new UnsupportedOperationException();
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
	public boolean isBeforeFirst() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isFirst() throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 获取光标是否位于此 ResultSet 对象的最后一行。 注：调用 isLast 方法可能开销很大，因为 JDBC
	 * 驱动程序可能需要再往后获取一行，以确定当前行是否为结果集中的最后一行。 对于带有 TYPE_FORWARD_ONLY 的结果集类型的
	 * ResultSet，对 isLast 方法的支持是可选的
	 */
	@Override
	public boolean isLast() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void beforeFirst() throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 将光标移动到此 ResultSet 对象的末尾，正好位于最后一行之后。如果结果集中不包含任何行，则此方法无效。
	 */
	@Override
	public void afterLast() throws SQLException {
		throw new UnsupportedOperationException();

	}

	@Override
	public boolean first() throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 将光标移动到此 ResultSet 对象的最后一行。
	 */
	@Override
	public boolean last() throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 获取当前行编号。第一行为 1 号，第二行为 2 号，依此类推。 对于带有 TYPE_FORWARD_ONLY 的结果集类型的
	 * ResultSet，对 getRow 方法的支持是可选的
	 */
	@Override
	public int getRow() throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 将光标移动到此 ResultSet 对象的给定行编号。
	 */
	@Override
	public boolean absolute(int row) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 按相对行数（或正或负）移动光标。
	 */
	@Override
	public boolean relative(int rows) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 将光标移动到此 ResultSet 对象的上一行。
	 */
	@Override
	public boolean previous() throws SQLException {
		throw new UnsupportedOperationException();
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
	public boolean rowUpdated() throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 获取当前行是否已有插入。返回值取决于此 ResultSet 对象是否可以检测到可见插入。
	 */
	@Override
	public boolean rowInserted() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 从此 ResultSet 对象和底层数据库中删除当前行。光标不位于插入行上时不能调用此方法。
	 */
	@Override
	public void deleteRow() throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 用数据库中的最近值刷新当前行。光标不位于插入行上时不能调用此方法。
	 */
	@Override
	public void refreshRow() throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 取消对 ResultSet 对象中的当前行所作的更新。
	 */
	@Override
	public void cancelRowUpdates() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void moveToInsertRow() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void moveToCurrentRow() throws SQLException {
		throw new UnsupportedOperationException();
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

	/*------------------------------------------------------*/

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNull(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBoolean(String columnLabel, boolean x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateByte(String columnLabel, byte x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateShort(String columnLabel, short x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateInt(String columnLabel, int x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateLong(String columnLabel, long x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateFloat(String columnLabel, float x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateDouble(String columnLabel, double x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateString(String columnLabel, String x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateDate(String columnLabel, Date x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateTime(String columnLabel, Time x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateObject(String columnLabel, Object x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void insertRow() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateRow() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateArray(int columnIndex, Array x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateArray(String columnLabel, Array x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNString(int columnIndex, String nString) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNString(String columnLabel, String nString) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException();
	}

}
