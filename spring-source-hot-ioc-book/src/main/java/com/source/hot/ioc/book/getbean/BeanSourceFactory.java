package com.source.hot.ioc.book.getbean;

public class BeanSourceFactory {
	public static BeanSource staticFactory() {
		BeanSource beanSource = new BeanSource();
		beanSource.setType("StaticFactory");
		return beanSource;
	}

	public BeanSource noStaticFactory() {
		BeanSource beanSource = new BeanSource();
		beanSource.setType("noStaticFactory");
		return beanSource;
	}
}
