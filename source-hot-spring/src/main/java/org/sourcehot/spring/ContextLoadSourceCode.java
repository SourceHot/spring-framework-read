package org.sourcehot.spring;

import org.sourcehot.spring.bean.Person;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ContextLoadSourceCode {
    public static void main(String[] args) {
        // 这是一个最基本的 spring 调用的例子
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ContextLoadSourceCode-beans.xml");
        Person bean = context.getBean(Person.class);
        System.out.println(bean.getName());
        System.out.println(bean.getApple().getName());
        bean.dis();
        System.out.println(bean.getAge());


    }
}
