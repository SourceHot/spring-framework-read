package org.source.hot.spring.overview.ioc.tx.declarative;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class DeclarativeTransactionTest {

	public static void main(String[] args) throws Exception {

		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(
				TxConfig.class);
		IssueServiceImpl bean = applicationContext.getBean(IssueServiceImpl.class);
		bean.insertIssue();
		System.out.println();
		applicationContext.close();
	}


}
