package com.tongbanjie.baymax.druid;

import com.tongbanjie.baymax.router.DruidRouteService;
import org.junit.Test;

/**
 * Created by sidawei on 16/1/25.
 */
public class TestDruidSelectRouteService {

    @Test
    public void test_1(){
        DruidRouteService service = new DruidRouteService();
        // druid不支持这类sql，这类sql拿到的所有条件的table都是最后一个表
        //String sql = "select sort,(select id from abort_batch where id='33') as name from prod_product where id='exp0002'";
        // 支持这类的子查询
        //String sql = "select sort from prod_product where id='exp0002' and name = (select name from t2 where nid=1)";
        //String sql = "select sort from prod_product where v1='vv1' and name = (select name from t2 where v2='vv2' and v3='vv3' or v4='vv4')";

        String sql = "select * from t where a=1 and (b=2 or c=3)";
        service.doRoute(sql, null);
    }
}
