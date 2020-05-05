package org.source.hot.spring.overview.ioc.dependency.lookup;

import org.source.hot.spring.overview.ioc.domain.UserEntity;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Demo {
	public static void main(String[] args) {
		BeanFactory beanFactory = new ClassPathXmlApplicationContext("META-INF/dependency.xml");
		UserEntity bean = beanFactory.getBean(UserEntity.class);
		System.out.println(bean);
	}
}
