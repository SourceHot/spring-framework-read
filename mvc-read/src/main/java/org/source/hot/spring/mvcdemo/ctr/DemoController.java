package org.source.hot.spring.mvcdemo.ctr;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {
	@GetMapping("/do")
	public Object go() {
		return "fff";
	}
}
