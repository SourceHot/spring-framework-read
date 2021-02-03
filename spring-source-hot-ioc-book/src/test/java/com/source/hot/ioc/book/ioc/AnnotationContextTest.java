package com.source.hot.ioc.book.ioc;

import com.source.hot.ioc.book.ann.AnnBeans;
import com.source.hot.ioc.book.ann.AnnPeople;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AnnotationContextTest {

	@Test
	void testBasePackages(){
		AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext("com.source.hot.ioc.book.ann");
		AnnPeople bean = context.getBean(AnnPeople.class);
		assert bean.getName().equals("people");
	}

	@Test
	void testComponentClasses(){
		AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext(AnnBeans.class);
		AnnPeople bean = context.getBean(AnnPeople.class);
		assert bean.getName().equals("people");
	}
}
