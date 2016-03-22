package com.tongbanjie.baymax.parser;

import com.tongbanjie.baymax.test.PrintUtil;
import com.tongbanjie.baymax.test.SelectTestSql;
import com.tongbanjie.baymax.parser.model.ConditionUnit;
import com.tongbanjie.baymax.parser.model.ConditionUnitOperator;
import com.tongbanjie.baymax.parser.model.ParseResult;
import com.tongbanjie.baymax.parser.model.SqlType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

/**
 * Created by sidawei on 16/3/18.
 */
public class DruidSelectParserTest implements SelectTestSql {

    IDruidSqlParser parser = DruidParserFactory.getParser(SqlType.SELECT);

    /*--------------------------------------------------single------------------------------------------------------*/

    @Test
    public void testParse_single1() throws Exception {
        ParseResult result = new ParseResult();
        parser.init(single1, null);
        parser.parse(result);

        PrintUtil.printCalculates(result.getCalculateUnits());

        // 计算单元
        Assert.assertEquals(1, result.getCalculateUnits().size());
        Assert.assertEquals(1, result.getCalculateUnits().get(0).getTablesAndConditions().size());
        Assert.assertEquals(2, result.getCalculateUnits().get(0).getTablesAndConditions().get("table1").size());

        Iterator<ConditionUnit> ite = result.getCalculateUnits().get(0).getTablesAndConditions().get("table1").iterator();
        ConditionUnit unit1 = ite.next();
        ConditionUnit unit2 = ite.next();

        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "a", new String[]{"1"}, ConditionUnitOperator.EQUAL), unit1);
        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "b", new String[]{"2"}, ConditionUnitOperator.EQUAL), unit2);

    }

    @Test
    public void testParse_single2() throws Exception {
        ParseResult result = new ParseResult();
        parser.init(single2, null);
        parser.parse(result);

        PrintUtil.printCalculates(result.getCalculateUnits());
        // 计算单元

        ConditionUnit unit1 = result.getCalculateUnits().get(0).getTablesAndConditions().get("table1").iterator().next();
        ConditionUnit unit2 = result.getCalculateUnits().get(1).getTablesAndConditions().get("table1").iterator().next();

        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "a", new String[]{"1"}, ConditionUnitOperator.EQUAL), unit1);
        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "a", new String[]{"2"}, ConditionUnitOperator.EQUAL), unit2);

    }

    @Test
    public void testParse_single13() throws Exception {
        ParseResult result = new ParseResult();
        parser.init(single13, null);
        parser.parse(result);

        PrintUtil.printCalculates(result.getCalculateUnits());

        // 计算单元
        Assert.assertEquals(4, result.getCalculateUnits().size());

        Iterator<ConditionUnit> ite1 = result.getCalculateUnits().get(0).getTablesAndConditions().get("table1").iterator();
        ConditionUnit unit1_1 = ite1.next();
        ConditionUnit unit1_2 = ite1.next();

        Iterator<ConditionUnit> ite2 = result.getCalculateUnits().get(1).getTablesAndConditions().get("table1").iterator();
        ConditionUnit unit2_1 = ite2.next();
        ConditionUnit unit2_2 = ite2.next();

        Iterator<ConditionUnit> ite3 = result.getCalculateUnits().get(2).getTablesAndConditions().get("table1").iterator();
        ConditionUnit unit3_1 = ite3.next();
        ConditionUnit unit3_2 = ite3.next();

        Iterator<ConditionUnit> ite4 = result.getCalculateUnits().get(3).getTablesAndConditions().get("table1").iterator();
        ConditionUnit unit4_1 = ite4.next();
        ConditionUnit unit4_2 = ite4.next();

        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "a", new String[]{"0"}, ConditionUnitOperator.EQUAL), unit1_1);
        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "c", new String[]{"0"}, ConditionUnitOperator.EQUAL), unit1_2);

        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "a", new String[]{"0"}, ConditionUnitOperator.EQUAL), unit2_1);
        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "d", new String[]{"0"}, ConditionUnitOperator.EQUAL), unit2_2);

        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "b", new String[]{"1"}, ConditionUnitOperator.EQUAL), unit3_1);
        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "c", new String[]{"0"}, ConditionUnitOperator.EQUAL), unit3_2);

        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "b", new String[]{"1"}, ConditionUnitOperator.EQUAL), unit4_1);
        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "d", new String[]{"0"}, ConditionUnitOperator.EQUAL), unit4_2);

    }

    @Test
    public void testParse_single14() throws Exception {
        ParseResult result = new ParseResult();
        parser.init(single14, null);
        parser.parse(result);

        PrintUtil.printCalculates(result.getCalculateUnits());

        // 计算单元
        Assert.assertEquals(4, result.getCalculateUnits().size());

        Iterator<ConditionUnit> ite1 = result.getCalculateUnits().get(0).getTablesAndConditions().get("table1").iterator();
        ConditionUnit unit1_1 = ite1.next();

        Iterator<ConditionUnit> ite2 = result.getCalculateUnits().get(1).getTablesAndConditions().get("table1").iterator();
        ConditionUnit unit2_1 = ite2.next();

        Iterator<ConditionUnit> ite3 = result.getCalculateUnits().get(2).getTablesAndConditions().get("table1").iterator();
        ConditionUnit unit3_1 = ite3.next();

        Iterator<ConditionUnit> ite4 = result.getCalculateUnits().get(3).getTablesAndConditions().get("table1").iterator();
        ConditionUnit unit4_1 = ite4.next();

        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "a", new String[]{"0"}, ConditionUnitOperator.EQUAL), unit1_1);

        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "b", new String[]{"1"}, ConditionUnitOperator.EQUAL), unit2_1);

        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "c", new String[]{"0"}, ConditionUnitOperator.EQUAL), unit3_1);

        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "d", new String[]{"0"}, ConditionUnitOperator.EQUAL), unit4_1);

    /*--------------------------------------------------child table------------------------------------------------------*/
    }

    @Test
    public void testParse_sb1() throws Exception {
        ParseResult result = new ParseResult();
        parser.init(sb1, null);
        parser.parse(result);

        // 计算单元
        Assert.assertEquals(1, result.getCalculateUnits().size());

        Iterator<ConditionUnit> ite1 = result.getCalculateUnits().get(0).getTablesAndConditions().get("table2").iterator();
        ConditionUnit unit1_1 = ite1.next();

        System.out.println(unit1_1);

        Assert.assertEquals(ConditionUnit.buildConditionUnit("table2", "b", new String[]{"0"}, ConditionUnitOperator.EQUAL), unit1_1);

    }

    @Test
    public void testParse_sb2() throws Exception {
        ParseResult result = new ParseResult();
        parser.init(sb2, null);
        parser.parse(result);

        PrintUtil.printCalculates(result.getCalculateUnits());

        // 计算单元
        Assert.assertEquals(1, result.getCalculateUnits().size());

        Iterator<ConditionUnit> ite1 = result.getCalculateUnits().get(0).getTablesAndConditions().get("table2").iterator();
        ConditionUnit unit1_1 = ite1.next();

        Assert.assertEquals(ConditionUnit.buildConditionUnit("table2", "b", new String[]{"2"}, ConditionUnitOperator.EQUAL), unit1_1);

    }

    /*--------------------------------------------------join------------------------------------------------------*/

    @Test
    public void testParse_join1() throws Exception {
        ParseResult result = new ParseResult();
        parser.init(join1, null);
        parser.parse(result);

        PrintUtil.printCalculates(result.getCalculateUnits());

        // 计算单元
        Assert.assertEquals(1, result.getCalculateUnits().size());

        Iterator<ConditionUnit> ite1 = result.getCalculateUnits().get(0).getTablesAndConditions().get("table1").iterator();
        ConditionUnit unit1_1 = ite1.next();

        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "id", new String[]{"100"}, ConditionUnitOperator.EQUAL), unit1_1);

    }

    @Test
    public void testParse_join10() throws Exception {
        ParseResult result = new ParseResult();
        parser.init(join10, null);
        parser.parse(result);

        PrintUtil.printCalculates(result.getCalculateUnits());

        // 计算单元
        Assert.assertEquals(1, result.getCalculateUnits().size());

        Iterator<ConditionUnit> ite1 = result.getCalculateUnits().get(0).getTablesAndConditions().get("table2").iterator();
        ConditionUnit unit1_1 = ite1.next();

        Assert.assertEquals(ConditionUnit.buildConditionUnit("table2", "id", new String[]{"100"}, ConditionUnitOperator.EQUAL), unit1_1);

    }
    /*--------------------------------------------------union------------------------------------------------------*/

    @Test
    public void testParse_union1() throws Exception {
        ParseResult result = new ParseResult();
        parser.init(union1, null);
        parser.parse(result);

        PrintUtil.printCalculates(result.getCalculateUnits());

        // 计算单元
        ConditionUnit unit1_1 = result.getCalculateUnits().get(0).getTablesAndConditions().get("table1").iterator().next();
        ConditionUnit unit2_1 = result.getCalculateUnits().get(0).getTablesAndConditions().get("table2").iterator().next();

        Assert.assertEquals(ConditionUnit.buildConditionUnit("table1", "a", new String[]{"1"}, ConditionUnitOperator.EQUAL), unit1_1);
        Assert.assertEquals(ConditionUnit.buildConditionUnit("table2", "b", new String[]{"2"}, ConditionUnitOperator.EQUAL), unit2_1);

    }
}
