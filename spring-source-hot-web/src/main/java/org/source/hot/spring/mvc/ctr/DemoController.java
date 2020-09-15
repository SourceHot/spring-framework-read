package org.source.hot.spring.mvc.ctr;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {
	@GetMapping("/go")
	public Object go() {
		return "go";
	}
}
