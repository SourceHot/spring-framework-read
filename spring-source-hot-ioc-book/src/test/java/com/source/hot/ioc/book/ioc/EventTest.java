package com.source.hot.ioc.book.ioc;

import org.junit.jupiter.api.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class EventTest {

	@Test
	void testEvent() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring-event.xml");
		context.start();
		context.stop();
		context.close();
	}
}
