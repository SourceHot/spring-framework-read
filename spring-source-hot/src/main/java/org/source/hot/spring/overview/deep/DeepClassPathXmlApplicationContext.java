package org.source.hot.spring.overview.deep;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DeepClassPathXmlApplicationContext {

	public static void main(String[] args) {


		ClassPathXmlApplicationContext classPathXmlApplicationContext
				= new ClassPathXmlApplicationContext("META-INF/deep/deep-ClassPathXmlApplicationContext.xml");

		classPathXmlApplicationContext.close();

	}
}
