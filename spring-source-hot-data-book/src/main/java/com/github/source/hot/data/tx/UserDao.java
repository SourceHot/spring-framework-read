package com.github.source.hot.data.tx;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserDao {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public void insertUser() throws Exception {
		String sql = "INSERT INTO `t_user`(`name`) VALUES (?)";
		String username = UUID.randomUUID().toString().substring(0, 3);
		jdbcTemplate.update(sql, username);
		System.out.println("插入成功");
//		int i = 10 / 0;
		throw new Exception("aaa");
	}

}
