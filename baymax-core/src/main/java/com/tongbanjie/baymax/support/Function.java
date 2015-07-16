package com.tongbanjie.baymax.support;

/**
 * 自定义函数的抽象
 * @author dawei
 *
 */
public interface Function {
	
	/**
	 * 执行自定义函数
	 * @param params
	 * @return
	 */
	Object apply(Object... params);
	
	/**
	 * 获取自定义函数在EL表达式中被使用的关键字
	 * @return
	 */
	String getFunctionName();
	
}
