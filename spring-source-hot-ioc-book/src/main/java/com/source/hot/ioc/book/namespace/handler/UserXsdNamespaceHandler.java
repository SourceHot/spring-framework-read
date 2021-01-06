package com.source.hot.ioc.book.namespace.handler;

import com.source.hot.ioc.book.parser.UserXsdParser;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class UserXsdNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("user_xsd", new UserXsdParser());
    }

}
