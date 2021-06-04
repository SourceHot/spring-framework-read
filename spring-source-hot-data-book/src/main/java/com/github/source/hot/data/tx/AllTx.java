package com.github.source.hot.data.tx;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class AllTx {
	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-tx-02.xml");
		UserDao bean = context.getBean(UserDao.class);
		bean.insertUser();
	}

}
