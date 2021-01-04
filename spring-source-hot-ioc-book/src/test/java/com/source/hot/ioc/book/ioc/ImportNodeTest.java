package com.source.hot.ioc.book.ioc;

import org.junit.jupiter.api.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

class ImportNodeTest {

    @Test
    void testImportNode() {
        ClassPathXmlApplicationContext context
                = new ClassPathXmlApplicationContext("META-INF/import-beans.xml");
        context.close();
    }

}
