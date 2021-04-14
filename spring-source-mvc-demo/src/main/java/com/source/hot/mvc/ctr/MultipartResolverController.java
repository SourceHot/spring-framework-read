package com.source.hot.mvc.ctr;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class MultipartResolverController {

	@PostMapping("/data")
	public String data(
			@RequestParam(value = "file",required = true) MultipartFile file
	) {
		System.out.println();
		return "hello";
	}
}
