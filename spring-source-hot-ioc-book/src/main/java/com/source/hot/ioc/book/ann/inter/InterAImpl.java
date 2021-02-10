package com.source.hot.ioc.book.ann.inter;

import com.source.hot.ioc.book.ann.AnnPeople;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterAImpl implements InterA {

	@Bean
	public AnnPeople annPeople() {
		AnnPeople annPeople = new AnnPeople();
		annPeople.setName("bean1");
		return annPeople;
	}

	@Bean
	public AnnPeople annPeople2() {
		AnnPeople annPeople = new AnnPeople();
		annPeople.setName("bean2");
		return annPeople;
	}

	public AnnPeople annp() {
		AnnPeople annPeople = new AnnPeople();
		annPeople.setName("interface implements bean");
		return annPeople;
	}


}
