package com.tongbanjie.baymax.utils;

import com.alibaba.fastjson.JSON;

public class Log {
	public static void debug(Object o){
		System.out.println(JSON.toJSONString(o, true));
	}
}
