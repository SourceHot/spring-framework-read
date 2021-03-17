package com.source.hot.mvc.applicationContextInitializer;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class ContextApplicationContextInitializer implements
		ApplicationContextInitializer<XmlWebApplicationContext> {

	public void initialize(XmlWebApplicationContext applicationContext) {
		System.out.println("com.source.hot.mvc.applicationContextInitializer.ContextApplicationContextInitializer.initialize");
	}

}