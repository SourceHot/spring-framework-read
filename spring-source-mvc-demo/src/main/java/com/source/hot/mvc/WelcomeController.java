package com.source.hot.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WelcomeController {

	@Autowired
	private ResourceLoader resourceLoader;

	@GetMapping("xmlConfig")
	public String xmlConfig() {
		return "xmlConfig";
	}

	@GetMapping("/beanNameView")
	public String beanNameView() {
		return "customerView";
	}

	@GetMapping("xsltView")
	public String xsltView(Model model) {
		Resource resource = resourceLoader.getResource("classpath:xsltdata.xml");
		model.addAttribute("employees", resource);
		return "XSLTView";
	}


	@RequestMapping({"page1"})
	public void handle() {
	}
}