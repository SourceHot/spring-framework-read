package com.source.hot.ioc.book.ioc;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

class CircularDependenceTest {
	static Map<String, Object> nameMappingObjectInstance = new HashMap<>();

	private static <T> void setProperty(T objectInstance) throws Exception {
		Field[] fields = objectInstance.getClass().getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			// 获取属性类型
			Class<?> fieldType = field.getType();
			String fieldClassName = fieldType.getName();
			// 从容器中获取
			Object cache = nameMappingObjectInstance.get(fieldClassName);
			// 容器中存在
			if (cache != null) {
				field.set(objectInstance, cache);
			}
			// 容器中不存在
			else {
				Object bean = getBean(fieldType);
				field.set(objectInstance, bean);
			}

		}
	}

	public static <T> T getBean(Class<T> clazz) throws Exception {
		String className = clazz.getName();
		// 容器中存在直接获取
		if (nameMappingObjectInstance.containsKey(className)) {
			return (T) nameMappingObjectInstance.get(className);
		}
		// 容器中不存在. 手动创建对象
		// 通过无参构造创建
		T objectInstance = getInstance(clazz);
		// 存入容器
		nameMappingObjectInstance.put(className, objectInstance);
		// 设置创建对象的数据
		setProperty(objectInstance);
		return objectInstance;
	}

	@NotNull
	private static <T> T getInstance(Class<T> clazz) throws Exception {
		return clazz.getDeclaredConstructor().newInstance();
	}

	@Test
	void testCircularDependenceInJava() {
		A a = new A();
		B b = new B();
		a.innerB = b;
		b.innerA = a;
		System.out.println();
	}

	@Test
	void testCircularDependenceInJavaAuto() throws Exception {
		A aBean = getBean(A.class);
		B bBean = getBean(B.class);

		System.out.println();
	}

}

class A {
	public B innerB;
}


class B {
	public A innerA;
}