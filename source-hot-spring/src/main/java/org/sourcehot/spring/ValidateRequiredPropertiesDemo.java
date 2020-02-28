package org.sourcehot.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ValidateRequiredPropertiesDemo extends ClassPathXmlApplicationContext {
    public ValidateRequiredPropertiesDemo(String... configLocations) throws BeansException {
        super(configLocations);
    }

    @Override
    protected void initPropertySources() {
        System.out.println("initPropertySources");
    }

    public static void main(String[] args) {
        ValidateRequiredPropertiesDemo validateRequiredPropertiesDemo = new ValidateRequiredPropertiesDemo("ContextLoadSourceCode-beans.xml");
        System.out.println();
    }
}
