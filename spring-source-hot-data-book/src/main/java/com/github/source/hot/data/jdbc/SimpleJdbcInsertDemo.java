package com.github.source.hot.data.jdbc;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class SimpleJdbcInsertDemo {
	public static DataSource mysqlDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://10.10.0.124:3306/shands_uc_3_back?useUnicode=true&amp;characterEncoding=utf8&amp;useSSL=false");
		dataSource.setUsername("root");
		dataSource.setPassword("1314dafa9900");
		return dataSource;
	}

	public static void main(String[] args) {
		SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(mysqlDataSource()).withTableName("t_user");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("id", "11");
		parameters.put("name", "小明");
		simpleJdbcInsert.execute(parameters);

	}

}
