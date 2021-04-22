package com.source.hot.mvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Controller
@CrossOrigin
@EnableWebMvc
public class HelloController {

	@GetMapping("/demo")
	public String demo() {
		return "hello";
	}


	@GetMapping("/a/{title}")
	public String title(
			@PathVariable("title") String title
	) {
		return title;
	}

	@GetMapping("/a/{title2}")
	public String title2(@PathVariable("title2") String title2) {
		return "title";
	}


}
