package com.github.source.hot.data;

import java.lang.reflect.Method;

import org.springframework.util.ClassUtils;

public class MethodIs {
	public static void main(String[] args) {
		Class<?> objectClass = Object.class;
		Method[] declaredMethods = objectClass.getDeclaredMethods();
		Method method = null;

		for (Method declaredMethod : declaredMethods) {
			boolean hashCode = declaredMethod.getName().equals("hashCode");
			if (hashCode) {
				method = declaredMethod;
				break;
			}
		}

		boolean userLevelMethod = ClassUtils.isUserLevelMethod(method);
		System.out.println(userLevelMethod);
	}
	private void  hh() {

	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
