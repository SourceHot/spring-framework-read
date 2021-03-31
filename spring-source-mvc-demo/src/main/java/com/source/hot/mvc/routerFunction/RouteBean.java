package com.source.hot.mvc.routerFunction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@Configuration
public class RouteBean {

	@Bean
	public RouterFunction<ServerResponse> productListing() {
		return route().GET("/product", req -> ok().body("data"))
				.build();
	}
}
