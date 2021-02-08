package com.source.hot.ioc.book.ioc;

import com.source.hot.ioc.book.ann.AnnPeople;
import com.source.hot.ioc.book.ann.ImportBeanDefinitionRegistrar.ImportBeanDefinitionRegistrarBeans;
import com.source.hot.ioc.book.ann.imported.ImportedBeans;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AnnImportTest {
	@Test
	void testAnnImportConfiguration() {
		AnnotationConfigApplicationContext context
				= new AnnotationConfigApplicationContext(ImportedBeans.class);
		AnnPeople bean = context.getBean("ConfigurationA.annPeople", AnnPeople.class);
		assert bean != null;
	}

	@Test
	void testImportBeanDefinitionRegistrar() {
		AnnotationConfigApplicationContext context
				= new AnnotationConfigApplicationContext(ImportBeanDefinitionRegistrarBeans.class);

		AnnPeople bean = context.getBean("abn", AnnPeople.class);
		assert bean != null;
	}

}
