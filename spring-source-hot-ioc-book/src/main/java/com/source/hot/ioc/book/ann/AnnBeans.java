package com.source.hot.ioc.book.ann;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope
public class AnnBeans {
	@Bean(name = "abc")
	public AnnPeople annPeople() {
		AnnPeople annPeople = new AnnPeople();
		annPeople.setName("people");
		return annPeople;
	}

	class InnerClass {

	}
}
