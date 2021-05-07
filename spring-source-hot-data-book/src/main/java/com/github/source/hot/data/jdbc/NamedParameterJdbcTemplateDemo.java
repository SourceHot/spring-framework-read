package com.github.source.hot.data.jdbc;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class NamedParameterJdbcTemplateDemo {
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
	public NamedParameterJdbcTemplate namedParameterJdbcTemplateDatasource(
			@Qualifier("mysqlDataSource") DataSource dataSource
	){
		return new NamedParameterJdbcTemplate(dataSource);
	}
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(NamedParameterJdbcTemplateDemo.class);
		NamedParameterJdbcTemplate template = context.getBean("namedParameterJdbcTemplateDatasource", NamedParameterJdbcTemplate.class);
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("id", "10");
		paramMap.put("name", "小明");
		template.update(
				"insert into t_user(id,name) values (:id,:name)",
				paramMap
		);

		System.out.println();
	}


//
//	@Bean
//	public JdbcTemplate jdbcTemplate(
//			DataSource mysqlDataSource
//	) {
//		JdbcTemplate jdbcTemplate = new JdbcTemplate();
//		jdbcTemplate.setDataSource(mysqlDataSource);
//		jdbcTemplate.setLazyInit(false);
//		return jdbcTemplate;
//	}
//
//
//
//	@Bean
//	public NamedParameterJdbcTemplate namedParameterJdbcTemplateJdbcOperation(
//			@Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate
//	){
//		return new NamedParameterJdbcTemplate(jdbcTemplate);
//	}
//

}
