package org.sourcehot.spring.rmi;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * rmi 服务端
 */
public class RMIServerSourceCode {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("rmi/RMISourceCode.xml");
    }
}
