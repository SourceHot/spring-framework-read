package org.sourcehot.spring.parser;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class UserNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("myUser", new UserBeanDefinitionParser());
    }
}
