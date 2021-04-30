package com.github.source.hot.data.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.github.source.hot.data.model.TUserEntity;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class JdbcTemplateWithAnnotationDemo {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringJdbcConfig.class);
		JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
		List<TUserEntity> query = jdbcTemplate.query("select * from t_user", new RowMapper<TUserEntity>() {
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
		jdbcTemplate.execute("select * from t_user");
		System.out.println();
	}
}
