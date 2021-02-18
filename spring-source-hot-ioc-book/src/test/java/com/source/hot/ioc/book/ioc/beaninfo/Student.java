package com.source.hot.ioc.book.ioc.beaninfo;

import org.springframework.beans.factory.InitializingBean;

public class Student implements InitializingBean {
	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("afterPropertiesSet");
	}

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}