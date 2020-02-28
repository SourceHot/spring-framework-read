package org.sourcehot.spring.ann;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class XmlAnnSourceCode {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("Spring-Aann.xml");
        Ubean bean = ctx.getBean("hc", Ubean.class);
        System.out.println(bean);
    }
}
