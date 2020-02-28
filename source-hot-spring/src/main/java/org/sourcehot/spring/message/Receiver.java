package org.sourcehot.spring.message;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

public class Receiver {
	public static void main(String[] args) throws Exception {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
		Connection connection = activeMQConnectionFactory.createConnection();
		connection.start();
		Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		Queue demo_queue = session.createQueue("demo_queue");
		MessageConsumer consumer = session.createConsumer(demo_queue);
		while (true) {
			Message receive = consumer.receive();
			session.commit();
			if (receive instanceof TextMessage) {
				String messageText = ((TextMessage) receive).getText();
				System.out.println("收到消息: " + messageText);
			}
			if (receive == null) {
				break;
			}
		}

		session.close();
		connection.close();


	}
}
