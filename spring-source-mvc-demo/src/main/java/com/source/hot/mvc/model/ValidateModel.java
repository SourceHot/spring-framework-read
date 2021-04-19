package com.source.hot.mvc.model;

import javax.validation.constraints.Size;
public class ValidateModel {
	@Size(min = 3, max = 5,message = "长度在3-5之间")
	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
