package org.source.hot.spring.overview.ioc.bean.lookup;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ReplacedMain {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/beans/spring-replaced-method.xml");
		Apple apple = context.getBean("apple", Apple.class);
		apple.hello();
	}
}
