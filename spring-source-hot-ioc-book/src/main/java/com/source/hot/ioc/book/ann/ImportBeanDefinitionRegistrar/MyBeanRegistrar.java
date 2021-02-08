package com.source.hot.ioc.book.ann.ImportBeanDefinitionRegistrar;

import com.source.hot.ioc.book.ann.AnnPeople;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class MyBeanRegistrar implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
			BeanDefinitionRegistry registry) {
		GenericBeanDefinition gbd = new GenericBeanDefinition();
		gbd.setBeanClass(AnnPeople.class);
		gbd.getPropertyValues().addPropertyValue("name", "name");
		registry.registerBeanDefinition("abn", gbd);
	}
}