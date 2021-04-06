package com.source.hot.mvc.ann.ctr;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestCtr {
	@GetMapping("/json")
	public Object json() {
		Map<String, String> map = new HashMap<>();
		map.put("demo", "demo");
		return map;
	}
}
