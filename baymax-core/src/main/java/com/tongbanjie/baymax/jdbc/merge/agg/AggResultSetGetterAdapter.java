package com.tongbanjie.baymax.jdbc.merge.agg;

import com.tongbanjie.baymax.jdbc.TResultSet;
import com.tongbanjie.baymax.jdbc.TResultSetLimit;
import com.tongbanjie.baymax.jdbc.TStatement;
import com.tongbanjie.baymax.router.model.ExecutePlan;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public abstract class AggResultSetGetterAdapter extends TResultSetLimit {


    public AggResultSetGetterAdapter(List<ResultSet> listResultSet, TStatement statement, ExecutePlan plan) {
        super(listResultSet, statement, plan);
    }

    public abstract boolean isAggColumn(String name);

    public abstract boolean isAggColumn(int index);

    public abstract <T> T merge(String columnLabel, Class<T> type) throws SQLException;

    public abstract <T> T merge(int columnIndex, Class<T> type) throws SQLException;

    /*------------------------------------------------------------------------*/

	@Override
	public String getString(int columnIndex) throws SQLException {
		if (isAggColumn(columnIndex)) {
            return merge(columnIndex, String.class);
		} else {
			return currentResultSet.getString(columnIndex);
		}
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, boolean.class);
		} else {
			return currentResultSet.getBoolean(columnIndex);
		}
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, byte.class);
		} else {
			return currentResultSet.getByte(columnIndex);
		}
	}

	@Override
	public short getShort(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, short.class);
		} else {
			return currentResultSet.getShort(columnIndex);
		}
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, int.class);
		} else {
			return currentResultSet.getInt(columnIndex);
		}
	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, long.class);
		} else {
			return currentResultSet.getLong(columnIndex);
		}
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, float.class);
		} else {
			return currentResultSet.getFloat(columnIndex);
		}
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, double.class);
		} else {
			return currentResultSet.getDouble(columnIndex);
		}
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, BigDecimal.class);
		} else {
			return currentResultSet.getBigDecimal(columnIndex);
		}
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, byte[].class);
		} else {
			return currentResultSet.getBytes(columnIndex);
		}
	}

	@Override
	public Date getDate(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, Date.class);
		} else {
			return currentResultSet.getDate(columnIndex);
		}
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, Time.class);
		} else {
			return currentResultSet.getTime(columnIndex);
		}
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, Timestamp.class);
		} else {
			return currentResultSet.getTimestamp(columnIndex);
		}
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, InputStream.class);
		} else {
			return currentResultSet.getAsciiStream(columnIndex);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, InputStream.class);
		} else {
			return currentResultSet.getUnicodeStream(columnIndex);
		}
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, InputStream.class);
		} else {
			return currentResultSet.getBinaryStream(columnIndex);
		}
	}

	@Override
	public String getString(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, String.class);
        } else {
		    return currentResultSet.getString(columnLabel);
        }
	}

	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, boolean.class);
        } else {
		    return currentResultSet.getBoolean(columnLabel);
        }
	}

	@Override
	public byte getByte(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, byte.class);
        } else {
		    return currentResultSet.getByte(columnLabel);
        }
	}

	@Override
	public short getShort(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, short.class);
        } else {
		    return currentResultSet.getShort(columnLabel);
        }
	}

	@Override
	public int getInt(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, int.class);
        } else {
		    return currentResultSet.getInt(columnLabel);
        }
	}

	@Override
	public long getLong(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, long.class);
        } else {
		    return currentResultSet.getLong(columnLabel);
        }
	}

	@Override
	public float getFloat(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, float.class);
        } else {
		    return currentResultSet.getFloat(columnLabel);
        }
	}

	@Override
	public double getDouble(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, double.class);
        } else {
	    	return currentResultSet.getDouble(columnLabel);
        }
	}

	@SuppressWarnings("deprecation")
	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, BigDecimal.class);
        } else {
		    return currentResultSet.getBigDecimal(columnLabel, scale);
        }
	}

	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, byte[].class);
        } else {
		    return currentResultSet.getBytes(columnLabel);
        }
	}

	@Override
	public Date getDate(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, Date.class);
        } else {
		    return currentResultSet.getDate(columnLabel);
        }
	}

	@Override
	public Time getTime(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, Time.class);
        } else {
		    return currentResultSet.getTime(columnLabel);
        }
	}

	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, Timestamp.class);
        } else {
		    return currentResultSet.getTimestamp(columnLabel);
        }
	}

	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, InputStream.class);
        } else {
		    return currentResultSet.getAsciiStream(columnLabel);
        }
	}

	@SuppressWarnings("deprecation")
	@Override
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, InputStream.class);
        } else {
		    return currentResultSet.getUnicodeStream(columnLabel);
        }
	}

	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, InputStream.class);
        } else {
		    return currentResultSet.getBinaryStream(columnLabel);
        }
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, Object.class);
        } else {
			return currentResultSet.getObject(columnIndex);
        }
	}

	@Override
	public Object getObject(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, Object.class);
        } else {
		    return currentResultSet.getObject(columnLabel);
        }
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, Reader.class);
        } else {
			return currentResultSet.getCharacterStream(columnIndex);
        }
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, Reader.class);
        } else {
		    return currentResultSet.getCharacterStream(columnLabel);
        }
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, BigDecimal.class);
        } else {
			return currentResultSet.getBigDecimal(columnIndex);
        }
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, BigDecimal.class);
        } else {
		    return currentResultSet.getBigDecimal(columnLabel);
        }
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, Object.class);
        } else {
			return currentResultSet.getObject(columnIndex);
        }
	}

	@Override
	public Ref getRef(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, Ref.class);
        } else {
			return currentResultSet.getRef(columnIndex);
        }
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, Blob.class);
        } else {
			return currentResultSet.getBlob(columnIndex);
        }
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, Clob.class);
        } else {
			return currentResultSet.getClob(columnIndex);
        }
	}

	@Override
	public Array getArray(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, Array.class);
        } else {
			return currentResultSet.getArray(columnIndex);
        }
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, Object.class);
        } else {
		    return currentResultSet.getObject(columnLabel, map);
        }
	}

	@Override
	public Ref getRef(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, Ref.class);
        } else {
		    return currentResultSet.getRef(columnLabel);
        }
	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, Blob.class);
        } else {
		    return currentResultSet.getBlob(columnLabel);
        }
	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, Clob.class);
        } else {
		    return currentResultSet.getClob(columnLabel);
        }
	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, Array.class);
        } else {
		    return currentResultSet.getArray(columnLabel);
        }
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, Date.class);
        } else {
			return currentResultSet.getDate(columnIndex);
        }
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, Date.class);
        } else {
		    return currentResultSet.getDate(columnLabel, cal);
        }
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, Time.class);
        } else {
			return currentResultSet.getTime(columnIndex);
        }
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, Time.class);
        } else {
		    return currentResultSet.getTime(columnLabel, cal);
        }
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, Timestamp.class);
        } else {
			return currentResultSet.getTimestamp(columnIndex, cal);
        }
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, Timestamp.class);
        } else {
		    return currentResultSet.getTimestamp(columnLabel, cal);
        }
	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, URL.class);
        } else {
			return currentResultSet.getURL(columnIndex);
        }
	}

	@Override
	public URL getURL(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, URL.class);
        } else {
		    return currentResultSet.getURL(columnLabel);
        }
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, NClob.class);
        } else {
			return currentResultSet.getNClob(columnIndex);
        }
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, NClob.class);
        } else {
		    return currentResultSet.getNClob(columnLabel);
        }
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, SQLXML.class);
        } else {
			return currentResultSet.getSQLXML(columnIndex);
        }
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, SQLXML.class);
        } else {
		    return currentResultSet.getSQLXML(columnLabel);
        }
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, String.class);
        } else {
			return currentResultSet.getNString(columnIndex);
        }
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, String.class);
        } else {
		    return currentResultSet.getNString(columnLabel);
        }
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
        if (isAggColumn(columnIndex)) {
            return merge(columnIndex, Reader.class);
        } else {
			return currentResultSet.getNCharacterStream(columnIndex);
        }
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
        if (isAggColumn(columnLabel)) {
            return merge(columnLabel, Reader.class);
        } else {
		    return currentResultSet.getNCharacterStream(columnLabel);
        }
	}

    /*-----------------------------------------------------------------------*/

}
