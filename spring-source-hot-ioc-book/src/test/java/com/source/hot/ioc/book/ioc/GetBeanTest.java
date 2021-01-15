package com.source.hot.ioc.book.ioc;

import com.source.hot.ioc.book.getbean.BeanSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

class GetBeanTest {
	ClassPathXmlApplicationContext context = null;

	@BeforeEach
	void init() {
		context = new ClassPathXmlApplicationContext("META-INF/get-bean.xml");
	}

	@Test
	void fromBean() {
		BeanSource beanSource = context.getBean("beanSource", BeanSource.class);
		assert beanSource.getType().equals("xml");
	}

	@Test
	void fromStatic() {
		BeanSource beanSourceFromStatic = context.getBean("beanSourceFromStatic", BeanSource.class);
		assert beanSourceFromStatic.getType().equals("StaticFactory");
	}

	@Test
	void fromNoStatic() {
		BeanSource beanSourceFromNoStatic = context.getBean("beanSourceFromNoStatic", BeanSource.class);
		assert beanSourceFromNoStatic.getType().equals("noStaticFactory");

	}

	@Test
	void fromFactoryBean() {
		BeanSource beanSourceFromFactoryBean = context.getBean("beanSourceFromFactoryBean", BeanSource.class);
		assert beanSourceFromFactoryBean.getType().equals("from factory bean .");
	}
}
