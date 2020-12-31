package org.source.hot.spring.overview.ioc.bean.annotation;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 *
 *
 * @author huifer
 */
public class AnnotationContextDemo {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context
                = new AnnotationConfigApplicationContext(AnnotationContextDemo.class);
        Us bean = context.getBean(Us.class);


        context.close();

    }

    @Bean
    public Us us() {
        Us us = new Us();
        us.setName("a");
        return us;
    }
}

class Us {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}