package com.source.hot.ioc.book.ann.inter;

import com.source.hot.ioc.book.ann.AnnPeople;

import org.springframework.context.annotation.Bean;

public interface InterA {
	@Bean
	default AnnPeople annpc() {
		AnnPeople annPeople = new AnnPeople();
		annPeople.setName("interface bean 1");
		return annPeople;
	}


	@Bean
	abstract AnnPeople annp();
}
