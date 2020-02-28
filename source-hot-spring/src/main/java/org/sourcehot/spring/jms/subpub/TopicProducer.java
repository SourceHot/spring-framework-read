package org.sourcehot.spring.jms.subpub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Destination;

@Component
public class TopicProducer {
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination topicTextDestination;

    public void sendTextMessage(final String text){
        //此方法等同于下面被注释部分
        jmsTemplate.convertAndSend(topicTextDestination,text);

        /*jmsTemplate.send(topicTextDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                     return session.createTextMessage(text);
                }
          });*/
    }
}