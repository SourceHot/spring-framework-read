package com.source.hot.ioc.book.ann;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AnnBeans {
	@Bean
	public AnnPeople annPeople() {
		AnnPeople annPeople = new AnnPeople();
		annPeople.setName("people");
		return annPeople;
	}
}
