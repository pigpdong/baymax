package com.tongbanjie.baymax.support;

public class Functions {

	/**
	 * 取一个String或一个数字的后几位转为Int
	 * @param source
	 * @param length
	 * @return
	 */
	public int subInt(Object source, int length){
		String s = String.valueOf(source);
		if(s.length() > length){
			s.substring(s.length() - 2);
		}
		return Integer.valueOf(s);
	}
	
}
