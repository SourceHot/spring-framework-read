package com.source.hot.ioc.book.parser;

import com.source.hot.ioc.book.xsd.UserXsd;
import org.w3c.dom.Element;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;

public class UserXsdParser extends AbstractSingleBeanDefinitionParser {
    @Override
    protected Class<?> getBeanClass(Element element) {
        return UserXsd.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String name = element.getAttribute("name");
        String idCard = element.getAttribute("idCard");
        builder.addPropertyValue("name", name);
        builder.addPropertyValue("idCard", idCard);
    }

}
