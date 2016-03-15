package com.tongbanjie.baymax.router.strategy;

import com.tongbanjie.baymax.parser.model.CalculateUnit;
import com.tongbanjie.baymax.router.strategy.model.ElFunction;
import com.tongbanjie.baymax.utils.Pair;

import java.util.List;
import java.util.Map;

public interface IPartitionTable {

    void init(Map<String, ElFunction<?,?>> functionsMap);

    String getLogicTableName();

    String[] getShardingKeys();

	boolean isDisableFullScan();

    /**
     * 一个Unit只会推导出一个表
     * @param units
     * @return
     */
    Pair<String/* targetDB */, String/* targetTable */> execute(CalculateUnit units);

	List<Pair<String/* targetDB */, String/* targetTable */>> getAllTableNames();
}
