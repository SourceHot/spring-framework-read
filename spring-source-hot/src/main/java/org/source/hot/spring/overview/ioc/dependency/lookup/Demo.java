package org.source.hot.spring.overview.ioc.dependency.lookup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.source.hot.spring.overview.ioc.domain.UserEntity;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Demo {

	public static void main(String[] args) {
//		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
//				"META-INF/dependency.xml");
//		UserEntity bean1 = context.getBean(UserEntity.class);
//		System.out.println(bean1.hashCode());
//		context.close();
		Log log = LogFactory.getLog(Demo.class);
		log.info("hello");

	}

	@Bean(name = "zhangsan")
	@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public UserEntity entity() {
		UserEntity userEntity = new UserEntity();
		userEntity.setAge(1);
		userEntity.setName("张三");
		return userEntity;
	}
}
