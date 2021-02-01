package com.source.hot.ioc.book.ioc;

import org.junit.jupiter.api.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LifeCycleTest {
	@Test
	void testLifeCycle() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring-lifecycle.xml");
		context.start();
		context.stop();
		context.close();
	}
}
