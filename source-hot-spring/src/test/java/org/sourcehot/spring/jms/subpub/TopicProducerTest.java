package org.sourcehot.spring.jms.subpub;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:jms/subpub/applicationContext-jms-consumer-topic.xml")
public class TopicProducerTest {
    @Test
    public void onMessage() throws IOException {
        System.in.read();
    }
}