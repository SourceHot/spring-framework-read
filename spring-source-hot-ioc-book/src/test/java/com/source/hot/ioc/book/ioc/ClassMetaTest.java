package com.source.hot.ioc.book.ioc;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

public class ClassMetaTest {
	@Test
	void testEnclosingClassName() {
		Class<A> aClass = A.class;
		Class<?> enclosingClass = aClass.getEnclosingClass();
		System.out.println(enclosingClass);

		Class<B> bClass = B.class;
		Class<?> enclosingClass1 = bClass.getEnclosingClass();
		System.out.println(enclosingClass1);

	}

	@Test
	void testCtest() {
		System.out.println(CTest.class.getEnclosingClass());
	}

	@Test
	void testAnn() {
		AnnotationMetadata introspect = AnnotationMetadata.introspect(C.class);
		Map<String, Object> annotationAttributes = introspect.getAnnotationAttributes(ComponentScans.class.getName(), true);
		System.out.println(introspect.hasAnnotation(Component.class.getName()));
		System.out.println(introspect.hasMetaAnnotation(Component.class.getName()));
	}

	@Test
	void annAttribute() throws InvocationTargetException, IllegalAccessException {
		A a = new A();
		Class<? extends A> aClass = a.getClass();
		Annotation[] annotations = aClass.getAnnotations();

		Map<String, Map<String, Object>> annotationAttributes = new HashMap<>();

		for (Annotation annotation : annotations) {
			Class<? extends Annotation> annClass = annotation.annotationType();

			String name = annClass.getName();
			Method[] declaredMethods = annClass.getDeclaredMethods();

			Map<String, Object> oneAnnotationAttributes = new HashMap<>();

			for (Method declaredMethod : declaredMethods) {
				Object invoke = declaredMethod.invoke(annotation);

				String annAtrrName = declaredMethod.getName();
				oneAnnotationAttributes.put(annAtrrName, invoke);

			}
			annotationAttributes.put(name, oneAnnotationAttributes);
		}

		System.out.println();
	}


	public static class B {

	}

	@Component(value = "abcd")

	public class A {

	}

	@Component
	@Configuration
	@ComponentScans(
			value = {
					@ComponentScan()
			}
	)
	public class C {

	}
}
