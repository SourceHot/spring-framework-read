package com.source.hot.ioc.book.ioc;

import com.source.hot.ioc.book.pojo.PeopleBean;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

class FirstIoCDemoTest {
    @Test
    void testClassPathXmlApplicationContext() {
        ClassPathXmlApplicationContext context
                = new ClassPathXmlApplicationContext("META-INF/first-ioc.xml");

        PeopleBean people = context.getBean("people", PeopleBean.class);

        String name = people.getName();
        assumeTrue(name.equals("zhangsan"));
    }


    @Test
    void testFileSystemXmlApplicationContext() {
        FileSystemXmlApplicationContext context
                = new FileSystemXmlApplicationContext("D:\\desktop\\git_repo\\spring-ebk\\spring-framework-read\\spring-source-hot-ioc-book\\src\\test\\resources\\META-INF\\first-ioc.xml");

        PeopleBean people = context.getBean("people", PeopleBean.class);

        String name = people.getName();
        assumeTrue(name.equals("zhangsan"));
		context.close();
    }

    @Test
    void testXmlBeanFactory() {
        XmlBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("META-INF/first-ioc.xml"));

        PeopleBean people = beanFactory.getBean("people", PeopleBean.class);

        String name = people.getName();
        assumeTrue(name.equals("zhangsan"));
    }

}
