package com.github.source.hot.data.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.github.source.hot.data.model.TUserEntity;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class JdbcTemplateWithXmlDemo {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("jdbcTemplateConfiguration.xml");
		JdbcTemplate bean = context.getBean(JdbcTemplate.class);
		List<TUserEntity> query = bean.query("select * from t_user", new RowMapper<TUserEntity>() {
			@Override
			public TUserEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
				long id = rs.getLong("id");
				String name = rs.getString("name");
				TUserEntity tUserEntity = new TUserEntity();
				tUserEntity.setId(id);
				tUserEntity.setName(name);
				return tUserEntity;
			}
		});
		System.out.println();
	}
}
