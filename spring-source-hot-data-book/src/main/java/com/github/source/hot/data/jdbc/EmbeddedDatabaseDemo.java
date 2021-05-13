package com.github.source.hot.data.jdbc;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToFile;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

public class EmbeddedDatabaseDemo {
	public static void main(String[] args) {
		withXML();
//		withJava();
		System.out.println();
	}

	private static void withXML() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("EmbeddedDatabaseConfiguration.xml");
		DataSource dataSource = context.getBean("dataSource", DataSource.class);
		JdbcTemplate jdbcTemplate = context.getBean("jdbcTemplate", JdbcTemplate.class);
		List<Map<String, Object>> maps = jdbcTemplate.queryForList("select * from users");
	}

	private static void withJava() {
		EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
				.generateUniqueName(true)
				.setType(EmbeddedDatabaseType.H2)
				.setScriptEncoding("UTF-8")
				.ignoreFailedDrops(true)
				.addScript("create_table.sql")
				.addScripts("insert_table.sql")
				.build();


		JdbcTemplate jdbcTemplate = new JdbcTemplate(db);
		List<Map<String, Object>> maps = jdbcTemplate.queryForList("select * from users");
		System.out.println();
	}
}
