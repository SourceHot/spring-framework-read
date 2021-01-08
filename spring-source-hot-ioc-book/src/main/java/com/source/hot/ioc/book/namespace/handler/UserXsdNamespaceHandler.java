package com.source.hot.ioc.book.namespace.handler;

import com.source.hot.ioc.book.parser.UserXsdParser;
import org.w3c.dom.Node;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;

public class UserXsdNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("user_xsd", new UserXsdParser());
    }

    @Override
    public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
        BeanDefinition beanDefinition = definition.getBeanDefinition();
        beanDefinition.getPropertyValues().addPropertyValue("namespace", "namespace");
        return definition;
    }
}
