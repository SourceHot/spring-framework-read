package org.sourcehot.spring.message;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

public class Sender {
	public static void main(String[] args) throws Exception {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
		Connection connection = activeMQConnectionFactory.createConnection();
		Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		Queue demo_queue = session.createQueue("demo_queue");
		MessageProducer producer = session.createProducer(demo_queue);
		for (int i = 0; i < 10; i++) {
			TextMessage textMessage = session.createTextMessage("hello-message");
//            Thread.sleep(10000L);
			System.out.println("sender ");
			producer.send(textMessage);
		}
		session.commit();
		session.close();
		connection.close();

	}
}
