package com.source.hot.mvc.ctr;

import javax.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class ValidateController {

	@ResponseBody
	@RequestMapping(value = "validString", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public String validString(
			@RequestParam(value = "str", defaultValue = "")
			@Size(min = 1, max = 3)
					String vStr) {
		return vStr;
	}
}
