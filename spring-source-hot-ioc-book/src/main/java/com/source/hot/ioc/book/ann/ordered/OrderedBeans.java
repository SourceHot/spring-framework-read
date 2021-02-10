package com.source.hot.ioc.book.ann.ordered;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class OrderedBeans {

	@Bean
	@Order(1)
	public OrderBeanOne orderBean1(){
		OrderBeanOne orderBeanOne = new OrderBeanOne();
		orderBeanOne.setName("1");
		return orderBeanOne;
	}
	@Bean
	@Order(2)
	public OrderBeanOne orderBean2(){
		OrderBeanOne orderBeanOne = new OrderBeanOne();
		orderBeanOne.setName("2");
		return orderBeanOne;
	}
}
