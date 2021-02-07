package com.source.hot.ioc.book.ioc;

import java.util.Arrays;
import java.util.function.Predicate;

import com.source.hot.ioc.book.conditional.ConditionBeans;
import com.source.hot.ioc.book.conditional.InterFunc;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ConditionTest {
	@Test
	void testCondition() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ConditionBeans.class);
		InterFunc bean = context.getBean(InterFunc.class);
		System.out.println(bean.data());
		String[] beanDefinitionNames = context.getBeanDefinitionNames();

		System.out.println();
	}
}
