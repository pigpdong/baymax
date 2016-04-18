package mybatis;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.Resource;

/**
 * Created by sidawei on 16/4/16.
 */
public class JpaSqlSessionFactoryBean extends SqlSessionFactoryBean {
    String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
            "<mapper namespace=\"jpaexample\">\n" +
            "\t\n" +
            "<resultMap type=\"example.vo.TradeOrder\" id=\"TradeOrderMap\">\n" +
            "\t\t<id column=\"id\" property=\"id\"/>\n" +
            "\t\t<result column=\"product_id\" property=\"productId\"/>\n" +
            "\t\t<result column=\"amount\" property=\"amount\"/>\n" +
            "\t\t<result column=\"real_pay_amount\" property=\"realPayAmount\"/>\n" +
            "\t\t<result column=\"create_time\" property=\"createTime\"/>\n" +
            "\t\t<result column=\"modify_time\" property=\"modifyTime\"/>\n" +
            "\t\t<result column=\"status\" property=\"status\"/>\n" +
            "\t\t<result column=\"type\" property=\"type\"/>\n" +
            "\t\t<result column=\"user_id\" property=\"userId\"/>\n" +
            "\t\t<result column=\"ta_id\" property=\"taId\"/>\n" +
            "\t\t<result column=\"type\" property=\"type\"/>\n" +
            "\t</resultMap>" +
            "\n" +
            "\t<select id=\"getById\" resultMap=\"TradeOrderMap\">\n" +
            "\t\tselect * from t_order where user_id=#{userId}\n" +
            "\t</select>\n" +
            "\t\n" +
            "\n" +
            "</mapper>";


    @Override
    public void setMapperLocations(Resource[] mapperLocations) {
        Resource[] newRes = new Resource[mapperLocations.length + 1];
        for (int i = 0; i < mapperLocations.length; i++) {
            newRes[i] = mapperLocations[i];
        }
        newRes[newRes.length - 1] = new InMemoryMapperResource("jpaexample", s);
        super.setMapperLocations(newRes);
    }
}
