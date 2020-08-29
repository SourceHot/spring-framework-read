package org.source.hot.spring.overview.ioc.cache;

import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@ComponentScan(value = { "org.source.hot.spring.overview.ioc.cache" })
@Configuration
public class CacheMain {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext annotationConfigApplicationContext
                = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.scan("org.source.hot.spring.overview.ioc.cache");
        annotationConfigApplicationContext.refresh();
        CacheMain cacheMain = annotationConfigApplicationContext.getBean(CacheMain.class);
        Student abc = new Student("abc", 123);
        cacheMain.getFromMem(abc);

        CacheManager bean = annotationConfigApplicationContext.getBean(CacheManager.class);
        System.out.println();

    }

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(
                Collections.singletonList(new ConcurrentMapCache("a"))
        );
        return simpleCacheManager;
    }



    @CachePut(value = "a", key = "#student.name", condition = "#student.name !=  '' ")
    public Student getFromMem(Student student) throws InterruptedException {

        TimeUnit.SECONDS.sleep(1);
        student.setName(student.getName().toUpperCase());
        return student;
    }

    public static class Student {
        private String name;

        private Integer age;

        public Student() {
        }

        public Student(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }

    @EnableCaching
    @Configuration
    public class CacheConfig {

    }
}
