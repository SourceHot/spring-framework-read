package com.source.hot.ioc.book.ioc;

import org.junit.jupiter.api.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

class AliasNodeTest {

    @Test
    void testAlias() {
        ClassPathXmlApplicationContext context
                = new ClassPathXmlApplicationContext("META-INF/alias-node.xml");

        Object p1 = context.getBean("p1");
        Object people = context.getBean("people");
        assert people.equals(p1);
    }
}
