package com.tongbanjie.baymax.router.strategy.rule;

import com.tongbanjie.baymax.router.strategy.PartitionRule;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sidawei on 16/3/20.
 */
public class ELRule extends PartitionRule {

    private String expression;

    /**
     * 执行EL表达式,结果只能返回Integer或Boolean类型
     *
     * @param params 参数
     * @return
     */
    public <T> Object execute(Map<String, Object> params, Class<T> toType) {
        Map<String, Object> vrs = new HashMap<String, Object>();
        //, Map<String, ElFunction<?,?>> functionMap
        //vrs.putAll(functionMap);// 拓展函数
        vrs.put("$ROOT", params);
        VariableResolverFactory vrfactory = new MapVariableResolverFactory(vrs);
        if (toType != null) {
            return MVEL.eval(expression, params, vrfactory, toType);
        } else {
            return MVEL.eval(expression, params, vrfactory);
        }
    }

    /*--------------------------------get set-------------------------------*/

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
