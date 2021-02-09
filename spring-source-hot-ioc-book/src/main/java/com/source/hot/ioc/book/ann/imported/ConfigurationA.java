package com.source.hot.ioc.book.ann.imported;

import com.source.hot.ioc.book.ann.AnnBeans;
import com.source.hot.ioc.book.ann.AnnPeople;

import org.springframework.context.annotation.Bean;

public class ConfigurationA extends AnnBeans {
	@Bean(name = "ConfigurationA.annPeople")
	public AnnPeople annPeople() {
		return new AnnPeople();
	}
}
