package org.sourcehot.springmvc.httpinvoker;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HttpInvokerRunSouceCode {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("HttpInvokerClient.xml");
        IHttpInvokerService httpInvokerProxyFactoryBean = context.getBean("httpInvokerProxyFactoryBean", IHttpInvokerService.class);
        int add = httpInvokerProxyFactoryBean.add(1, 2);
        System.out.println(add);
    }
}
