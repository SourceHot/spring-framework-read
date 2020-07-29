package org.source.hot.spring.overview.ioc.tx.declarative;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IssueServiceImpl {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Transactional()
	public boolean insertIssue() throws Exception {
		jdbcTemplate.execute("INSERT INTO `scrum`.`issue`() VALUES ()");

		throw new Exception("a");
	}

}
