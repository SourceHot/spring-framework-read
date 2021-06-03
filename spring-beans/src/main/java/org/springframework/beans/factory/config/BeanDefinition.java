/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.AttributeAccessor;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * A BeanDefinition describes a bean instance, which has property values, constructor argument
 * values, and further information supplied by concrete implementations.
 *
 * <p>This is just a minimal interface: The main intention is to allow a
 * {@link BeanFactoryPostProcessor} to introspect and modify property values and other bean
 * metadata.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see ConfigurableListableBeanFactory#getBeanDefinition
 * @see org.springframework.beans.factory.support.RootBeanDefinition
 * @see org.springframework.beans.factory.support.ChildBeanDefinition
 * @since 19.03.2004
 */
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {

	/**
	 * Scope identifier for the standard singleton scope: {@value}.
	 * <p>Note that extended bean factories might support further scopes.
	 * <p>
	 * 作用域,单例
	 *
	 * @see #setScope
	 * @see ConfigurableBeanFactory#SCOPE_SINGLETON
	 */
	String SCOPE_SINGLETON = ConfigurableBeanFactory.SCOPE_SINGLETON;

	/**
	 * Scope identifier for the standard prototype scope: {@value}.
	 * <p>Note that extended bean factories might support further scopes.
	 * 作用域,prototype
	 *
	 * @see #setScope
	 * @see ConfigurableBeanFactory#SCOPE_PROTOTYPE
	 */
	String SCOPE_PROTOTYPE = ConfigurableBeanFactory.SCOPE_PROTOTYPE;


	/**
	 * Role hint indicating that a {@code BeanDefinition} is a major part of the application.
	 * Typically corresponds to a user-defined bean.
	 */
	int ROLE_APPLICATION = 0;

	/**
	 * Role hint indicating that a {@code BeanDefinition} is a supporting part of some larger
	 * configuration, typically an outer {@link org.springframework.beans.factory.parsing.ComponentDefinition}.
	 * {@code SUPPORT} beans are considered important enough to be aware of when looking more
	 * closely at a particular {@link org.springframework.beans.factory.parsing.ComponentDefinition},
	 * but not when looking at the overall configuration of an application.
	 */
	int ROLE_SUPPORT = 1;

	/**
	 * Role hint indicating that a {@code BeanDefinition} is providing an entirely background role
	 * and has no relevance to the end-user. This hint is used when registering beans that are
	 * completely part of the internal workings of a {@link org.springframework.beans.factory.parsing.ComponentDefinition}.
	 */
	int ROLE_INFRASTRUCTURE = 2;


	// Modifiable attributes

	/**
	 * Return the name of the parent definition of this bean definition, if any.
	 * 获取父类的名字(父BeanDefinition)
	 */
	@Nullable
	String getParentName();

	/**
	 * Set the name of the parent definition of this bean definition, if any.
	 *
	 * 设置父类名称
	 */
	void setParentName(@Nullable String parentName);

	/**
	 * Return the current bean class name of this bean definition.
	 * <p>Note that this does not have to be the actual class name used at runtime, in
	 * case of a child definition overriding/inheriting the class name from its parent. Also, this
	 * may just be the class that a factory method is called on, or it may even be empty in case of
	 * a factory bean reference that a method is called on. Hence, do <i>not</i> consider this to be
	 * the definitive bean type at runtime but rather only use it for parsing purposes at the
	 * individual bean definition level.
	 *
	 * 获取 bean 类型名称 (xxx.Class)
	 * @see #getParentName()
	 * @see #getFactoryBeanName()
	 * @see #getFactoryMethodName()
	 */
	@Nullable
	String getBeanClassName();

	/**
	 * Specify the bean class name of this bean definition.
	 * <p>The class name can be modified during bean factory post-processing,
	 * typically replacing the original class name with a parsed variant of it.
	 * 设置 beanClass
	 * @see #setParentName
	 * @see #setFactoryBeanName
	 * @see #setFactoryMethodName
	 */
	void setBeanClassName(@Nullable String beanClassName);

	/**
	 * Return the name of the current target scope for this bean, or {@code null} if not known yet.
	 * 获取作用域
	 */
	@Nullable
	String getScope();

	/**
	 * Override the target scope of this bean, specifying a new scope name.
	 *
	 * 设置作用域
	 * @see #SCOPE_SINGLETON
	 * @see #SCOPE_PROTOTYPE
	 */
	void setScope(@Nullable String scope);

	/**
	 * Return whether this bean should be lazily initialized, i.e. not eagerly instantiated on
	 * startup. Only applicable to a singleton bean.
	 * 是否延迟加载
	 */
	boolean isLazyInit();

	/**
	 * Set whether this bean should be lazily initialized.
	 * <p>If {@code false}, the bean will get instantiated on startup by bean
	 * factories that perform eager initialization of singletons.
	 * 设置是否延迟加载
	 */
	void setLazyInit(boolean lazyInit);

	/**
	 * Return the bean names that this bean depends on.
	 * 获取依赖名称
	 */
	@Nullable
	String[] getDependsOn();

	/**
	 * Set the names of the beans that this bean depends on being initialized. The bean factory will
	 * guarantee that these beans get initialized first.
	 * 设置需要的依赖
	 */
	void setDependsOn(@Nullable String... dependsOn);

	/**
	 * Return whether this bean is a candidate for getting autowired into some other bean.
	 * 是否需要自动链接到别的bean
	 */
	boolean isAutowireCandidate();

	/**
	 * Set whether this bean is a candidate for getting autowired into some other bean.
	 * <p>Note that this flag is designed to only affect type-based autowiring.
	 * It does not affect explicit references by name, which will get resolved even if the specified
	 * bean is not marked as an autowire candidate. As a consequence, autowiring by name will
	 * nevertheless inject a bean if the name matches.
	 * 设置是否需要自动链接到背的bean
	 */
	void setAutowireCandidate(boolean autowireCandidate);

	/**
	 * Return whether this bean is a primary autowire candidate.
	 * 是否主要,针对多个相同类型的情况下使用
	 */
	boolean isPrimary();

	/**
	 * Set whether this bean is a primary autowire candidate.
	 * <p>If this value is {@code true} for exactly one bean among multiple
	 * matching candidates, it will serve as a tie-breaker.
	 * 设置是否是主要的bean
	 */
	void setPrimary(boolean primary);

	/**
	 * Return the factory bean name, if any.
	 * 获取 factory bean 名称
	 */
	@Nullable
	String getFactoryBeanName();

	/**
	 * Specify the factory bean to use, if any. This the name of the bean to call the specified
	 * factory method on.
	 *
	 * 设置 factory bean 名称
	 * @see #setFactoryMethodName
	 */
	void setFactoryBeanName(@Nullable String factoryBeanName);

	/**
	 * Return a factory method, if any. 
	 * 获取工厂方法名称
	 */
	@Nullable
	String getFactoryMethodName();

	/**
	 * Specify a factory method, if any. This method will be invoked with constructor arguments, or
	 * with no arguments if none are specified. The method will be invoked on the specified factory
	 * bean, if any, or otherwise as a static method on the local bean class.
	 *
	 * 设置工厂方法名称
	 * @see #setFactoryBeanName
	 * @see #setBeanClassName
	 */
	void setFactoryMethodName(@Nullable String factoryMethodName);

	/**
	 * Return the constructor argument values for this bean.
	 * <p>The returned instance can be modified during bean factory post-processing.
	 *
	 * 获取 构造标签的对象{@code <constructor-arg/>}
	 * @return the ConstructorArgumentValues object (never {@code null})
	 */
	ConstructorArgumentValues getConstructorArgumentValues();

	/**
	 * Return if there are constructor argument values defined for this bean.
	 *
	 * 是否存在构造标签的java对象
	 *
	 * @since 5.0.2
	 */
	default boolean hasConstructorArgumentValues() {
		return !getConstructorArgumentValues().isEmpty();
	}

	/**
	 * Return the property values to be applied to a new instance of the bean.
	 * <p>The returned instance can be modified during bean factory post-processing.
	 *
	 * 获取属性值对象
	 * @return the MutablePropertyValues object (never {@code null})
	 */
	MutablePropertyValues getPropertyValues();

	/**
	 * Return if there are property values values defined for this bean.
	 *
	 * 是否存在属性值对象
	 * @since 5.0.2
	 */
	default boolean hasPropertyValues() {
		return !getPropertyValues().isEmpty();
	}

	/**
	 * Return the name of the initializer method.
	 *
	 * 获取初始化函数方法名称
	 * @since 5.1
	 */
	@Nullable
	String getInitMethodName();

	/**
	 * Set the name of the initializer method.
	 *设置初始化函数方法名称(bean初始化时调用)
	 * @since 5.1
	 */
	void setInitMethodName(@Nullable String initMethodName);

	/**
	 * Return the name of the destroy method.
	 * 获取摧毁方法名称
	 * @since 5.1
	 */
	@Nullable
	String getDestroyMethodName();

	/**
	 * Set the name of the destroy method.
	 * 设置摧毁方法名称(bean摧毁时调用)
	 * @since 5.1
	 */
	void setDestroyMethodName(@Nullable String destroyMethodName);

	/**
	 * Get the role hint for this {@code BeanDefinition}. The role hint provides the frameworks as
	 * well as tools with an indication of the role and importance of a particular {@code
	 * BeanDefinition}.
	 *
	 * 获取 beanRole
	 * @see #ROLE_APPLICATION
	 * @see #ROLE_SUPPORT
	 * @see #ROLE_INFRASTRUCTURE
	 */
	int getRole();

	/**
	 * Set the role hint for this {@code BeanDefinition}. The role hint provides the frameworks as
	 * well as tools with an indication of the role and importance of a particular {@code
	 * BeanDefinition}.
	 *
	 * 设置 beanRole
	 * @see #ROLE_APPLICATION
	 * @see #ROLE_SUPPORT
	 * @see #ROLE_INFRASTRUCTURE
	 * @since 5.1
	 */
	void setRole(int role);

	/**
	 * Return a human-readable description of this bean definition.
	 * 获取 bean 描述信息
	 */
	@Nullable
	String getDescription();

	/**
	 * Set a human-readable description of this bean definition.
	 * 设置 bean 描述信息
	 * @since 5.1
	 */
	void setDescription(@Nullable String description);


	// Read-only attributes

	/**
	 * Return a resolvable type for this bean definition, based on the bean class or other specific
	 * metadata.
	 * <p>This is typically fully resolved on a runtime-merged bean definition
	 * but not necessarily on a configuration-time definition instance.
	 *
	 * 获取 ResolvableType 对象
	 * @return the resolvable type (potentially {@link ResolvableType#NONE})
	 *
	 * @see ConfigurableBeanFactory#getMergedBeanDefinition
	 * @since 5.2
	 */
	ResolvableType getResolvableType();

	/**
	 * Return whether this a <b>Singleton</b>, with a single, shared instance returned on all
	 * calls.
	 *
	 * 判断是否单例对象
	 * @see #SCOPE_SINGLETON
	 */
	boolean isSingleton();

	/**
	 * Return whether this a <b>Prototype</b>, with an independent instance returned for each call.
	 *
	 * 判断是否原型对象
	 * @see #SCOPE_PROTOTYPE
	 * @since 3.0
	 */
	boolean isPrototype();

	/**
	 * Return whether this bean is "abstract", that is, not meant to be instantiated.
	 *
	 * 判断是否 abstract
	 */
	boolean isAbstract();

	/**
	 * Return a description of the resource that this bean definition came from (for the purpose of
	 * showing context in case of errors).
	 *
	 * 获取 资源描述
	 */
	@Nullable
	String getResourceDescription();

	/**
	 * Return the originating BeanDefinition, or {@code null} if none. Allows for retrieving the
	 * decorated bean definition, if any.
	 * <p>Note that this method returns the immediate originator. Iterate through the
	 * originator chain to find the original BeanDefinition as defined by the user.
	 *
	 * 获取远程bean定义
	 */
	@Nullable
	BeanDefinition getOriginatingBeanDefinition();

}
