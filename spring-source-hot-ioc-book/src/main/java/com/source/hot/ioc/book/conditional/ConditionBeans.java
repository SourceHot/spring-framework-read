package com.source.hot.ioc.book.conditional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConditionBeans {

	@Bean
	@Conditional(LinuxCondition.class)
	public InterFunc linux() {
		return new InterFunc() {
			@Override
			public String data() {
				return "linux";
			}
		};
	}

	@Bean
	@Conditional(WindowsCondition.class)
	public InterFunc windows() {
		return new InterFunc() {
			@Override
			public String data() {
				return "windows";
			}
		};
	}

}
