package com.source.hot.ioc.book.ioc.beaninfo;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.InitializingBean;

public class BeanInfoTest {
	// 首字母大写
	public static String captureName(String name) {
		char[] cs = name.toCharArray();
		cs[0] -= 32;
		return String.valueOf(cs);

	}

	@Test
	void classTest() throws IntrospectionException {
		Class clazz = Student.class;
		BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
		System.out.println();
	}

	@Test
	void beanInfoCreateBean() throws Exception {

		Class<Student> clazz = Student.class;
		// 设置属性表
		Map<String, Object> prop = new HashMap<>(8);
		prop.put("name", "student_name");

		Student bean = getObject(clazz, prop);
		assert bean.getName().equals("student_name");

	}

	@Test
	void classGeneratorBean() throws Exception {
		Class<Student> clazz = Student.class;
		// 设置属性表
		Map<String, Object> prop = new HashMap<>(8);
		prop.put("name", "student_name");

		Student bean = getObjectSample(clazz, prop);
		assert bean.getName().equals("student_name");

	}

	private <T> T getObjectSample(Class<T> clazz, Map<String, Object> prop) throws Exception {

		T t = clazz.newInstance();
		prop.forEach((k, v) -> {

			try {
				Method method = clazz.getDeclaredMethod("set" + captureName(k), String.class);
				method.setAccessible(true);
				if (method != null) {
					method.invoke(t, v);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});
		return t;

	}

	@NotNull
	private <T> T getObject(Class<T> clazz, Map<String, Object> prop) throws Exception {
		// 获取 BeanInfo 接口
		BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
		// 获取 Bean Class
		Class<?> beanClass = beanInfo.getBeanDescriptor().getBeanClass();
		// 获取所有的构造函数
		Constructor<?>[] declaredConstructors = beanClass.getDeclaredConstructors();
		// 确认构造函数: 直接取无参构造
		Constructor constructor = confirmConstructor(declaredConstructors);
		// 通过构造函数获取对象
		Object bean = constructor.newInstance();
		// 为对象设置属性
		// 提取属性描述
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			// 属性名称
			String name = propertyDescriptor.getName();
			if (prop.containsKey(name)) {

				// 写函数
				Method writeMethod = propertyDescriptor.getWriteMethod();
				// 从属性表中获取属性名称对应的属性值
				Object proValue = prop.get(name);
				writeMethod.invoke(bean, proValue);
			}
		}
		if (bean instanceof InitializingBean) {
			((InitializingBean) bean).afterPropertiesSet();
		}
		return (T) bean;
	}

	private Constructor confirmConstructor(Constructor<?>[] declaredConstructors) {

		for (Constructor<?> declaredConstructor : declaredConstructors) {
			if (declaredConstructor.getParameterTypes().length == 0) {
				return declaredConstructor;
			}
		}
		return null;
	}

}
