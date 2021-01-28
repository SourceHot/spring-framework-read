package com.source.hot.ioc.book.ioc;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MessageSourceTest {

	@Test
	void testXml() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/message-source.xml");

		String usHome = context.getMessage("home", null, Locale.US);
		assert usHome.equals("Home");
		String zhHome = context.getMessage("home", null, Locale.CHINESE);
		assert zhHome.equals("jia");

		String format_data = context.getMessage("format_data", new Object[] {"abc"}, Locale.US);
		assert format_data.equals("abc.abc");
		String format_data2 = context.getMessage("format_data", new Object[] {new MessageSourceResolvable() {
			@Override
			public String[] getCodes() {
				return new String[] {"home"};
			}
		}}, Locale.US);
	}

}
