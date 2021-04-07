package com.source.hot.mvc.ann.ctr;

import com.source.hot.mvc.ex.ResponseStatusEx;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

@RestController
public class ResponseStatusCtr {
	@GetMapping("/responseStatus")
	public Object responseStatus() {
		throw new ResponseStatusEx();
	}


	@GetMapping("/responseEx")
	public Object responseEx() throws Exception {
		throw new UnsupportedMediaTypeStatusException("test-ex");
	}

}
