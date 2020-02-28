package org.sourcehot.spring.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CacheSourceCode {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("Cache.xml");
        CacheService bean = classPathXmlApplicationContext.getBean(CacheService.class);
        bean.add(1, new DemoCache("a"));
        System.out.println(bean.get(1));

        SimpleCacheManager bean1 = classPathXmlApplicationContext.getBean(SimpleCacheManager.class);
        Cache demoCache = bean1.getCache("demoCache");
        DemoCache demoCache1 = demoCache.get(1, DemoCache.class);


        System.out.println(bean1.getCacheNames());

        System.out.println();
    }
}
