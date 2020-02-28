package org.sourcehot.spring;

import org.sourcehot.spring.bean.JavaObjectTest;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ConstructorSourceCode {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext
                ("ConstructorDemo.xml");
        JavaObjectTest obj = context.getBean("obj", JavaObjectTest.class);
        System.out.println(obj.getList());
//        obj2
        JavaObjectTest obj2 = context.getBean("obj2", JavaObjectTest.class);
        System.out.println(obj2.getMap());
        context.close();

    }
}
