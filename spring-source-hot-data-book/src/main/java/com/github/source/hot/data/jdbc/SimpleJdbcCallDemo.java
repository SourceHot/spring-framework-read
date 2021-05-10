package com.github.source.hot.data.jdbc;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class SimpleJdbcCallDemo {
	public static DataSource mysqlDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://10.10.0.124:3306/shands_uc_3_back?useUnicode=true&amp;characterEncoding=utf8&amp;useSSL=false");
		dataSource.setUsername("root");
		dataSource.setPassword("1314dafa9900");
		return dataSource;
	}

	public static void main(String[] args) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(mysqlDataSource()).withFunctionName("get_user_name");
				Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("in_id", "11");
		Map<String, Object> out = jdbcCall.execute(parameters);
		System.out.println();

	}

}
