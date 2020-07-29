package org.source.hot.spring.overview.ioc.tx.declarative;
 
import javax.sql.DataSource;
 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
 
import com.alibaba.druid.pool.DruidDataSource;
 
@ComponentScan(basePackages = "org.source.hot.spring.overview.ioc.tx.declarative")
@EnableTransactionManagement
public class TxConfig {
 
	@Bean // 数据源
	public DataSource dataSource() {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setUsername("huifer");
		dataSource.setPassword("a12345");
		dataSource.setUrl("jdbc:mysql://47.98.225.144:3306/scrum?useSSL=false");
		dataSource.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
		return dataSource;
	}
 
	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
 
	@Bean //事务管理器
	public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
 
}