package org.sourcehot.spring.qualifier;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class QualifierSourceCode {
    public static void main(String[] args) {
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("QualifierSourceCode-beans.xml");
        PersonService service = context.getBean(PersonService.class);
        System.out.println(service.getPerson().getPersonName());
        context.close();
    }
}
