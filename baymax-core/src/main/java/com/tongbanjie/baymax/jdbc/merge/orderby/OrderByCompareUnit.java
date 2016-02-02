package com.tongbanjie.baymax.jdbc.merge.orderby;

import com.tongbanjie.baymax.exception.BayMaxException;
import com.tongbanjie.baymax.jdbc.merge.OrderbyColumn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by sidawei on 16/2/2.
 *
 * TODO 测试 null,所有字段都相等,Date
 */
public class OrderByCompareUnit implements Comparable{

    private List<OrderbyColumn> orderbyColumns;

    private ResultSet set;

    /**
     * 当前ResultSet的下标
     */
    private int index;

    public OrderByCompareUnit(ResultSet set, int index, List<OrderbyColumn> orderbyColumns){
        this.set = set;
        this.index = index;
        this.orderbyColumns = orderbyColumns;
    }

    @Override
    public int compareTo(Object o) {
        if (o == null){
            return -1;
        }
        OrderByCompareUnit unit = (OrderByCompareUnit)o;
        for (OrderbyColumn c : orderbyColumns){
            try {
                Object c1 = set.getObject(c.getColumnName());
                Object c2 = unit.set.getObject(c.getColumnName());

                int ret = 0;

                if (c1 == null && c2 == null){
                    ret = 0;
                }else if (c1 == null){
                    ret = -1;
                }else if (c2 == null){
                    ret = 1;
                }else {
                    if (c1 instanceof Number){
                        double d1 = ((Number) c1).doubleValue();
                        double d2 = ((Number) c2).doubleValue();

                        if (d1 > d2){
                            ret = 1;
                        }else if (d1 < d2){
                            ret = -1;
                        }
                    } else if (c1 instanceof java.util.Date){
                        // java.sql.Date;Time;Timestamp 都是java.util.Date的之类
                        ret = ((Date) c1).compareTo((Date)c2);
                    }else if (c1 instanceof String){
                        ret = ((String)c1).compareTo((String)c2);
                    }
                }

                if (ret != 0){
                    if (c.getOrderbyType() == OrderbyColumn.OrderbyType.ASC){
                        return ret * -1;
                    }else {
                        return ret;
                    }
                }

            } catch (SQLException e) {
                throw new BayMaxException("Order by 排序失败, 不能识别的字段类型:" + o.getClass() + ";只能接收Number,Date和String类型.");
            }
        }
        return 0;
    }

    public ResultSet getSet() {
        return set;
    }

    public int getIndex() {
        return index;
    }
}
