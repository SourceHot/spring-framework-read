package org.sourcehot.spring.el;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringELSourceCode {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("EL-context.xml");
        EaBean eaBean = context.getBean("eaBean", EaBean.class);
        System.out.println(eaBean.getEbBean().getName());
    }
}
