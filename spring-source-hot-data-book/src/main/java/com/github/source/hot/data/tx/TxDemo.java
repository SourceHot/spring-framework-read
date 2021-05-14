package com.github.source.hot.data.tx;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TxDemo {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-tx-01.xml");
		WorkService bean = context.getBean(WorkService.class);
		bean.work();

	}

}
