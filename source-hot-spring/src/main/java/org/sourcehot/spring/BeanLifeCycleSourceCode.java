package org.sourcehot.spring;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * bean 生命周期
 */
public class BeanLifeCycleSourceCode {
    public static void main(String[] args) {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("BeanLifeCycleSourceCode.xml");
        context.close();

    }
}
