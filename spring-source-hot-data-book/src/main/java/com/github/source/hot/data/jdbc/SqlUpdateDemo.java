package com.github.source.hot.data.jdbc;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.object.SqlUpdate;

public class SqlUpdateDemo {
	public static void main(String[] args) {
		String SQL = "update t_user set name = ? where id = ?";

		SqlUpdate sqlUpdate = new SqlUpdate(mysqlDataSource(),SQL);
		sqlUpdate.declareParameter(new SqlParameter("name", Types.VARCHAR));
		sqlUpdate.declareParameter(new SqlParameter("id", Types.INTEGER));
		sqlUpdate.compile();

		int i = sqlUpdate.update("张三", 11);
		System.out.println();
	}

	public static DataSource mysqlDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://10.10.0.124:3306/shands_uc_3_back?useUnicode=true&amp;characterEncoding=utf8&amp;useSSL=false");
		dataSource.setUsername("root");
		dataSource.setPassword("1314dafa9900");
		return dataSource;
	}
}
