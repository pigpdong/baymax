package com.tongbanjie.baymax.router.strategy;

import com.tongbanjie.baymax.router.ColumnProcess;

import java.util.Map;

/**
 * Created by sidawei on 16/3/20.
 */
public abstract class PartitionRule {

    private String column;

    private ColumnProcess columnProcess;

    /*------------------------------ method ---------------------------------*/

    /**
     * 执行EL表达式,结果只能返回Integer或Boolean类型
     *
     * @param params 参数
     * @return
     */
    public abstract <T> Object execute(Map<String, Object> params, Class<T> toType);

    /*--------------------------------get set-------------------------------*/

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public ColumnProcess getColumnProcess() {
        return columnProcess;
    }

    public void setColumnProcess(ColumnProcess columnProcess) {
        this.columnProcess = columnProcess;
    }
}
