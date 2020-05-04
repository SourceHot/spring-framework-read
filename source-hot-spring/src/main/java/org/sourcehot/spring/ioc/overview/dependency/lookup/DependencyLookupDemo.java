package org.sourcehot.spring.ioc.overview.dependency.lookup;

import org.sourcehot.spring.ioc.overview.domain.UserEntity;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DependencyLookupDemo {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF\\dependency-lookup.xml");
        UserEntity bean = context.getBean(UserEntity.class);
    }
}
