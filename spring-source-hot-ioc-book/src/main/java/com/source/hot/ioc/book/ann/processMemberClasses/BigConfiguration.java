package com.source.hot.ioc.book.ann.processMemberClasses;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class BigConfiguration {


	public class SmallConfigA {

	}

	@Component
	public class SmallConfigB {


	}
}
