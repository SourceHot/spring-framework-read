package org.source.hot.spring.overview.ioc.bean.lookup;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LookupMain {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/beans/spring-lookup-method.xml");

		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		BeanDefinition apple = beanFactory.getBeanDefinition("apple");
		Object attribute = apple.getAttribute("meta-key");
		System.out.println(attribute);
		Shop shop = context.getBean("shop", Shop.class);
		System.out.println(shop.getFruits().getName());
	}
}
