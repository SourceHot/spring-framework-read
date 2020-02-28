package org.sourcehot.spring.ann;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;

@Configuration
public class BeanConfig {
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean(value = "hc")
    @Lazy
    @Order(100)
    public Ubean f() {
        return new Ubean();
    }
}
