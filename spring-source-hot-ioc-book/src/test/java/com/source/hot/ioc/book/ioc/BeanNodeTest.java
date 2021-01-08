package com.source.hot.ioc.book.ioc;

import com.source.hot.ioc.book.pojo.PeopleBean;
import com.source.hot.ioc.book.pojo.PeopleBeanTwo;
import com.source.hot.ioc.book.pojo.lookup.Apple;
import com.source.hot.ioc.book.pojo.lookup.Shop;
import org.junit.jupiter.api.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

class BeanNodeTest {
    @Test
    void testBean() {
        ClassPathXmlApplicationContext context
                = new ClassPathXmlApplicationContext("META-INF/bean-node.xml");

        Object people = context.getBean("people");
        context.close();
    }

    @Test
    void testLookupMethodBean() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring-lookup-method.xml");

        Shop shop = context.getBean("shop", Shop.class);
        System.out.println(shop.getFruits().getName());
        assert context.getBean("apple").equals(shop.getFruits());
    }

    @Test
    void testReplacedMethod() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring-replaced-method.xml");
        Apple apple = context.getBean("apple", Apple.class);
        apple.hello();
    }

    @Test
    void testConstructArg() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring-constructor-arg.xml");
        PeopleBean people = context.getBean("people", PeopleBean.class);
        assert people.getName().equals("zhangsan");
    }

    @Test
    void testConstructArgForName() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring-constructor-arg.xml");
        PeopleBean people = context.getBean("people2", PeopleBean.class);
        assert people.getName().equals("zhangsan");
    }

    @Test
    void testProperty() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring-property.xml");
        PeopleBean people = context.getBean("people", PeopleBean.class);
        assert people.getName().equals("zhangsan");
    }

    @Test
    void testQualifier() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring-qualifier.xml");
        PeopleBeanTwo peopleTwo = context.getBean("p2", PeopleBeanTwo.class);
        assert peopleTwo.getPeopleBean().equals(context.getBean("peopleBean", PeopleBean.class));
    }
}
