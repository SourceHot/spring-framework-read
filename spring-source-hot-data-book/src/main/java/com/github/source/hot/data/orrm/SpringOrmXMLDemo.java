package com.github.source.hot.data.orrm;

import java.util.List;

import com.github.source.hot.data.model.TUserEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringOrmXMLDemo {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("hibernate5Configuration.xml");
		SessionFactory bean = context.getBean(SessionFactory.class);
		Session session = bean.openSession();
		TUserEntity tUserEntity = session.get(TUserEntity.class, 1L);
		System.out.println();
	}

}
