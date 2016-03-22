package com.tongbanjie.baymax.router.strategy.rule;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by sidawei on 16/3/21.
 */
public class ELRuleTest {

    @Test
    public void testExecute() throws Exception {
        ELRule rule = new ELRule();
        rule.setExpression("user_id % 4");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("user_id", 1);

        Object result = rule.execute(map, Integer.class);

        System.out.println(result);
    }

    @Test
    public void testExecute_0() throws Exception {
        ELRule rule = new ELRule();
        rule.setExpression("user_id % 4");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("user_id", 1);

        Object result = rule.execute(map, null);

        System.out.println(result);
    }
}