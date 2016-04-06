package com.tongbanjie.baymax.router;

import com.tongbanjie.baymax.router.strategy.*;
import com.tongbanjie.baymax.test.SelectTestSql;
import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.jdbc.model.ParameterMethod;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import com.tongbanjie.baymax.router.model.ExecuteType;
import com.tongbanjie.baymax.router.model.TargetSql;
import com.tongbanjie.baymax.router.strategy.function.ELFunction;
import com.tongbanjie.baymax.support.BaymaxContext;
import com.tongbanjie.baymax.utils.New;
import com.tongbanjie.baymax.utils.NewArrayList;
import junit.framework.Assert;
import org.junit.Test;

import java.util.*;

/**
 * Created by sidawei on 16/3/21.
 */
public class DruidRouteServiceTest implements SelectTestSql{

    private DruidRouteService routeService = new DruidRouteService();

    {
        ininContext();
    }

    /*--------------------------------------------------single------------------------------------------------------*/

    @Test
    public void testDoRoute() throws Exception {
        ExecutePlan plan = routeService.doRoute(single14, null);
        System.out.println(plan);
        Assert.assertEquals(ExecuteType.ALL, plan.getExecuteType());
        TargetSql sql = plan.getSqlList().get(0);
        Assert.assertEquals("p1", sql.getPartition());
        Assert.assertEquals("table1", sql.getLogicTableName());
        Assert.assertEquals("table1_1", sql.getTargetTableName());
        Assert.assertEquals(4, plan.getSqlList().size());
    }

    private void ininContext(){
        ELFunction function = new ELFunction();
        function.setExpression("value % 4");

        // table
        PartitionTable table = new PartitionTable();
        table.setLogicTableName("table1");
        table.setNamePatten("table1_{0}");

        // column
        PartitionColumn column = new PartitionColumn();
        column.setName("a");

        //rule
        PartitionTableRule rule = new PartitionTableRule();
        rule.setColumns(NewArrayList.newIt().add(column).toArrayList());
        rule.setFunction(function);

        table.setRule(rule);


        // node mapping
        List<String> nodeMapping = new ArrayList<String>();
        nodeMapping.add("p1:0,1,2,3");

        table.setNodeMapping(new SimpleTableNodeMapping(nodeMapping));

        BaymaxContext.setTables(NewArrayList.newIt().add(table).toArrayList());

        BaymaxContext.init();
    }

    @Test
    public void testBuildParameters() throws Exception {
        Map<Integer, ParameterCommand> commonds = new HashMap<Integer, ParameterCommand>();
        commonds.put(3, new ParameterCommand(ParameterMethod.setInt, new Object[]{3}, 3));
        commonds.put(1, new ParameterCommand(ParameterMethod.setInt, new Object[]{1}, 1));
        commonds.put(2, new ParameterCommand(ParameterMethod.setInt, new Object[]{2}, 2));
        List<Object> result = routeService.buildParameters(commonds);
        System.out.println(result);
        Assert.assertEquals(3, result.size());

        Assert.assertEquals(1, result.get(0));
        Assert.assertEquals(2, result.get(1));
        Assert.assertEquals(3, result.get(2));
    }
}