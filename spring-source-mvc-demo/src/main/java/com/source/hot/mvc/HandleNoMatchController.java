package com.source.hot.mvc;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class HandleNoMatchController {
	@RequestMapping(value = "/getMapping", method = RequestMethod.GET)
	public String getMapping() {
		return "data";
	}

	@PostMapping(value = "/postMapping/",
			consumes = {MediaType.APPLICATION_JSON_VALUE},
			produces = {"text/plain"}
	)
	@CrossOrigin
	public Object postMapping(
			@RequestBody(required = false) Map<String, String> map
	) {
		return "";
	}

	@GetMapping(value = "/getMapping2", params = {"va"})
	public String getMapping2(
			@RequestParam("va") String va
	) {
		return va;
	}
}
