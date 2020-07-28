package org.source.hot.spring.overview.ioc.tx;

import java.util.List;
import java.util.Map;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class TxTest {

	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"META-INF/spring-tx.xml");
		JdbcTemplate jdbcTemplate = context.getBean("jdbcTemplate", JdbcTemplate.class);
		List<Map<String, Object>> maps = jdbcTemplate.queryForList("select * from label");

		TransactionTemplate transactionTemplate = context
				.getBean("transactionTemplate", TransactionTemplate.class);

		transactionTemplate.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(TransactionStatus status) {
				try {
					jdbcTemplate
							.execute("INSERT INTO `scrum`.`label`(`name`) VALUES ('1')");
					return null;
				} catch (Exception e) {
					status.setRollbackOnly();
				}
				return null;
			}

		});


		System.out.println();
	}
}
