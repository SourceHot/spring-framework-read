package com.source.hot.ioc.book.ioc;

import com.source.hot.ioc.book.xsd.UserXsd;
import org.junit.jupiter.api.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

class CustomXmlTest {

    @Test
    void testXmlCustom() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/custom-xml.xml");
        UserXsd testUserBean = context.getBean("testUserBean", UserXsd.class);
        assert testUserBean.getName().equals("huifer");
        assert testUserBean.getIdCard().equals("123");
        context.close();
    }
}
