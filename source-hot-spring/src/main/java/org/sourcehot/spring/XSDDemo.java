package org.sourcehot.spring;

import org.sourcehot.spring.bean.UserXtd;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 自定义标签测试用例
 */
public class XSDDemo {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("XTD-xml.xml");
        UserXtd user = (UserXtd) applicationContext.getBean("testUserBean");
        System.out.println(user.getEmailAddress());
    }
}
