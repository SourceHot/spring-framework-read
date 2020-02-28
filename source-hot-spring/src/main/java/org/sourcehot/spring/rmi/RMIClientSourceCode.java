package org.sourcehot.spring.rmi;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RMIClientSourceCode {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("rmi/RMIClientSourceCode.xml");
        IDemoRmiService rmiClient = context.getBean("rmiClient", IDemoRmiService.class);
        int add = rmiClient.add(1, 2);
        System.out.println(add);
    }
}
