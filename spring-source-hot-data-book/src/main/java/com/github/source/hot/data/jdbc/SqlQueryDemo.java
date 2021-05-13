package com.github.source.hot.data.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.github.source.hot.data.model.TUserEntity;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.object.SqlQuery;

public class SqlQueryDemo {
	public static void main(String[] args) {
		SqlQuery<TUserEntity> query = new SqlQuery<TUserEntity>() {
			@Override
			protected RowMapper<TUserEntity> newRowMapper(Object[] parameters, Map<?, ?> context) {
				return new RowMapper<TUserEntity>() {
					@Override
					public TUserEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
						TUserEntity tUserEntity = new TUserEntity();
						tUserEntity.setName(rs.getString("name"));
						tUserEntity.setId(rs.getLong("id"));
						return tUserEntity;
					}
				};
			}
		};

		query.setDataSource(mysqlDataSource());
		query.setSql("select * from t_user");
		List<TUserEntity> execute = query.execute();
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
