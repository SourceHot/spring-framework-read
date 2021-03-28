package com.source.hot.ioc.book.ioc.propertySources;

import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@PropertySource(value = "classpath:data.properties")
@PropertySources(value = @PropertySource("classpath:data.properties"))
@Configuration
public class PropertySourceTest {
	@Test
	void testPropertySource() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PropertySourceTest.class);
		assert context.getEnvironment().getProperty("a").equals("123");
	}
}
