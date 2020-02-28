package org.sourcehot.spring.jms.p2p;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class MyMessageListener implements MessageListener {

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            System.out.println("接收到消息:" + textMessage.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}