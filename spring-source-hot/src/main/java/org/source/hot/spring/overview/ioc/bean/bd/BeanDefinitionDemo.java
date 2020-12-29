package org.source.hot.spring.overview.ioc.bean.bd;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@ComponentScan(basePackages = {"org.source.hot"})
public class BeanDefinitionDemo implements BeanFactoryAware {

	private BeanFactory beanFactory;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(BeanDefinitionDemo.class);
		Person person = ctx.getBean(Person.class);
		System.out.println(person.getName());

	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@PostConstruct
	public void register() {
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(Person.class);
		beanDefinitionBuilder.addPropertyValue("name", "张三");
		BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) this.beanFactory;
		beanFactory.registerBeanDefinition("person", beanDefinitionBuilder.getBeanDefinition());
	}

}

class Person {

	private String name;

	private Integer age;

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}