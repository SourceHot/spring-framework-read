package com.source.hot.ioc.book.ioc.inner;

import java.util.Map;

import com.source.hot.ioc.book.ann.AnnPeople;
import com.source.hot.ioc.book.ann.inter.InterA;
import com.source.hot.ioc.book.ann.inter.InterAImpl;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class InterfaceBeansTest {
	@Test
	void testInterface(){
		AnnotationConfigApplicationContext context
				= new AnnotationConfigApplicationContext(InterAImpl.class);
		Map<String, AnnPeople> beansOfType = context.getBeansOfType(AnnPeople.class);
		InterA bean = context.getBean(InterA.class);

	}
}
