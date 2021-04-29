package com.github.source.hot.data.orrm;

import com.github.source.hot.data.model.TUserEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ImportResource({"classpath:hibernate5Configuration.xml"})
public class HibernateXMLConf {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(HibernateXMLConf.class);
		SessionFactory bean = context.getBean(SessionFactory.class);
		Session session = bean.openSession();
		TUserEntity tUserEntity = new TUserEntity();
		tUserEntity.setName("ac");
		session.save(tUserEntity);
	}
}