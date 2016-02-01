package com.tongbanjie.baymax.jdbc.merge;

public class OrderbyColumn {

    private String      columnName;
    private int         columnIndex;
	private OderbyType   orderbyType;

	public OrderbyColumn(String columnName, int columnIndex, OderbyType orderbyType) {
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.orderbyType = orderbyType;
    }

    public String getColumnName() {
        return columnName;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public OderbyType getOrderbyType() {
        return orderbyType;
    }

    /**
     * 聚合函数合并类型
     */
    public enum OderbyType{
            ASC,DESC;
    }
}