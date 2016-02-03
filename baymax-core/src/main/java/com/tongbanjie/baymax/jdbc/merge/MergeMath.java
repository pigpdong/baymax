package com.tongbanjie.baymax.jdbc.merge;

import java.math.BigDecimal;

/**
 * Created by sidawei on 16/2/3.
 */
public class MergeMath {
    public static BigDecimal sum(BigDecimal o1, BigDecimal o2){
        if (o1 == null){
            o1 = new BigDecimal(0);
        }
        if (o2 != null){
            o1.add(o2);
        }
        return o1;
    }
}
