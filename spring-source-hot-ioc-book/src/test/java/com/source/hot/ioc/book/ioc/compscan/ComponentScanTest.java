package com.source.hot.ioc.book.ioc.compscan;

import com.source.hot.ioc.book.ann.AnnPeople;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;

@ComponentScan(basePackageClasses = ScanBeanA.class)
@Configuration
public class ComponentScanTest {
	@Test
	void scan(){
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ComponentScanTest.class);
		AnnPeople bean = context.getBean(AnnPeople.class);
		assert bean.getName().equals("scanBeanA.people");
	}
}
