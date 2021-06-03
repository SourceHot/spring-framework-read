package com.github.source.hot.data.tx;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public class WorkService {
	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional(rollbackFor = Exception.class)
	public void work() {
		jdbcTemplate.execute("INSERT INTO `t_user`(`name`) VALUES ('12')");
		throw new RuntimeException("111");
	}
}
