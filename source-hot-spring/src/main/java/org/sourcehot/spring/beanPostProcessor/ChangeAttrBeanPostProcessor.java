package org.sourcehot.spring.beanPostProcessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.StringValueResolver;

import java.util.HashSet;
import java.util.Set;

public class ChangeAttrBeanPostProcessor implements BeanFactoryPostProcessor {
    private Set<String> attr;

    public ChangeAttrBeanPostProcessor() {
        attr = new HashSet<>();
    }

    public Set<String> getAttr() {
        return attr;
    }

    public void setAttr(Set<String> attr) {
        this.attr = attr;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

            StringValueResolver stringValueResolver = new StringValueResolver() {
                @Override
                public String resolveStringValue(String strVal) {
                    if (attr.contains(strVal)) {
                        return "隐藏属性";
                    }
                    else {
                        return strVal;
                    }
                }
            };
            BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(stringValueResolver);
            visitor.visitBeanDefinition(beanDefinition);
        }
    }
}
