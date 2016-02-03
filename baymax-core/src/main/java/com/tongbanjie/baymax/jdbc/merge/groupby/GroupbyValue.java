package com.tongbanjie.baymax.jdbc.merge.groupby;

import com.tongbanjie.baymax.jdbc.merge.DataConvert;
import com.tongbanjie.baymax.jdbc.merge.MergeColumn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by sidawei on 16/2/3.
 *
 * 代表分组中的一行数据, 对于聚合函数,有合并功能
 */
public class GroupbyValue {
    /**
     * 一行中 每一列的值
     */
    private Object[] valus;

    private GroupbyMetaData metaData;

    public GroupbyValue(ResultSet set, GroupbyMetaData metaData, Map<String, MergeColumn.MergeType> aggColumns) throws SQLException {
        this(set, metaData, aggColumns, null);
    }

    /**
     *
     * @param set
     * @param other     同组的上一行
     * @throws SQLException
     */
    public GroupbyValue(ResultSet set, GroupbyMetaData metaData, Map<String, MergeColumn.MergeType> aggColumns, GroupbyValue other) throws SQLException {
        this.metaData = metaData;
        int size = metaData.getColumnCount();
        valus = new Object[size + 1];
        for (int i = 1; i< size + 1; i++){
            String columnLabel = metaData.getColumnLabel(i);
            valus[i] = set.getObject(columnLabel);
            if (other != null && other.getValus() != null && other.getValus().length > 0){
                // TODO 合并
                if (aggColumns.containsKey(columnLabel)){
                    MergeColumn.MergeType mergeType = aggColumns.get(columnLabel);
                    valus[i] = GroupbyAggMerger.merge(valus[i], other.getValus()[i], mergeType);
                }
            }
        }
    }

    public Object[] getValus() {
        return valus;
    }

    public <T> T getValue(int index, Class<T> type){
        return (T)DataConvert.convertValue(valus[index], type);
    }

    public <T> T getValue(String columnLabel, Class<T> type){
        return (T)DataConvert.convertValue(valus[metaData.getColumnIndex(columnLabel)], type);
    }
}
