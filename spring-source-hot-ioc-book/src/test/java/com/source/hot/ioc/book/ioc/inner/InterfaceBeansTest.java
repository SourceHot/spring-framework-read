package com.source.hot.ioc.book.ioc.inner;

import com.source.hot.ioc.book.ann.inter.InterA;
import com.source.hot.ioc.book.ann.inter.InterAImpl;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class InterfaceBeansTest {
	@Test
	void testInterface(){
		AnnotationConfigApplicationContext context
				= new AnnotationConfigApplicationContext(InterAImpl.class);

		InterA bean = context.getBean(InterA.class);

	}
}
