package com.source.hot.ioc.book.ioc;

import com.source.hot.ioc.book.ann.ordered.OrderedBeans;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class OrderedTest {
	@Test
	void orderedTest() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(OrderedBeans.class);
		context.close();
	}
}
