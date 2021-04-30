package com.github.source.hot.data.orrm;

import com.github.source.hot.data.model.TUserEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@Import(HibernateConf.class)
public class SpringOrmAnnotationDemo {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringOrmAnnotationDemo.class);
		SessionFactory bean = context.getBean(SessionFactory.class);
		Session session = bean.openSession();
		TUserEntity tUserEntity = session.get(TUserEntity.class, 1L);
		System.out.println();

	}
}