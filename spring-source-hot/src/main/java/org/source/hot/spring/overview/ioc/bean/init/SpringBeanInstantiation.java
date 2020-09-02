package org.source.hot.spring.overview.ioc.bean.init;


import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringBeanInstantiation {

	public static void main(String[] args) {
		BeanFactory context = new ClassPathXmlApplicationContext(
				"META-INF/beans/spring-bean-instantiation.xml");
		// 别名测试
//        UserBean userFactoryBean = context.getBean("userFactoryBean", UserBean.class);
		UserBean staticMethodBean = context.getBean("static-method-user", UserBean.class);
		UserBean factoryUser = context.getBean("factory-use", UserBean.class);
		UserBean factoryBean = context.getBean("factory-bean-user", UserBean.class);
		System.out.println();

	}
}
