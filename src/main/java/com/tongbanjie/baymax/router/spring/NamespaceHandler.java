package com.tongbanjie.baymax.router.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.tongbanjie.baymax.router.impl.DefaultRule;

/**
 * 用于Spring XML自定义标签拓展
 * @author dawei
 *
 */
public class NamespaceHandler extends NamespaceHandlerSupport {

	//com.alibaba.dubbo.config.spring.schema.DubboNamespaceHandler
	@Override
	public void init() {
		registerBeanDefinitionParser("rule", new BeanDefinitionParser(DefaultRule.class));
	}

}
