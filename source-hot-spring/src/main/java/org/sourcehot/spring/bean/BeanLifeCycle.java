package org.sourcehot.spring.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * bean生命周期
 */
public class BeanLifeCycle
        implements DisposableBean,
        InitializingBean,
        BeanFactoryAware,
        BeanNameAware {
    private String name;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("设置 beanFactory");
    }

    @Override
    public void setBeanName(String name) {
        System.out.println("设置 beanName");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("销毁");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("设置属性后");
    }

    public void initMethod() {
        System.out.println("init method");
    }
    public void destroyMethod(){
        System.out.println("destroy Method");
    }

    public BeanLifeCycle() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
