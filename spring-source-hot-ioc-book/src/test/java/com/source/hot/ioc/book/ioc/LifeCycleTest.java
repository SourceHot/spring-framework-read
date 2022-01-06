package com.source.hot.ioc.book.ioc;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

public class LifeCycleTest {
	@Test
	void testLifeCycle() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring-lifecycle.xml");
		context.start();
		context.stop();
		context.close();
	}

	@Component
	public abstract class A {

		@Lookup("aa")
		public Object data() {
			return null;
		}
	}

	@Component(value = "aa")
	public class Aa {

	}
}
