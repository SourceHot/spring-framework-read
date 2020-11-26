/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.LogFactory;

import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Extension of the standard JavaBeans {@link PropertyDescriptor} class,
 * overriding {@code getPropertyType()} such that a generically declared
 * type variable will be resolved against the containing bean class.
 *
 * 通用类型感知属性描述符
 * @author Juergen Hoeller
 * @since 2.5.2
 */
final class GenericTypeAwarePropertyDescriptor extends PropertyDescriptor {
	/**
	 * 类型
	 */
	private final Class<?> beanClass;

	/**
	 * 可读方法
	 */
	@Nullable
	private final Method readMethod;

	/**
	 * 可写方法
	 */
	@Nullable
	private final Method writeMethod;

	/**
	 * 属性编辑器类型
	 */
	private final Class<?> propertyEditorClass;

	/**
	 * 可能的可写方法
	 */
	@Nullable
	private volatile Set<Method> ambiguousWriteMethods;

	/**
	 * 可写方法的参数
	 */
	@Nullable
	private MethodParameter writeMethodParameter;

	/**
	 * 属性类型
	 */
	@Nullable
	private Class<?> propertyType;


	/**
	 * 构造函数
	 * @param beanClass 对象蕾西
	 * @param propertyName 属性名称
	 * @param readMethod 可读函数
	 * @param writeMethod 可写函数
	 * @param propertyEditorClass 属性编辑器类型
	 * @throws IntrospectionException
	 */
	public GenericTypeAwarePropertyDescriptor(Class<?> beanClass, String propertyName,
			@Nullable Method readMethod, @Nullable Method writeMethod, Class<?> propertyEditorClass)
			throws IntrospectionException {

		super(propertyName, null, null);
		this.beanClass = beanClass;

		// 计算可读方法
		Method readMethodToUse = (readMethod != null ? BridgeMethodResolver.findBridgedMethod(readMethod) : null);
		// 计算可写方法
		Method writeMethodToUse = (writeMethod != null ? BridgeMethodResolver.findBridgedMethod(writeMethod) : null);
		if (writeMethodToUse == null && readMethodToUse != null) {
			// Fallback: Original JavaBeans introspection might not have found matching setter
			// method due to lack of bridge method resolution, in case of the getter using a
			// covariant return type whereas the setter is defined for the concrete property type.
			// 获取方法
			// 获取set 方法
			Method candidate = ClassUtils.getMethodIfAvailable(
					this.beanClass, "set" + StringUtils.capitalize(getName()), (Class<?>[]) null);
			if (candidate != null && candidate.getParameterCount() == 1) {
				writeMethodToUse = candidate;
			}
		}
		this.readMethod = readMethodToUse;
		this.writeMethod = writeMethodToUse;

		if (this.writeMethod != null) {
			if (this.readMethod == null) {
				// Write method not matched against read method: potentially ambiguous through
				// several overloaded variants, in which case an arbitrary winner has been chosen
				// by the JDK's JavaBeans Introspector...
				Set<Method> ambiguousCandidates = new HashSet<>();
				// 方法推测 , 满足下面要求的就可能是 可写方法
				for (Method method : beanClass.getMethods()) {
					if (method.getName().equals(writeMethodToUse.getName()) &&
							!method.equals(writeMethodToUse) && !method.isBridge() &&
							method.getParameterCount() == writeMethodToUse.getParameterCount()) {
						ambiguousCandidates.add(method);
					}
				}
				if (!ambiguousCandidates.isEmpty()) {
					// 赋值
					this.ambiguousWriteMethods = ambiguousCandidates;
				}
			}
			// 构造可写函数的参数对象
			this.writeMethodParameter = new MethodParameter(this.writeMethod, 0).withContainingClass(this.beanClass);
		}

		if (this.readMethod != null) {
			// 属性类型的计算
			// 计算方式: 通过 class 中寻找 method , 将 找到的 method 的返回值作为结果
			this.propertyType = GenericTypeResolver.resolveReturnType(this.readMethod, this.beanClass);
		}
		else if (this.writeMethodParameter != null) {
			// 获取参数类型
			this.propertyType = this.writeMethodParameter.getParameterType();
		}

		// 属性编辑器类型赋值
		this.propertyEditorClass = propertyEditorClass;
	}


	public Class<?> getBeanClass() {
		return this.beanClass;
	}

	@Override
	@Nullable
	public Method getReadMethod() {
		return this.readMethod;
	}

	@Override
	@Nullable
	public Method getWriteMethod() {
		return this.writeMethod;
	}

	public Method getWriteMethodForActualAccess() {
		Assert.state(this.writeMethod != null, "No write method available");
		Set<Method> ambiguousCandidates = this.ambiguousWriteMethods;
		if (ambiguousCandidates != null) {
			this.ambiguousWriteMethods = null;
			LogFactory.getLog(GenericTypeAwarePropertyDescriptor.class).warn("Invalid JavaBean property '" +
					getName() + "' being accessed! Ambiguous write methods found next to actually used [" +
					this.writeMethod + "]: " + ambiguousCandidates);
		}
		return this.writeMethod;
	}

	public MethodParameter getWriteMethodParameter() {
		Assert.state(this.writeMethodParameter != null, "No write method available");
		return this.writeMethodParameter;
	}

	@Override
	@Nullable
	public Class<?> getPropertyType() {
		return this.propertyType;
	}

	@Override
	public Class<?> getPropertyEditorClass() {
		return this.propertyEditorClass;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof GenericTypeAwarePropertyDescriptor)) {
			return false;
		}
		GenericTypeAwarePropertyDescriptor otherPd = (GenericTypeAwarePropertyDescriptor) other;
		return (getBeanClass().equals(otherPd.getBeanClass()) && PropertyDescriptorUtils.equals(this, otherPd));
	}

	@Override
	public int hashCode() {
		int hashCode = getBeanClass().hashCode();
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(getReadMethod());
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(getWriteMethod());
		return hashCode;
	}

}
