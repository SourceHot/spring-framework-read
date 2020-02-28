package org.sourcehot.spring.parser;

import org.sourcehot.spring.bean.UserXtd;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class UserBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    /**
     * 标签对应class
     * @param element the {@code Element} that is being parsed
     * @return
     */
    @Override
    protected Class<?> getBeanClass(Element element) {
        return UserXtd.class;
    }


    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        // 获取 userName 标签属性值
        String name = element.getAttribute("userName");
        // 获取 emailAddress 标签属性值
        String address = element.getAttribute("emailAddress");

        if (StringUtils.hasText(name)) {
            builder.addPropertyValue("userName", name);
        }
        if (StringUtils.hasText(address)) {
            builder.addPropertyValue("emailAddress", address);
        }
    }
}
