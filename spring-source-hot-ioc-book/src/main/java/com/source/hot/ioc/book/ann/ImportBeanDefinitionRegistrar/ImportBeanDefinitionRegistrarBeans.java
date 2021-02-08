package com.source.hot.ioc.book.ann.ImportBeanDefinitionRegistrar;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(MyBeanRegistrar.class)
public class ImportBeanDefinitionRegistrarBeans {
}
