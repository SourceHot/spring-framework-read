package org.sourcehot.spring;

import org.sourcehot.spring.dao.HsLog;
import org.sourcehot.spring.dao.impl.HsLogDaoImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringJDBCSourceCode {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("JDBC-demo.xml");
        HsLogDaoImpl bean = applicationContext.getBean(HsLogDaoImpl.class);
//        List<HsLog> all = bean.findAll();
//        System.out.println(all);
//        HsLog hsLog = new HsLog();
//        hsLog.setSource("jlkjll");
//        bean.save(hsLog);
//
        HsLog hsLog = bean.byId(461);
        System.out.println();
    }
}
