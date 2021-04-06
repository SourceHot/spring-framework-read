package com.source.hot.mvc.ann.ctr;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

@RestController
@SessionAttributes(value = {"session_attr"})
public class RestCtr {
	@GetMapping("/json")
	public Object json() {
		Map<String, String> map = new HashMap<>();
		map.put("demo", "demo");
		return map;
	}

	@GetMapping("/json2")
	public Object json2(@ModelAttribute(name = "name") String name) {
		return name;
	}

	@RequestMapping("/testSessionAttributes")
	public String testSessionAttributes(Model model) {
		model.addAttribute("session_attr", "session_attr");
		return "success";
	}

	@GetMapping("/data_param")
	public Object dataParam(
		@RequestParam(defaultValue = "default_value") String data
	) {
		return data;
	}
}
