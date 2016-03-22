package com.tongbanjie.baymax.router.strategy;

import com.tongbanjie.baymax.exception.BayMaxException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sidawei on 16/3/20.
 */
public class PartitionTableNodeMapping {

    /*---------------------------------------------------------------*/

    protected List<String> nodeMapping;

    /*---------------------------------------------------------------*/

    protected Map<String/*suffix*/, String/*partition*/> tableMapping = new ConcurrentHashMap<String, String>(); 	// 所有表到分区的映射

    /*---------------------------------------------------------------*/

    /**
     * 把配置中的tableMapping转换为对象
     * <p>
     * SimpleTable能接收的参数只能是
     * p1:01
     * p1:02
     * p2:03
     * p2:04
     *
     */
    public void initTableMapping() {
        if (nodeMapping == null || nodeMapping.size() == 0){
            throw new BayMaxException("nodeMapping 不能为空");
        }
        for (String partition : nodeMapping) {
            // TODO CHECK P1:01
            String[] str = partition.trim().split(":");
            tableMapping.put(str[1].trim(), str[0].trim());
        }
    }

    /*-------------------------------get set--------------------------------*/

    public List<String> getNodeMapping() {
        return nodeMapping;
    }

    public void setNodeMapping(List<String> nodeMapping) {
        this.nodeMapping = nodeMapping;
        // init
        initTableMapping();
    }
}
