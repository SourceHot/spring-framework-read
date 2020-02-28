package org.sourcehot.spring.beanPostProcessor;

import org.sourcehot.spring.bean.Apple;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BeanFactoryPostProcessorSourceCode {
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("BeanFactoryPostProcessor-demo.xml");
		Apple apple = context.getBean("apple", Apple.class);
		System.out.println(apple);
	}
}
