package com.source.hot.mvc.ann.ctr;

import com.source.hot.mvc.ex.ResponseStatusEx;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResponseStatusCtr {
	@GetMapping("/responseStatus")
	public Object responseStatus() {
		throw new ResponseStatusEx();
	}
}
