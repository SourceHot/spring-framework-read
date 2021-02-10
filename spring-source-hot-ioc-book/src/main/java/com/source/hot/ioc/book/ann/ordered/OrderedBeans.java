package com.source.hot.ioc.book.ann.ordered;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Configuration
@ComponentScan(basePackageClasses = {OrderedBeans.OrderBeanOne.class, OrderedBeans.OrderBeanTwo.class})
public class OrderedBeans {
	@Order(2)
	@Component
	public class OrderBeanTwo {
	}

	@Order(1)
	@Component
	public class OrderBeanOne {
	}

}
