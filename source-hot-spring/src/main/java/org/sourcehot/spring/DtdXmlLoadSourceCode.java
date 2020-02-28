package org.sourcehot.spring;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DtdXmlLoadSourceCode {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("DtdXmlDemo.xml");
        context.close();

    }
}
