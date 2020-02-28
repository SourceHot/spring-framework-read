package org.sourcehot.spring.aop;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AopSourceCode {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("aop/AopDemo.xml");
//        TestAopBean testAopBean = context.getBean("testAopBean", TestAopBean.class);
//        testAopBean.test();
        Ins bean = context.getBean(Ins.class);
        bean.hh();
    }
}
