package com.source.hot.ioc.book.getbean;

import org.springframework.beans.factory.FactoryBean;

public class BeanSourceFactoryBean implements FactoryBean<BeanSource> {
	@Override
	public BeanSource getObject() throws Exception {
		BeanSource beanSource = new BeanSource();
		beanSource.setType("from factory bean .");
		return beanSource;
	}

	@Override
	public Class<?> getObjectType() {
		return BeanSource.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
