package com.tongbanjie.baymax.support;

import com.tongbanjie.baymax.router.strategy.model.ElFunction;

/**
 * 取一个String或一个数字的后几位转为Int
 * @author dawei
 *
 */
public class SubInt implements ElFunction<String, String> {

	@Override
	public String getFunctionName() {
		return "subInt";
	}

	@Override
	public String apply(String input) {
		String s = String.valueOf(input);
		if(s.length() > 2){
			s = s.substring(s.length() - 2);
		}
		return s;
	}
}
