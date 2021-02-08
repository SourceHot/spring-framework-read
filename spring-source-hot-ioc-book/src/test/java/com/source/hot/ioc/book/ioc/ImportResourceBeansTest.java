package com.source.hot.ioc.book.ioc;

import com.source.hot.ioc.book.pojo.PeopleBean;
import com.source.hot.ioc.book.pojo.lookup.Apple;
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
	void testImportResource(){
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ImportResourceBeansTest.class);
		PeopleBean bean = context.getBean(PeopleBean.class);
		assert bean != null;
	}
}
