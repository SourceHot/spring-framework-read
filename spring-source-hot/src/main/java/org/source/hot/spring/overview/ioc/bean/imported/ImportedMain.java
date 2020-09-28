package org.source.hot.spring.overview.ioc.bean.imported;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ImportedMain {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/import/Spring-import.xml");
		context.close();
	}
}
