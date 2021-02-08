package com.source.hot.ioc.book.ioc;

import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@ImportResource(locations = {
		"classpath:/META-INF/first-ioc.xml"
})
@Configuration
public class ImportResourceBeansTest {
	@Test
	void tt(){
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ImportResourceBeansTest.class);
		System.out.println();
	}
}
