package com.github.source.hot.data.jdbc;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@ComponentScan("com.github.source.hot.data.jdbc")
public class SpringJdbcConfig {
	@Bean
	public DataSource mysqlDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://10.10.0.124:3306/shands_uc_3_back?useUnicode=true&amp;characterEncoding=utf8&amp;useSSL=false");
		dataSource.setUsername("root");
		dataSource.setPassword("1314dafa9900");
		return dataSource;
	}

	@Bean
	public JdbcTemplate jdbcTemplate(
			DataSource mysqlDataSource
	) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(mysqlDataSource);
		jdbcTemplate.setLazyInit(false);
		return jdbcTemplate;
	}

}