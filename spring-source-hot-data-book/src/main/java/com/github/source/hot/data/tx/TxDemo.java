package com.github.source.hot.data.tx;

import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TxDemo {
	public static void main(String[] args) throws Exception {
//		System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");
		System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY,
				"D:\\desktop\\git_repo\\spring-ebk\\spring-framework-read\\spring-source-hot-data-book\\prox");
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-tx-01.xml");
		WorkService bean = context.getBean(WorkService.class);
		bean.work();

	}

}
