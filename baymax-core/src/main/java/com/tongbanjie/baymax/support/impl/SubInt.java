package com.tongbanjie.baymax.support.impl;

import com.tongbanjie.baymax.support.Function;

/**
 * 取一个String或一个数字的后几位转为Int
 * @author dawei
 *
 */
public class SubInt implements Function{

	@Override
	public Object apply(Object... params) {
		Object source = params[0];
		int length = (Integer) params[1];
		String s = String.valueOf(source);
		if(s.length() > length){
			s = s.substring(s.length() - length);
		}
		return s;
	}

	@Override
	public String getFunctionName() {
		return "subInt";
	}
	
	public static void main(String[] args) {
		System.out.println(new SubInt().apply(123456,3));
	}
}
