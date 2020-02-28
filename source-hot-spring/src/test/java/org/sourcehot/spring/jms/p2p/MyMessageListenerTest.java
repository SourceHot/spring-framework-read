package org.sourcehot.spring.jms.p2p;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;


@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:jms/p2p/applicationContext-jms-consumer-queue.xml")
public class MyMessageListenerTest {
	@Test
	public void onMessage() throws IOException {
		System.in.read();
	}
}