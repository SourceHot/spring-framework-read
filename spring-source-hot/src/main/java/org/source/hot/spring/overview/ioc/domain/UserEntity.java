package org.source.hot.spring.overview.ioc.domain;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class UserEntity implements InitializingBean, BeanPostProcessor, DisposableBean {
	private String name;
	private Integer age;

	public UserEntity(String name, Integer age) {
		this.name = name;
		this.age = age;
	}

	public UserEntity() {
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		System.out.println("postProcessBeforeInitialization");
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		System.out.println("postProcessAfterInitialization");
		return bean;
	}

	@Override
	public void destroy() throws Exception {
		System.out.println("摧毁方法");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("afterPropertiesSet");
	}

	@Override
	public String toString() {
		return "UserEntity{" +
				"name='" + name + '\'' +
				", age=" + age +
				'}';
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}
}
