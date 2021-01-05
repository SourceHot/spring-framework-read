package com.source.hot.ioc.book.ioc;

import org.junit.jupiter.api.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

class BeanNodeTest {
    @Test
    void testBean() {
        ClassPathXmlApplicationContext context
                = new ClassPathXmlApplicationContext("META-INF/bean-node.xml");

        Object people = context.getBean("people");
        context.close();
    }
}
