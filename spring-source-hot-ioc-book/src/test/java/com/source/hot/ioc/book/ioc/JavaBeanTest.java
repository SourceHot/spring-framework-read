package com.source.hot.ioc.book.ioc;


import java.lang.reflect.Constructor;

import com.source.hot.ioc.book.live.LiveBean;
import com.source.hot.ioc.book.pojo.PeopleBean;
import org.junit.jupiter.api.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JavaBeanTest {

    @Test
    void javaBeanLifeCycle() {
        PeopleBean peopleBean = new PeopleBean();
        peopleBean.setName("zhangsan");

        System.out.println(peopleBean.getName());
    }

    @Test
    void testSpringBeanLive() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/live-bean.xml");
        LiveBean liveBean = context.getBean("liveBean", LiveBean.class);
        context.close();
    }

    @Test
    void testClass() throws Exception {
        String className = "com.source.hot.ioc.book.live.LiveBean";
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Class<?> aClass = contextClassLoader.loadClass(className);

        Object o2 = aClass.newInstance();

        Constructor<?> constructor = aClass.getConstructor();
        Object o = constructor.newInstance();
        if (o instanceof LiveBean) {
            LiveBean o1 = (LiveBean) o;
            o1.setAddress("shangHai");
            System.out.println(o1.getAddress());
        }
    }
}
