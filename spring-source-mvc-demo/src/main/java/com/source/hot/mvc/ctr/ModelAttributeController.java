package com.source.hot.mvc.ctr;

import com.source.hot.mvc.model.Person;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ModelAttributeController {

	@PostMapping("/ModelAttribute")
	public String model(
			@ModelAttribute("person") Person person,
			ModelMap model
	) {
		return "modelAttribute";
	}

	@GetMapping("/showModel")
	public ModelAndView showModel(
	) {
		return new ModelAndView("modelAttribute", "person", new Person());
	}
}
