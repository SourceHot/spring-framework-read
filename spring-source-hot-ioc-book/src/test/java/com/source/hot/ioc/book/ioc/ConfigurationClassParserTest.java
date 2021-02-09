package com.source.hot.ioc.book.ioc;

import com.source.hot.ioc.book.ann.processMemberClasses.BigConfiguration;
import com.source.hot.ioc.book.ann.three.ThreeVarBeans;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


public class ConfigurationClassParserTest {

	@Test
	void testThreeVar() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ThreeVarBeans.class);
	}

	@Test
	void testProcessMemberClasses(){
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BigConfiguration.class);

	}

}
