package org.sourcehot.spring.messagesource;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Locale;

public class MessageSourceSourceCode {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("MessageSource-demo.xml");
        String code1 = context.getMessage("code", null, Locale.ENGLISH);
        System.out.println(code1);
    }
}
