package com.source.hot.ioc.book.ioc.compscan;

import com.source.hot.ioc.book.ann.AnnPeople;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScanBeanA {
	@Bean
	public AnnPeople annPeople() {
		AnnPeople annPeople = new AnnPeople();
		annPeople.setName("scanBeanA.people");
		return annPeople;
	}
}
