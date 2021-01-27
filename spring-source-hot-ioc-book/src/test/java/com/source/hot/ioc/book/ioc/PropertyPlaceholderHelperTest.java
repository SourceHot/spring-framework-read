package com.source.hot.ioc.book.ioc;

import java.util.Properties;

import com.source.hot.ioc.book.pojo.PropertyBean;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.PropertyPlaceholderHelper;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class PropertyPlaceholderHelperTest {
	Properties properties = new Properties();

	PropertyPlaceholderHelper propertyPlaceholderHelper;

	@NotNull
	private static PropertyPlaceholderHelper.PlaceholderResolver getPlaceholderResolver(Properties properties) {
		return new PropertyPlaceholderHelper.PlaceholderResolver() {
			@Override
			public String resolvePlaceholder(String placeholderName) {
				String value = properties.getProperty(placeholderName);
				return value;
			}
		};
	}

	@Test
	void testPropertyPlaceholder() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/PropertyResolution.xml");
		PropertyBean bean = context.getBean(PropertyBean.class);
		assumeTrue(bean.getName().equals("zhangsan"));
	}

	@BeforeEach
	void initProperties() {
		properties.put("a", "1");
		properties.put("b", "2");
		properties.put("c", "3");
		properties.put("a23", "abc");
		propertyPlaceholderHelper = new PropertyPlaceholderHelper("{", "}");
	}

	/**
	 * 没有占位符
	 */
	@Test
	void testNoPlaceholder() {
		String noPlaceholder = "a";
		String replacePlaceholders = propertyPlaceholderHelper.replacePlaceholders(noPlaceholder, this::getValue);
		assumeTrue(replacePlaceholders.equals("a"));
	}

	/**
	 * 存在一个占位符
	 */
	@Test
	void testOnePlaceholder() {
		String onePlaceholder = "{a}";
		String replacePlaceholders = propertyPlaceholderHelper.replacePlaceholders(onePlaceholder, this::getValue);
		assumeTrue(replacePlaceholders.equals("1"));
	}

	/**
	 * 存在平级占位符
	 */
	@Test
	void testSameLevelPlaceholder() {
		String sameLevelPlaceholder = "{a}{b}{c}";
		String replacePlaceholders = propertyPlaceholderHelper.replacePlaceholders(sameLevelPlaceholder, getPlaceholderResolver(properties));
		assumeTrue(replacePlaceholders.equals("123"));

	}

	/**
	 * 存在嵌套占位符
	 */
	@Test
	void testNestedPlaceholder() {
		String nestedPlaceholder = "{a{b}{c}}";
		String replacePlaceholders = propertyPlaceholderHelper.replacePlaceholders(nestedPlaceholder, getPlaceholderResolver(properties));
		assumeTrue(replacePlaceholders.equals("abc"));

	}

	private String getValue(String key) {
		return this.properties.getProperty(key);
	}
}
