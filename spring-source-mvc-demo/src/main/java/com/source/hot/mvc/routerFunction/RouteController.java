package com.source.hot.mvc.routerFunction;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RouteController {
	@RequestMapping("/route_demo")
	public List<Integer> productListing() {
		List<Integer> integers = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			integers.add(i);
		}
		return integers;
	}

}
