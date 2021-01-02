package com.source.hot.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

	@GetMapping("/demo")
	public String demo() {
		return "hello";
	}
}
