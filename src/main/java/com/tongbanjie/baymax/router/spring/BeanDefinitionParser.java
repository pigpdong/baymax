package com.tongbanjie.baymax.router.spring;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 用于Spring XML自定义标签拓展
 * @author dawei
 *
 */
public class BeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

	private final Class<?> beanClass;

	public BeanDefinitionParser(Class<?> beanClass) {
		this.beanClass = beanClass;
	}

	protected Class<?> getBeanClass(Element element) {
		return beanClass;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder beanDefinition) {
		String id = element.getAttribute("id");
		if ((id == null || id.length() == 0)) {
			String generatedBeanName = element.getAttribute("name");
			if (generatedBeanName == null || generatedBeanName.length() == 0) {
				generatedBeanName = beanClass.getName();
			}
			id = generatedBeanName;
			int counter = 2;
			while (parserContext.getRegistry().containsBeanDefinition(id)) {
				id = generatedBeanName + (counter++);
			}
		}
		if (id != null && id.length() > 0) {
			if (parserContext.getRegistry().containsBeanDefinition(id)) {
				throw new IllegalStateException("Duplicate spring bean id " + id);
			}
			/**
			 * 如果标签上没有定义ID，则自动生成ID
			 */
			element.setAttribute("id", id);
			parserContext.getRegistry().registerBeanDefinition(id, beanDefinition.getRawBeanDefinition());
		}
		// 自定义参数的处理
		beanDefinition.addPropertyValue("physicsTablePrefix", element.getAttribute("physicsTablePrefix"));
		beanDefinition.addPropertyValue("logicTableName", element.getAttribute("logicTableName"));
		beanDefinition.addPropertyValue("shardingColumns", element.getAttribute("shardingColumns"));
	    
		/**
		 * 解析标签内部的<property>节点
		 */
		parseProperties(element.getChildNodes(), beanDefinition, parserContext);
	}

	/**
	 * 解析 </p>
	 * <property name="dbIndex" value="p1,p2" />
	 * @param nodeList
	 * @param beanDefinition
	 * @param parserContext
	 */
	private static void parseProperties(NodeList nodeList, BeanDefinitionBuilder beanDefinition, ParserContext parserContext) {
		if (nodeList != null && nodeList.getLength() > 0) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node instanceof Element) {
					if ("property".equals(node.getNodeName()) || "property".equals(node.getLocalName())) {
						String name = ((Element) node).getAttribute("name");
						if (name != null && name.length() > 0) {
							parserContext.getDelegate().parsePropertyElement((Element)node, beanDefinition.getRawBeanDefinition());
						}
					}
				}
			}
		}
	}
}
