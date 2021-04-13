package com.source.hot.mvc;

import javax.jws.WebParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

@Controller
public class WelcomeController {

	@GetMapping("xmlConfig")
	public String xmlConfig() {
		return "xmlConfig";
	}

	@GetMapping("/beanNameView")
	public String beanNameView() {
		return "customerView";
	}
	@Autowired
	private ResourceLoader resourceLoader;
	@GetMapping("xsltView")
	public String xsltView(Model model){
		Resource resource = resourceLoader.getResource("classpath:xsltdata.xml");
		model.addAttribute("employees", resource);
		return "XSLTView";
	}
}