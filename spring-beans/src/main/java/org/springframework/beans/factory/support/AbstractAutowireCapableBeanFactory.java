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

package org.springframework.beans.factory.support;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.AutowiredPropertyMarker;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.StringUtils;

/**
 * Abstract bean factory superclass that implements default bean creation, with the full
 * capabilities specified by the {@link RootBeanDefinition} class. Implements the {@link
 * org.springframework.beans.factory.config.AutowireCapableBeanFactory} interface in addition to
 * AbstractBeanFactory's {@link #createBean} method.
 *
 * <p>Provides bean creation (with constructor resolution), property population,
 * wiring (including autowiring), and initialization. Handles runtime bean references, resolves
 * managed collections, calls initialization methods, etc. Supports autowiring constructors,
 * properties by name, and properties by type.
 *
 * <p>The main template method to be implemented by subclasses is
 * {@link #resolveDependency(DependencyDescriptor, String, Set, TypeConverter)}, used for autowiring
 * by type. In case of a factory which is capable of searching its bean definitions, matching beans
 * will typically be implemented through such a search. For other factory styles, simplified
 * matching algorithms can be implemented.
 *
 * <p>Note that this class does <i>not</i> assume or implement bean definition
 * registry capabilities. See {@link DefaultListableBeanFactory} for an implementation of the {@link
 * org.springframework.beans.factory.ListableBeanFactory} and {@link BeanDefinitionRegistry}
 * interfaces, which represent the API and SPI view of such a factory, respectively.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Mark Fisher
 * @author Costin Leau
 * @author Chris Beams
 * @author Sam Brannen
 * @author Phillip Webb
 * @see RootBeanDefinition
 * @see DefaultListableBeanFactory
 * @see BeanDefinitionRegistry
 * @since 13.02.2004
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
		implements AutowireCapableBeanFactory {

	/**
	 * Dependency types to ignore on dependency check and autowire, as Set of Class objects: for
	 * example, String. Default is none.
	 *
	 * 忽略的依赖类型
	 */
	private final Set<Class<?>> ignoredDependencyTypes = new HashSet<>();

	/**
	 * Dependency interfaces to ignore on dependency check and autowire, as Set of Class objects. By
	 * default, only the BeanFactory interface is ignored.
	 * 忽略的依赖接口
	 */
	private final Set<Class<?>> ignoredDependencyInterfaces = new HashSet<>();

	/**
	 * The name of the currently created bean, for implicit dependency registration on getBean etc
	 * invocations triggered from a user-specified Supplier callback.
	 * 正在创建的 bean Name
	 */
	private final NamedThreadLocal<String> currentlyCreatedBean = new NamedThreadLocal<>("Currently created bean");

	/**
	 * Cache of unfinished FactoryBean instances: FactoryBean name to BeanWrapper.
	 * key: beanName
	 * value: bean wrapper
	 */
	private final ConcurrentMap<String, BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

	/**
	 * Cache of candidate factory methods per factory class.
	 * 工厂方法缓存
	 * key: 工厂类
	 * value: 方法列表
	 */
	private final ConcurrentMap<Class<?>, Method[]> factoryMethodCandidateCache = new ConcurrentHashMap<>();

	/**
	 * Cache of filtered PropertyDescriptors: bean Class to PropertyDescriptor array.
	 * key: 类
	 * value: 属性描述
	 */
	private final ConcurrentMap<Class<?>, PropertyDescriptor[]> filteredPropertyDescriptorsCache =
			new ConcurrentHashMap<>();

	/**
	 * Strategy for creating bean instances.
	 * 实例化策略
	 */
	private InstantiationStrategy instantiationStrategy = new CglibSubclassingInstantiationStrategy();

	/**
	 * Resolver strategy for method parameter names.
	 * 参数名称发现接口
	 */
	@Nullable
	private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

	/**
	 * Whether to automatically try to resolve circular references between beans.
	 * 是否允许循环引用
	 */
	private boolean allowCircularReferences = true;

	/**
	 * Whether to resort to injecting a raw bean instance in case of circular reference, even if the
	 * injected bean eventually got wrapped.
	 *  是否允许当前 Bean 的原始类型注入到别的 Bean 中
	 */
	private boolean allowRawInjectionDespiteWrapping = false;


	/**
	 * Create a new AbstractAutowireCapableBeanFactory.
	 */
	public AbstractAutowireCapableBeanFactory() {
		super();
		// 添加忽略的依赖接口(即add方法调用)
		ignoreDependencyInterface(BeanNameAware.class);
		ignoreDependencyInterface(BeanFactoryAware.class);
		ignoreDependencyInterface(BeanClassLoaderAware.class);
	}

	/**
	 * Create a new AbstractAutowireCapableBeanFactory with the given parent.
	 *
	 * @param parentBeanFactory parent bean factory, or {@code null} if none
	 */
	public AbstractAutowireCapableBeanFactory(@Nullable BeanFactory parentBeanFactory) {
		this();
		setParentBeanFactory(parentBeanFactory);
	}

	/**
	 * Return the instantiation strategy to use for creating bean instances.
	 */
	protected InstantiationStrategy getInstantiationStrategy() {
		return this.instantiationStrategy;
	}

	/**
	 * Set the instantiation strategy to use for creating bean instances. Default is
	 * CglibSubclassingInstantiationStrategy.
	 *
	 * @see CglibSubclassingInstantiationStrategy
	 */
	public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
		this.instantiationStrategy = instantiationStrategy;
	}

	/**
	 * Return the ParameterNameDiscoverer to use for resolving method parameter names if needed.
	 */
	@Nullable
	protected ParameterNameDiscoverer getParameterNameDiscoverer() {
		return this.parameterNameDiscoverer;
	}

	/**
	 * Set the ParameterNameDiscoverer to use for resolving method parameter names if needed (e.g.
	 * for constructor names).
	 * <p>Default is a {@link DefaultParameterNameDiscoverer}.
	 */
	public void setParameterNameDiscoverer(@Nullable ParameterNameDiscoverer parameterNameDiscoverer) {
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}

	/**
	 * Set whether to allow circular references between beans - and automatically try to resolve
	 * them.
	 * <p>Note that circular reference resolution means that one of the involved beans
	 * will receive a reference to another bean that is not fully initialized yet. This can lead to
	 * subtle and not-so-subtle side effects on initialization; it does work fine for many
	 * scenarios, though.
	 * <p>Default is "true". Turn this off to throw an exception when encountering
	 * a circular reference, disallowing them completely.
	 * <p><b>NOTE:</b> It is generally recommended to not rely on circular references
	 * between your beans. Refactor your application logic to have the two beans involved delegate
	 * to a third bean that encapsulates their common logic.
	 */
	public void setAllowCircularReferences(boolean allowCircularReferences) {
		this.allowCircularReferences = allowCircularReferences;
	}

	/**
	 * Set whether to allow the raw injection of a bean instance into some other bean's property,
	 * despite the injected bean eventually getting wrapped (for example, through AOP
	 * auto-proxying).
	 * <p>This will only be used as a last resort in case of a circular reference
	 * that cannot be resolved otherwise: essentially, preferring a raw instance getting injected
	 * over a failure of the entire bean wiring process.
	 * <p>Default is "false", as of Spring 2.0. Turn this on to allow for non-wrapped
	 * raw beans injected into some of your references, which was Spring 1.2's (arguably unclean)
	 * default behavior.
	 * <p><b>NOTE:</b> It is generally recommended to not rely on circular references
	 * between your beans, in particular with auto-proxying involved.
	 *
	 * @see #setAllowCircularReferences
	 */
	public void setAllowRawInjectionDespiteWrapping(boolean allowRawInjectionDespiteWrapping) {
		this.allowRawInjectionDespiteWrapping = allowRawInjectionDespiteWrapping;
	}

	/**
	 * Ignore the given dependency type for autowiring: for example, String. Default is none.
	 */
	public void ignoreDependencyType(Class<?> type) {
		this.ignoredDependencyTypes.add(type);
	}

	/**
	 * Ignore the given dependency interface for autowiring.
	 * <p>This will typically be used by application contexts to register
	 * dependencies that are resolved in other ways, like BeanFactory through BeanFactoryAware or
	 * ApplicationContext through ApplicationContextAware.
	 * <p>By default, only the BeanFactoryAware interface is ignored.
	 * For further types to ignore, invoke this method for each type.
	 *
	 * 添加忽略的依赖接口
	 * @see org.springframework.beans.factory.BeanFactoryAware
	 * @see org.springframework.context.ApplicationContextAware
	 */
	public void ignoreDependencyInterface(Class<?> ifc) {
		this.ignoredDependencyInterfaces.add(ifc);
	}

	/**
	 * 配置拷贝
	 */
	@Override
	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		super.copyConfigurationFrom(otherFactory);
		if (otherFactory instanceof AbstractAutowireCapableBeanFactory) {
			AbstractAutowireCapableBeanFactory otherAutowireFactory =
					(AbstractAutowireCapableBeanFactory) otherFactory;
			this.instantiationStrategy = otherAutowireFactory.instantiationStrategy;
			this.allowCircularReferences = otherAutowireFactory.allowCircularReferences;
			this.ignoredDependencyTypes.addAll(otherAutowireFactory.ignoredDependencyTypes);
			this.ignoredDependencyInterfaces.addAll(otherAutowireFactory.ignoredDependencyInterfaces);
		}
	}


	//-------------------------------------------------------------------------
	// Typical methods for creating and populating external bean instances
	//-------------------------------------------------------------------------

	@Override
	@SuppressWarnings("unchecked")
	public <T> T createBean(Class<T> beanClass) throws BeansException {
		// Use prototype bean definition, to avoid registering bean as dependent bean.
		RootBeanDefinition bd = new RootBeanDefinition(beanClass);
		bd.setScope(SCOPE_PROTOTYPE);
		// 确认是否需要缓存
		bd.allowCaching = ClassUtils.isCacheSafe(beanClass, getBeanClassLoader());
		// 创建 bean
		return (T) createBean(beanClass.getName(), bd, null);
	}

	/**
	 * 注入 bean
	 */
	@Override
	public void autowireBean(Object existingBean) {
		// Use non-singleton bean definition, to avoid registering bean as dependent bean.
		// 创建 BeanDefinition
		RootBeanDefinition bd = new RootBeanDefinition(ClassUtils.getUserClass(existingBean));
		// 设置 scope
		bd.setScope(SCOPE_PROTOTYPE);
		// 是否缓存
		bd.allowCaching = ClassUtils.isCacheSafe(bd.getBeanClass(), getBeanClassLoader());
		// 创建 beanWrapper
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		// 属性设置
		populateBean(bd.getBeanClass().getName(), bd, bw);
	}

	/**
	 * 配置bean信息
	 *
	 */
	@Override
	public Object configureBean(Object existingBean, String beanName) throws BeansException {
		// 标记 beanName 正在创建
		markBeanAsCreated(beanName);
		// 获取合并的 beanDefinition
		BeanDefinition mbd = getMergedBeanDefinition(beanName);
		RootBeanDefinition bd = null;
		if (mbd instanceof RootBeanDefinition) {
			RootBeanDefinition rbd = (RootBeanDefinition) mbd;
			// 是否原型bean. 如果是原型bean会做深度拷贝
			bd = (rbd.isPrototype() ? rbd : rbd.cloneBeanDefinition());
		}
		if (bd == null) {
			bd = new RootBeanDefinition(mbd);
		}
		if (!bd.isPrototype()) {
			bd.setScope(SCOPE_PROTOTYPE);
			bd.allowCaching = ClassUtils.isCacheSafe(ClassUtils.getUserClass(existingBean), getBeanClassLoader());
		}
		// bean 包装接口
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		// 设置属性
		populateBean(beanName, bd, bw);
		// 实例化 bean
		return initializeBean(beanName, existingBean, bd);
	}


	//-------------------------------------------------------------------------
	// Specialized methods for fine-grained control over the bean lifecycle
	//-------------------------------------------------------------------------

	@Override
	public Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
		// Use non-singleton bean definition, to avoid registering bean as dependent bean.
		RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
		bd.setScope(SCOPE_PROTOTYPE);
		return createBean(beanClass.getName(), bd, null);
	}

	@Override
	public Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
		// Use non-singleton bean definition, to avoid registering bean as dependent bean.
		final RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
		bd.setScope(SCOPE_PROTOTYPE);
		if (bd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR) {
			return autowireConstructor(beanClass.getName(), bd, null, null).getWrappedInstance();
		}
		else {
			Object bean;
			final BeanFactory parent = this;
			if (System.getSecurityManager() != null) {
				bean = AccessController.doPrivileged(
						(PrivilegedAction<Object>) () ->
								getInstantiationStrategy().instantiate(bd, null, parent),
						getAccessControlContext()
				);
			}
			else {
				bean = getInstantiationStrategy().instantiate(bd, null, parent);
			}
			populateBean(beanClass.getName(), bd, new BeanWrapperImpl(bean));
			return bean;
		}
	}

	@Override
	public void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException {

		// 注入形式是否是构造函数注入
		if (autowireMode == AUTOWIRE_CONSTRUCTOR) {
			throw new IllegalArgumentException("AUTOWIRE_CONSTRUCTOR not supported for existing bean instance");
		}
		// Use non-singleton bean definition, to avoid registering bean as dependent bean.
		// beanDefinition 创建
		RootBeanDefinition bd =
				new RootBeanDefinition(ClassUtils.getUserClass(existingBean), autowireMode, dependencyCheck);
		bd.setScope(SCOPE_PROTOTYPE);
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		populateBean(bd.getBeanClass().getName(), bd, bw);
	}

	@Override
	public void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException {
		// 标记 beanName 正在创建
		markBeanAsCreated(beanName);
		BeanDefinition bd = getMergedBeanDefinition(beanName);
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		// 应用属性
		applyPropertyValues(beanName, bd, bw, bd.getPropertyValues());
	}

	@Override
	public Object initializeBean(Object existingBean, String beanName) {
		return initializeBean(beanName, existingBean, null);
	}

	/**
	 * 前置方法执行
	 * @param existingBean the existing bean instance
	 * @param beanName the name of the bean, to be passed to it if necessary
	 * (only passed to {@link BeanPostProcessor BeanPostProcessors};
	 * can follow the {@link #ORIGINAL_INSTANCE_SUFFIX} convention in order to
	 * enforce the given instance to be returned, i.e. no proxies etc)
	 * @return
	 * @throws BeansException
	 */
	@Override
	public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
			throws BeansException {

		Object result = existingBean;
		for (BeanPostProcessor processor : getBeanPostProcessors()) {
			Object current = processor.postProcessBeforeInitialization(result, beanName);
			if (current == null) {
				return result;
			}
			result = current;
		}
		return result;
	}

	/**
	 * 后置方法执行
	 * @param existingBean the existing bean instance
	 * @param beanName the name of the bean, to be passed to it if necessary
	 * (only passed to {@link BeanPostProcessor BeanPostProcessors};
	 * can follow the {@link #ORIGINAL_INSTANCE_SUFFIX} convention in order to
	 * enforce the given instance to be returned, i.e. no proxies etc)
	 * @return
	 * @throws BeansException
	 */
	@Override
	public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
			throws BeansException {

		Object result = existingBean;
		for (BeanPostProcessor processor : getBeanPostProcessors()) {
			// 执行 spring 容器中 BeanPostProcessor
			Object current = processor.postProcessAfterInitialization(result, beanName);
			if (current == null) {
				return result;
			}
			result = current;
		}
		return result;
	}

	@Override
	public void destroyBean(Object existingBean) {
		new DisposableBeanAdapter(existingBean, getBeanPostProcessors(), getAccessControlContext()).destroy();
	}


	//-------------------------------------------------------------------------
	// Delegate methods for resolving injection points
	//-------------------------------------------------------------------------

	@Override
	public Object resolveBeanByName(String name, DependencyDescriptor descriptor) {
		InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
		try {
			return getBean(name, descriptor.getDependencyType());
		}
		finally {
			ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
		}
	}

	@Override
	@Nullable
	public Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName) throws BeansException {
		return resolveDependency(descriptor, requestingBeanName, null, null);
	}


	//---------------------------------------------------------------------
	// Implementation of relevant AbstractBeanFactory template methods
	//---------------------------------------------------------------------

	/**
	 * Central method of this class: creates a bean instance, populates the bean instance, applies
	 * post-processors, etc.
	 *
	 * @param beanName beanName
	 * @param mbd beanDefinition
	 * @param args 参数
	 * @see #doCreateBean
	 */
	@Override
	protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
			throws BeanCreationException {

		// 第一部分: 准备 BeanDefinition
		if (logger.isTraceEnabled()) {
			logger.trace("Creating instance of bean '" + beanName + "'");
		}
		// beanDefinition
		RootBeanDefinition mbdToUse = mbd;

		// Make sure bean class is actually resolved at this point, and
		// clone the bean definition in case of a dynamically resolved Class
		// which cannot be stored in the shared merged bean definition.
		// 获取当前需要加载的类
		Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
		// 1. 待处理的类不为空
		// 2. bean定义中含有 beanClass
		// 3. className 不为空
		// 满足、上述三点的情况下会去创建 RootBeanDefinition
		if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
			// 创建 beanDefinition
			mbdToUse = new RootBeanDefinition(mbd);
			// 设置bean class
			mbdToUse.setBeanClass(resolvedClass);
		}


		// 第二部分：BeanDefition 中处理重写方法
		// Prepare method overrides.
		try {
			// 方法重写
			mbdToUse.prepareMethodOverrides();
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
					beanName, "Validation of method overrides failed", ex
			);
		}

		// 第三部分：bean 创建之前的处理
		try {
			// Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
			// 创建bean之前的行为
			Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
			if (bean != null) {
				return bean;
			}
		}
		catch (Throwable ex) {
			throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
					"BeanPostProcessor before instantiation of bean failed", ex
			);
		}

		// 第四部分
		try {
			// 创建bean
			Object beanInstance = doCreateBean(beanName, mbdToUse, args);
			if (logger.isTraceEnabled()) {
				logger.trace("Finished creating instance of bean '" + beanName + "'");
			}
			return beanInstance;
		}
		catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
			// A previously detected exception with proper bean creation context already,
			// or illegal singleton state to be communicated up to DefaultSingletonBeanRegistry.
			throw ex;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
		}
	}

	/**
	 * Actually create the specified bean. Pre-creation processing has already happened at this
	 * point, e.g. checking {@code postProcessBeforeInstantiation} callbacks.
	 * <p>Differentiates between default bean instantiation, use of a
	 * factory method, and autowiring a constructor.
	 *
	 * 创建bean
	 * @param beanName the name of the bean
	 * @param mbd      the merged bean definition for the bean
	 * @param args     explicit arguments to use for constructor or factory method invocation
	 *
	 * @return a new instance of the bean
	 *
	 * @throws BeanCreationException if the bean could not be created
	 * @see #instantiateBean
	 * @see #instantiateUsingFactoryMethod
	 * @see #autowireConstructor
	 */
	protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args)
			throws BeanCreationException {

		// 第一部分: 获取实例
		// Instantiate the bean.
		BeanWrapper instanceWrapper = null;
		// 是否单例
		if (mbd.isSingleton()) {
			// beanFactory 移除当前创建的beanName
			instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
		}
		// beanWrapper 是否存在
		if (instanceWrapper == null) {
			// 创建 bean 实例
			instanceWrapper = createBeanInstance(beanName, mbd, args);
		}


		// 第二部分 后置方法调用
		// 获取 实例
		final Object bean = instanceWrapper.getWrappedInstance();
		// beanWrapper中存储的实例.class
		Class<?> beanType = instanceWrapper.getWrappedClass();
		if (beanType != NullBean.class) {
			mbd.resolvedTargetType = beanType;
		}

		// Allow post-processors to modify the merged bean definition.
		synchronized (mbd.postProcessingLock) {
			if (!mbd.postProcessed) {
				try {
					// 后置方法执行 BeanPostProcessor -> MergedBeanDefinitionPostProcessor
					applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
				}
				catch (Throwable ex) {
					throw new BeanCreationException(mbd.getResourceDescription(), beanName,
							"Post-processing of merged bean definition failed", ex
					);
				}
				mbd.postProcessed = true;
			}
		}


		// 第三部分
		// Eagerly cache singletons to be able to resolve circular references
		// even when triggered by lifecycle interfaces like BeanFactoryAware.
		// 是否需要提前暴露
		boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
				isSingletonCurrentlyInCreation(beanName));

		// 单例对象暴露
		if (earlySingletonExposure) {
			if (logger.isTraceEnabled()) {
				logger.trace("Eagerly caching bean '" + beanName +
						"' to allow for resolving potential circular references");
			}
			// 添加单例工厂
			addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
		}

		// 第四部分
		// 实例化bean
		// Initialize the bean instance.
		Object exposedObject = bean;
		try {
			// 设置属性
			populateBean(beanName, mbd, instanceWrapper);
			// 实例化bean
			exposedObject = initializeBean(beanName, exposedObject, mbd);
		}
		catch (Throwable ex) {
			if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
				throw (BeanCreationException) ex;
			}
			else {
				throw new BeanCreationException(
						mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
			}
		}


		// 第五部分
		// 依赖相关处理
		if (earlySingletonExposure) {
			Object earlySingletonReference = getSingleton(beanName, false);
			if (earlySingletonReference != null) {
				if (exposedObject == bean) {
					exposedObject = earlySingletonReference;
				}
				else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
					// 当前bean的依赖列表
					String[] dependentBeans = getDependentBeans(beanName);
					// 当前bean的依赖列表
					Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
					for (String dependentBean : dependentBeans) {
						if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
							actualDependentBeans.add(dependentBean);
						}
					}
					if (!actualDependentBeans.isEmpty()) {
						throw new BeanCurrentlyInCreationException(
								beanName,
								"Bean with name '" + beanName + "' has been injected into other beans [" +
										StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
										"] in its raw version as part of a circular reference, but has eventually been " +
										"wrapped. This means that said other beans do not use the final version of the " +
										"bean. This is often the result of over-eager type matching - consider using " +
										"'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example."
						);
					}
				}
			}
		}

		// 第六部分
		// Register bean as disposable.
		try {
			// 注册bean(一次性的bean)
			registerDisposableBeanIfNecessary(beanName, bean, mbd);
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanCreationException(
					mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
		}

		return exposedObject;
	}

	/**
	 * 类型推测
	 * @param beanName the name of the bean
	 * @param mbd the merged bean definition to determine the type for
	 * @param typesToMatch the types to match in case of internal type matching purposes
	 * (also signals that the returned {@code Class} will never be exposed to application code)
	 * @return
	 */
	@Override
	@Nullable
	protected Class<?> predictBeanType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
		// determineTargetType 进行推断
		Class<?> targetType = determineTargetType(beanName, mbd, typesToMatch);
		// Apply SmartInstantiationAwareBeanPostProcessors to predict the
		// eventual type after a before-instantiation shortcut.
		// 通过 SmartInstantiationAwareBeanPostProcessor 的  predictBeanType 得到类型
		if (targetType != null && !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			boolean matchingOnlyFactoryBean = typesToMatch.length == 1 && typesToMatch[0] == FactoryBean.class;
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
					SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
					Class<?> predicted = ibp.predictBeanType(targetType, beanName);
					if (predicted != null &&
							(!matchingOnlyFactoryBean || FactoryBean.class.isAssignableFrom(predicted))) {
						return predicted;
					}
				}
			}
		}
		return targetType;
	}

	/**
	 * Determine the target type for the given bean definition.
	 * 确定 bean 的类型
	 * @param beanName     the name of the bean (for error handling purposes)
	 * @param mbd          the merged bean definition for the bean
	 * @param typesToMatch the types to match in case of internal type matching purposes (also
	 *                     signals that the returned {@code Class} will never be exposed to
	 *                     application code)
	 *
	 * @return the type for the bean if determinable, or {@code null} otherwise
	 */
	@Nullable
	protected Class<?> determineTargetType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
		// beanDefinition 中的类型
		Class<?> targetType = mbd.getTargetType();
		// 不存在的情况下处理方式
		if (targetType == null) {
			targetType = (mbd.getFactoryMethodName() != null ?
					getTypeForFactoryMethod(beanName, mbd, typesToMatch) :
					resolveBeanClass(mbd, beanName, typesToMatch));
			if (ObjectUtils.isEmpty(typesToMatch) || getTempClassLoader() == null) {
				mbd.resolvedTargetType = targetType;
			}
		}
		return targetType;
	}

	/**
	 * Determine the target type for the given bean definition which is based on a factory method.
	 * Only called if there is no singleton instance registered for the target bean already.
	 * <p>This implementation determines the type matching {@link #createBean}'s
	 * different creation strategies. As far as possible, we'll perform static type checking to
	 * avoid creation of the target bean.
	 *
	 * 获取工厂方法的类型
	 * @param beanName     the name of the bean (for error handling purposes)
	 * @param mbd          the merged bean definition for the bean
	 * @param typesToMatch the types to match in case of internal type matching purposes (also
	 *                     signals that the returned {@code Class} will never be exposed to
	 *                     application code)
	 *
	 * @return the type for the bean if determinable, or {@code null} otherwise
	 *
	 * @see #createBean
	 */
	@Nullable
	protected Class<?> getTypeForFactoryMethod(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
		// 获取 工厂方法的回调类型解析器
		ResolvableType cachedReturnType = mbd.factoryMethodReturnType;
		if (cachedReturnType != null) {
			// 类型解析器中获取类型
			return cachedReturnType.resolve();
		}

		// 公共父类
		Class<?> commonType = null;
		// 工厂方法
		Method uniqueCandidate = mbd.factoryMethodToIntrospect;

		if (uniqueCandidate == null) {
			Class<?> factoryClass;
			boolean isStatic = true;

			// 工厂bean名称
			String factoryBeanName = mbd.getFactoryBeanName();
			if (factoryBeanName != null) {
				// 工厂 bean 名称和 参数 bean名称是否相同
				if (factoryBeanName.equals(beanName)) {
					throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName,
							"factory-bean reference points back to the same bean definition"
					);
				}
				// Check declared factory method return type on factory class.
				// 获取 工厂bean的类型
				factoryClass = getType(factoryBeanName);
				isStatic = false;
			}
			else {
				// Check declared factory method return type on bean class.
				// 解析 bean 类型
				factoryClass = resolveBeanClass(mbd, beanName, typesToMatch);
			}

			if (factoryClass == null) {
				return null;
			}
			// cglib 中的处理
			factoryClass = ClassUtils.getUserClass(factoryClass);

			// If all factory methods have the same return type, return that type.
			// Can't clearly figure out exact method due to type converting / autowiring!
			int minNrOfArgs =
					(mbd.hasConstructorArgumentValues() ? mbd.getConstructorArgumentValues().getArgumentCount() : 0);
			// 候选函数列表
			Method[] candidates = this.factoryMethodCandidateCache.computeIfAbsent(
					factoryClass,
					clazz -> ReflectionUtils.getUniqueDeclaredMethods(clazz, ReflectionUtils.USER_DECLARED_METHODS)
			);

			for (Method candidate : candidates) {
				// 过滤条件
				if (Modifier.isStatic(candidate.getModifiers()) == isStatic && mbd.isFactoryMethod(candidate) &&
						candidate.getParameterCount() >= minNrOfArgs) {
					// Declared type variables to inspect?
					if (candidate.getTypeParameters().length > 0) {
						try {
							// Fully resolve parameter names and argument values.
							// 函数的参数类型
							Class<?>[] paramTypes = candidate.getParameterTypes();
							String[] paramNames = null;
							// 获取 参数名称发现器
							ParameterNameDiscoverer pnd = getParameterNameDiscoverer();
							if (pnd != null) {
								// 解析参数名称
								paramNames = pnd.getParameterNames(candidate);
							}
							// 构造函数参数对象
							ConstructorArgumentValues cav = mbd.getConstructorArgumentValues();
							// 获取 ConstructorArgumentValues 中的 ValueHolder
							Set<ConstructorArgumentValues.ValueHolder> usedValueHolders = new HashSet<>(paramTypes.length);
							// 参数对象
							Object[] args = new Object[paramTypes.length];
							// 循环中处理两个集合
							// 1. usedValueHolders 放入  ValueHolder
							// 2.  args 放入 ValueHolder 中存储的值
							for (int i = 0; i < args.length; i++) {
								ConstructorArgumentValues.ValueHolder valueHolder = cav.getArgumentValue(
										i, paramTypes[i], (paramNames != null ? paramNames[i] : null), usedValueHolders);
								if (valueHolder == null) {
									valueHolder = cav.getGenericArgumentValue(null, null, usedValueHolders);
								}
								if (valueHolder != null) {
									args[i] = valueHolder.getValue();
									usedValueHolders.add(valueHolder);
								}
							}

							// 获取返回值
							Class<?> returnType = AutowireUtils.resolveReturnTypeForFactoryMethod(
									candidate, args, getBeanClassLoader());
							uniqueCandidate = (commonType == null && returnType == candidate.getReturnType() ?
									candidate : null);

							// 公共父类获取
							commonType = ClassUtils.determineCommonAncestor(returnType, commonType);
							if (commonType == null) {
								// Ambiguous return types found: return null to indicate "not determinable".
								return null;
							}
						}
						catch (Throwable ex) {
							if (logger.isDebugEnabled()) {
								logger.debug("Failed to resolve generic return type for factory method: " + ex);
							}
						}
					}
					else {
						uniqueCandidate = (commonType == null ? candidate : null);
						// 公共父类获取
						commonType = ClassUtils.determineCommonAncestor(candidate.getReturnType(), commonType);
						if (commonType == null) {
							// Ambiguous return types found: return null to indicate "not determinable".
							return null;
						}
					}
				}
			}

			mbd.factoryMethodToIntrospect = uniqueCandidate;
			if (commonType == null) {
				return null;
			}
		}

		// Common return type found: all factory methods return same type. For a non-parameterized
		// unique candidate, cache the full type declaration context of the target factory method.
		// 获取 return 值
		cachedReturnType = (uniqueCandidate != null ?
				ResolvableType.forMethodReturnType(uniqueCandidate) : ResolvableType.forClass(commonType));
		mbd.factoryMethodReturnType = cachedReturnType;
		return cachedReturnType.resolve();
	}

	/**
	 * This implementation attempts to query the FactoryBean's generic parameter metadata if present
	 * to determine the object type. If not present, i.e. the FactoryBean is declared as a raw type,
	 * checks the FactoryBean's {@code getObjectType} method on a plain instance of the FactoryBean,
	 * without bean properties applied yet. If this doesn't return a type yet, and {@code allowInit}
	 * is {@code true} a full creation of the FactoryBean is used as fallback (through delegation to
	 * the superclass's implementation).
	 * <p>The shortcut check for a FactoryBean is only applied in case of a singleton
	 * FactoryBean. If the FactoryBean instance itself is not kept as singleton, it will be fully
	 * created to check the type of its exposed object.
	 */
	@Override
	protected ResolvableType getTypeForFactoryBean(String beanName, RootBeanDefinition mbd, boolean allowInit) {
		// 第一部分
		// Check if the bean definition itself has defined the type with an attribute
		// 从 属性中获取
		ResolvableType result = getTypeForFactoryBeanFromAttributes(mbd);
		if (result != ResolvableType.NONE) {
			return result;
		}

		// 第二部分
		// bean class 转换成 ResolvableType
		ResolvableType beanType =
				(mbd.hasBeanClass() ? ResolvableType.forClass(mbd.getBeanClass()) : ResolvableType.NONE);

		// bean 类型处理
		// For instance supplied beans try the target type and bean class
		if (mbd.getInstanceSupplier() != null) {
			// bean 目标类型处理
			result = getFactoryBeanGeneric(mbd.targetType);
			if (result.resolve() != null) {
				return result;
			}
			// bean 原始类型处理
			result = getFactoryBeanGeneric(beanType);
			if (result.resolve() != null) {
				return result;
			}
		}

		// 第三部分

		// Consider factory methods
		// 提取 factory Bean Name
		String factoryBeanName = mbd.getFactoryBeanName();
		// 提取 factory method
		String factoryMethodName = mbd.getFactoryMethodName();

		// Scan the factory bean methods
		if (factoryBeanName != null) {
			if (factoryMethodName != null) {
				// Try to obtain the FactoryBean's object type from its factory method
				// declaration without instantiating the containing bean at all.
				// 获取 bean Definition
				BeanDefinition factoryBeanDefinition = getBeanDefinition(factoryBeanName);
				Class<?> factoryBeanClass;
				if (factoryBeanDefinition instanceof AbstractBeanDefinition &&
						((AbstractBeanDefinition) factoryBeanDefinition).hasBeanClass()) {
					// 从 bean definition 中获取bean类型
					factoryBeanClass = ((AbstractBeanDefinition) factoryBeanDefinition).getBeanClass();
				}
				else {
					// 获取合并的beanDefinition
					RootBeanDefinition fbmbd = getMergedBeanDefinition(factoryBeanName, factoryBeanDefinition);
					// bean 类型推测
					factoryBeanClass = determineTargetType(factoryBeanName, fbmbd);
				}
				if (factoryBeanClass != null) {
					// 从 工厂方法中获取类型描述 ResolvableType
					result = getTypeForFactoryBeanFromMethod(factoryBeanClass, factoryMethodName);
					if (result.resolve() != null) {
						return result;
					}
				}
			}
			// If not resolvable above and the referenced factory bean doesn't exist yet,
			// exit here - we don't want to force the creation of another bean just to
			// obtain a FactoryBean's object type...
			// 是否需要做元数据缓存
			if (!isBeanEligibleForMetadataCaching(factoryBeanName)) {
				return ResolvableType.NONE;
			}
		}

		// 第四部分
		// If we're allowed, we can create the factory bean and call getObjectType() early
		if (allowInit) {
			FactoryBean<?> factoryBean = (mbd.isSingleton() ?
					// 单例 工厂bean验证
					getSingletonFactoryBeanForTypeCheck(beanName, mbd) :
					// 非 单例 工厂bean验证
					getNonSingletonFactoryBeanForTypeCheck(beanName, mbd));
			if (factoryBean != null) {
				// Try to obtain the FactoryBean's object type from this early stage of the instance.
				// factoryBean 中获取bean类型
				Class<?> type = getTypeForFactoryBean(factoryBean);
				if (type != null) {
					// 转换成  ResolvableType对象
					return ResolvableType.forClass(type);
				}
				// No type found for shortcut FactoryBean instance:
				// fall back to full creation of the FactoryBean instance.
				// 父类 通过 doGetBean 然后转换成 ResolvableType 对象
				return super.getTypeForFactoryBean(beanName, mbd, true);
			}
		}

		// 第五部分
		if (factoryBeanName == null && mbd.hasBeanClass() && factoryMethodName != null) {
			// No early bean instantiation possible: determine FactoryBean's type from
			// static factory method signature or from class inheritance hierarchy...
			// 从 工厂方法中获取类型描述 ResolvableType
			return getTypeForFactoryBeanFromMethod(mbd.getBeanClass(), factoryMethodName);
		}

		// bean 原始类型处理
		result = getFactoryBeanGeneric(beanType);
		// 解析结果存在
		if (result.resolve() != null) {
			return result;
		}
		// 返回空
		return ResolvableType.NONE;
	}

	private ResolvableType getFactoryBeanGeneric(@Nullable ResolvableType type) {
		if (type == null) {
			return ResolvableType.NONE;
		}
		return type.as(FactoryBean.class).getGeneric();
	}

	/**
	 * Introspect the factory method signatures on the given bean class, trying to find a common
	 * {@code FactoryBean} object type declared there.
	 *
	 * 从 工厂方法中获取类型描述 ResolvableType
	 * @param beanClass         the bean class to find the factory method on
	 * @param factoryMethodName the name of the factory method
	 *
	 * @return the common {@code FactoryBean} object type, or {@code null} if none
	 */
	private ResolvableType getTypeForFactoryBeanFromMethod(Class<?> beanClass, String factoryMethodName) {
		// CGLIB subclass methods hide generic parameters; look at the original user class.
		// 获取bean类型
		Class<?> factoryBeanClass = ClassUtils.getUserClass(beanClass);
		// 创建查询类
		FactoryBeanMethodTypeFinder finder = new FactoryBeanMethodTypeFinder(factoryMethodName);
		// 执行方法 `doWith`
		ReflectionUtils.doWithMethods(factoryBeanClass, finder, ReflectionUtils.USER_DECLARED_METHODS);
		// 获取结果
		return finder.getResult();
	}

	/**
	 * This implementation attempts to query the FactoryBean's generic parameter metadata if present
	 * to determine the object type. If not present, i.e. the FactoryBean is declared as a raw type,
	 * checks the FactoryBean's {@code getObjectType} method on a plain instance of the FactoryBean,
	 * without bean properties applied yet. If this doesn't return a type yet, a full creation of
	 * the FactoryBean is used as fallback (through delegation to the superclass's implementation).
	 * <p>The shortcut check for a FactoryBean is only applied in case of a singleton
	 * FactoryBean. If the FactoryBean instance itself is not kept as singleton, it will be fully
	 * created to check the type of its exposed object.
	 */
	@Override
	@Deprecated
	@Nullable
	protected Class<?> getTypeForFactoryBean(String beanName, RootBeanDefinition mbd) {
		return getTypeForFactoryBean(beanName, mbd, true).resolve();
	}

	/**
	 * Obtain a reference for early access to the specified bean, typically for the purpose of
	 * resolving a circular reference.
	 *
	 * @param beanName the name of the bean (for error handling purposes)
	 * @param mbd      the merged bean definition for the bean
	 * @param bean     the raw bean instance
	 *
	 * @return the object to expose as bean reference
	 */
	protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
		Object exposedObject = bean;
		if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
					SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
					exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName);
				}
			}
		}
		return exposedObject;
	}


	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------

	/**
	 * Obtain a "shortcut" singleton FactoryBean instance to use for a {@code getObjectType()} call,
	 * without full initialization of the FactoryBean.
	 *
	 * @param beanName the name of the bean
	 * @param mbd      the bean definition for the bean
	 *
	 * @return the FactoryBean instance, or {@code null} to indicate that we couldn't obtain a
	 * shortcut FactoryBean instance
	 */
	@Nullable
	private FactoryBean<?> getSingletonFactoryBeanForTypeCheck(String beanName, RootBeanDefinition mbd) {
		synchronized (getSingletonMutex()) {
			// 获取 beanName 对应的 BeanWrapper
			BeanWrapper bw = this.factoryBeanInstanceCache.get(beanName);

			if (bw != null) {
				// 直接返回 beanWrapper 的强制转换
				return (FactoryBean<?>) bw.getWrappedInstance();
			}
			// 获取单例对象
			Object beanInstance = getSingleton(beanName, false);
			// 类型比较
			if (beanInstance instanceof FactoryBean) {
				return (FactoryBean<?>) beanInstance;
			}
			if (isSingletonCurrentlyInCreation(beanName) ||
					(mbd.getFactoryBeanName() != null && isSingletonCurrentlyInCreation(mbd.getFactoryBeanName()))) {
				return null;
			}

			Object instance;
			try {
				// Mark this bean as currently in creation, even if just partially.
				// 创建前的验证
				beforeSingletonCreation(beanName);
				// Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
				// 实例解析
				instance = resolveBeforeInstantiation(beanName, mbd);
				if (instance == null) {
					// 创建 beanWrapper
					bw = createBeanInstance(beanName, mbd, null);
					// 获取实例
					instance = bw.getWrappedInstance();
				}
			}
			catch (UnsatisfiedDependencyException ex) {
				// Don't swallow, probably misconfiguration...
				throw ex;
			}
			catch (BeanCreationException ex) {
				// Instantiation failure, maybe too early...
				if (logger.isDebugEnabled()) {
					logger.debug("Bean creation exception on singleton FactoryBean type check: " + ex);
				}
				onSuppressedException(ex);
				return null;
			}
			finally {
				// Finished partial creation of this bean.
				// 创建后的行为
				afterSingletonCreation(beanName);
			}

			// 获取 factoryBean
			// instance 强转 FactoryBean
			FactoryBean<?> fb = getFactoryBean(beanName, instance);
			if (bw != null) {
				// 置入容器
				this.factoryBeanInstanceCache.put(beanName, bw);
			}
			return fb;
		}
	}

	/**
	 * Obtain a "shortcut" non-singleton FactoryBean instance to use for a {@code getObjectType()}
	 * call, without full initialization of the FactoryBean.
	 *
	 * @param beanName the name of the bean
	 * @param mbd      the bean definition for the bean
	 *
	 * @return the FactoryBean instance, or {@code null} to indicate that we couldn't obtain a
	 * shortcut FactoryBean instance
	 */
	@Nullable
	private FactoryBean<?> getNonSingletonFactoryBeanForTypeCheck(String beanName, RootBeanDefinition mbd) {
		// 是否处于原型模式下正在创建
		if (isPrototypeCurrentlyInCreation(beanName)) {
			return null;
		}

		Object instance;
		try {
			// Mark this bean as currently in creation, even if just partially.
			// 原型模式下创建的 前置函数
			beforePrototypeCreation(beanName);
			// Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
			// 实例解析
			instance = resolveBeforeInstantiation(beanName, mbd);
			if (instance == null) {
				// 创建 bean 实例
				BeanWrapper bw = createBeanInstance(beanName, mbd, null);
				instance = bw.getWrappedInstance();
			}
		}
		catch (UnsatisfiedDependencyException ex) {
			// Don't swallow, probably misconfiguration...
			throw ex;
		}
		catch (BeanCreationException ex) {
			// Instantiation failure, maybe too early...
			if (logger.isDebugEnabled()) {
				logger.debug("Bean creation exception on non-singleton FactoryBean type check: " + ex);
			}
			onSuppressedException(ex);
			return null;
		}
		finally {
			// Finished partial creation of this bean.
			// 原型模式下创建的 后置函数
			afterPrototypeCreation(beanName);
		}

		return getFactoryBean(beanName, instance);
	}

	/**
	 * Apply MergedBeanDefinitionPostProcessors to the specified bean definition, invoking their
	 * {@code postProcessMergedBeanDefinition} methods.
	 *
	 * @param mbd      the merged bean definition for the bean
	 * @param beanType the actual type of the managed bean instance
	 * @param beanName the name of the bean
	 *
	 * @see MergedBeanDefinitionPostProcessor#postProcessMergedBeanDefinition
	 */
	protected void applyMergedBeanDefinitionPostProcessors(RootBeanDefinition mbd, Class<?> beanType, String beanName) {
		for (BeanPostProcessor bp : getBeanPostProcessors()) {
			if (bp instanceof MergedBeanDefinitionPostProcessor) {
				MergedBeanDefinitionPostProcessor bdp = (MergedBeanDefinitionPostProcessor) bp;
				bdp.postProcessMergedBeanDefinition(mbd, beanType, beanName);
			}
		}
	}

	/**
	 * Apply before-instantiation post-processors, resolving whether there is a before-instantiation
	 * shortcut for the specified bean.
	 *
	 * @param beanName the name of the bean
	 * @param mbd      the bean definition for the bean
	 *
	 * @return the shortcut-determined bean instance, or {@code null} if none
	 */
	@Nullable
	protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
		Object bean = null;
		// 是否需要在 bean 初始化之前做一些关于 beanPostProcessor 相关的行为
		if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
			// Make sure bean class is actually resolved at this point.
			// 是否是合成bean
			// 是否拥有 BeanPostProcessor 接口
			if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
				// 目标类型确认
				Class<?> targetType = determineTargetType(beanName, mbd);
				// 前置方法 BeanPostProcessor 接口的调用
				if (targetType != null) {
					// postProcessBeforeInstantiation 执行
					bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
					if (bean != null) {
						// 后置方法
						// postProcessAfterInitialization 执行
						bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
					}
				}
			}
			mbd.beforeInstantiationResolved = (bean != null);
		}
		return bean;
	}

	/**
	 * Apply InstantiationAwareBeanPostProcessors to the specified bean definition (by class and
	 * name), invoking their {@code postProcessBeforeInstantiation} methods.
	 * <p>Any returned object will be used as the bean instead of actually instantiating
	 * the target bean. A {@code null} return value from the post-processor will result in the
	 * target bean being instantiated.
	 *
	 * @param beanClass the class of the bean to be instantiated
	 * @param beanName  the name of the bean
	 *
	 * @return the bean object to use instead of a default instance of the target bean, or {@code
	 * null}
	 *
	 * @see InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation
	 */
	@Nullable
	protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
		for (BeanPostProcessor bp : getBeanPostProcessors()) {
			if (bp instanceof InstantiationAwareBeanPostProcessor) {
				InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
				// 调用自定义实现
				Object result = ibp.postProcessBeforeInstantiation(beanClass, beanName);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * Create a new instance for the specified bean, using an appropriate instantiation strategy:
	 * factory method, constructor autowiring, or simple instantiation.
	 * bean 初始化
	 * @param beanName the name of the bean
	 * @param mbd      the bean definition for the bean
	 * @param args     explicit arguments to use for constructor or factory method invocation
	 *
	 * @return a BeanWrapper for the new instance , bean 的包装结果
	 *
	 * @see #obtainFromSupplier
	 * @see #instantiateUsingFactoryMethod
	 * @see #autowireConstructor
	 * @see #instantiateBean
	 */
	protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) {
		// Make sure bean class is actually resolved at this point.
		// 第一部分
		// 获取 bean class
		Class<?> beanClass = resolveBeanClass(mbd, beanName);
		// bean 是否可以被创建的验证
		if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName,
					"Bean class isn't public, and non-public access not allowed: " + beanClass.getName()
			);
		}

		// 第二部分
		// 返回一个用来创建bean实例的回调接口
		// Supplier get 直接获取bean对象
		Supplier<?> instanceSupplier = mbd.getInstanceSupplier();
		if (instanceSupplier != null) {
			return obtainFromSupplier(instanceSupplier, beanName);
		}

		// 第三部分
		if (mbd.getFactoryMethodName() != null) {
			// 通过工厂方法创建
			return instantiateUsingFactoryMethod(beanName, mbd, args);
		}

		// 第四部分
		// Shortcut when re-creating the same bean...
		// 是否已经处理完成的标记
		boolean resolved = false;
		// 是否需要自动注入的标记
		boolean autowireNecessary = false;
		if (args == null) {
			synchronized (mbd.constructorArgumentLock) {
				if (mbd.resolvedConstructorOrFactoryMethod != null) {
					resolved = true;
					autowireNecessary = mbd.constructorArgumentsResolved;
				}
			}
		}
		// 第五部分
		if (resolved) {
			if (autowireNecessary) {
				// 自动构造 bean
				return autowireConstructor(beanName, mbd, null, null);
			}
			else {
				// 实例化bean
				return instantiateBean(beanName, mbd);
			}
		}

		// 第六部分
		// Candidate constructors for autowiring?
		// 确定构造函数列表
		Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
		if (ctors != null || mbd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR ||
				mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) {
			// 自动装配处理
			return autowireConstructor(beanName, mbd, ctors, args);
		}

		// Preferred constructors for default construction?
		ctors = mbd.getPreferredConstructors();
		if (ctors != null) {
			// 自动装配处理
			return autowireConstructor(beanName, mbd, ctors, null);
		}

		// No special handling: simply use no-arg constructor.
		// 实例化 bean
		return instantiateBean(beanName, mbd);
	}

	/**
	 * Obtain a bean instance from the given supplier.
	 *
	 * 从 supplier 中获取对象
	 * @param instanceSupplier the configured supplier
	 * @param beanName         the corresponding bean name
	 *
	 * @return a BeanWrapper for the new instance
	 *
	 * @see #getObjectForBeanInstance
	 * @since 5.0
	 */
	protected BeanWrapper obtainFromSupplier(Supplier<?> instanceSupplier, String beanName) {
		Object instance;

		// 获取当前的beanName
		String outerBean = this.currentlyCreatedBean.get();
		// 设置当前处理的beanName
		this.currentlyCreatedBean.set(beanName);
		try {
			// 从 Supplier 中获取
			instance = instanceSupplier.get();
		}
		finally {
			if (outerBean != null) {
				// 如果 currentlyCreatedBean 取不到设置
				this.currentlyCreatedBean.set(outerBean);
			}
			else {
				// 移除
				this.currentlyCreatedBean.remove();
			}
		}

		if (instance == null) {
			// supplier 中获取不到, 将实例设置为 NullBean
			instance = new NullBean();
		}
		// beanWrapper 包装
		BeanWrapper bw = new BeanWrapperImpl(instance);
		// beanWrapper 实例化后的操作
		initBeanWrapper(bw);
		return bw;
	}

	/**
	 * Overridden in order to implicitly register the currently created bean as dependent on further
	 * beans getting programmatically retrieved during a {@link Supplier} callback.
	 *
	 * @see #obtainFromSupplier
	 * @since 5.0
	 */
	@Override
	protected Object getObjectForBeanInstance(
			Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd) {

		String currentlyCreatedBean = this.currentlyCreatedBean.get();
		if (currentlyCreatedBean != null) {
			registerDependentBean(beanName, currentlyCreatedBean);
		}

		return super.getObjectForBeanInstance(beanInstance, name, beanName, mbd);
	}

	/**
	 * Determine candidate constructors to use for the given bean, checking all registered {@link
	 * SmartInstantiationAwareBeanPostProcessor SmartInstantiationAwareBeanPostProcessors}.
	 *
	 * 通过 BeanPostProcessor 的子接口  SmartInstantiationAwareBeanPostProcessor 来获取 Constructor 列表
	 * @param beanClass the raw class of the bean
	 * @param beanName  the name of the bean
	 *
	 * @return the candidate constructors, or {@code null} if none specified
	 *
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor#determineCandidateConstructors
	 */
	@Nullable
	protected Constructor<?>[] determineConstructorsFromBeanPostProcessors(@Nullable Class<?> beanClass, String beanName)
			throws BeansException {

		if (beanClass != null && hasInstantiationAwareBeanPostProcessors()) {
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
					SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
					//  SmartInstantiationAwareBeanPostProcessor 的实现类中获取
					Constructor<?>[] ctors = ibp.determineCandidateConstructors(beanClass, beanName);
					if (ctors != null) {
						return ctors;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Instantiate the given bean using its default constructor.
	 *
	 * 创建bean的包装
	 * @param beanName the name of the bean
	 * @param mbd      the bean definition for the bean
	 *
	 * @return a BeanWrapper for the new instance
	 */
	protected BeanWrapper instantiateBean(final String beanName, final RootBeanDefinition mbd) {
		try {
			Object beanInstance;
			final BeanFactory parent = this;
			if (System.getSecurityManager() != null) {
				// 获取实例化策略来进行实例化
				beanInstance = AccessController.doPrivileged(
						(PrivilegedAction<Object>) () ->
								getInstantiationStrategy().instantiate(mbd, beanName, parent),
						getAccessControlContext()
				);
			}
			else {
				// 获取实例化策略来进行实例化
				beanInstance = getInstantiationStrategy().instantiate(mbd, beanName, parent);
			}

			// beanWrapper 创建
			BeanWrapper bw = new BeanWrapperImpl(beanInstance);
			initBeanWrapper(bw);
			return bw;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					mbd.getResourceDescription(), beanName, "Instantiation of bean failed", ex);
		}
	}

	/**
	 * Instantiate the bean using a named factory method. The method may be static, if the mbd
	 * parameter specifies a class, rather than a factoryBean, or an instance variable on a factory
	 * object itself configured using Dependency Injection.
	 *
	 * @param beanName     the name of the bean
	 * @param mbd          the bean definition for the bean
	 * @param explicitArgs argument values passed in programmatically via the getBean method, or
	 *                     {@code null} if none (-> use constructor argument values from bean
	 *                     definition)
	 *
	 * @return a BeanWrapper for the new instance
	 *
	 * @see #getBean(String, Object[])
	 */
	protected BeanWrapper instantiateUsingFactoryMethod(
			String beanName, RootBeanDefinition mbd, @Nullable Object[] explicitArgs) {
		// 调用 factory-method 方法
		return new ConstructorResolver(this).instantiateUsingFactoryMethod(beanName, mbd, explicitArgs);
	}

	/**
	 * "autowire constructor" (with constructor arguments by type) behavior. Also applied if
	 * explicit constructor argument values are specified, matching all remaining arguments with
	 * beans from the bean factory.
	 * <p>This corresponds to constructor injection: In this mode, a Spring
	 * bean factory is able to host components that expect constructor-based dependency resolution.
	 *
	 * @param beanName     the name of the bean
	 * @param mbd          the bean definition for the bean
	 * @param ctors        the chosen candidate constructors
	 * @param explicitArgs argument values passed in programmatically via the getBean method, or
	 *                     {@code null} if none (-> use constructor argument values from bean
	 *                     definition)
	 *
	 * @return a BeanWrapper for the new instance
	 */
	protected BeanWrapper autowireConstructor(
			String beanName, RootBeanDefinition mbd, @Nullable Constructor<?>[] ctors, @Nullable Object[] explicitArgs) {

		return new ConstructorResolver(this).autowireConstructor(beanName, mbd, ctors, explicitArgs);
	}

	/**
	 * Populate the bean instance in the given BeanWrapper with the property values from the bean
	 * definition.
	 *
	 * 属性设置
	 * @param beanName the name of the bean
	 * @param mbd      the bean definition for the bean
	 * @param bw       the BeanWrapper with bean instance
	 */
	@SuppressWarnings("deprecation")  // for postProcessPropertyValues
	protected void populateBean(String beanName, RootBeanDefinition mbd, @Nullable BeanWrapper bw) {
		// 第一部分
		if (bw == null) {
			if (mbd.hasPropertyValues()) {
				throw new BeanCreationException(
						mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
			}
			else {
				// Skip property population phase for null instance.
				return;
			}
		}


		// 第二部分
		// 执行 InstantiationAwareBeanPostProcessor 的 postProcessAfterInstantiation 方法
		// Give any InstantiationAwareBeanPostProcessors the opportunity to modify the
		// state of the bean before properties are set. This can be used, for example,
		// to support styles of field injection.
		if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof InstantiationAwareBeanPostProcessor) {
					InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
					if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
						return;
					}
				}
			}
		}


		// 第三部分
		PropertyValues pvs = (mbd.hasPropertyValues() ? mbd.getPropertyValues() : null);
		// 获取自动注入的值(自动注入的类型)
		int resolvedAutowireMode = mbd.getResolvedAutowireMode();
		// 自动注入
		if (resolvedAutowireMode == AUTOWIRE_BY_NAME || resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
			MutablePropertyValues newPvs = new MutablePropertyValues(pvs);
			// Add property values based on autowire by name if applicable.
			if (resolvedAutowireMode == AUTOWIRE_BY_NAME) {
				// 按照名称注入
				autowireByName(beanName, mbd, bw, newPvs);
			}
			// Add property values based on autowire by type if applicable.
			if (resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
				// 按照类型注入
				autowireByType(beanName, mbd, bw, newPvs);
			}
			pvs = newPvs;
		}

		// 第四部分
		boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();
		boolean needsDepCheck = (mbd.getDependencyCheck() != AbstractBeanDefinition.DEPENDENCY_CHECK_NONE);

		PropertyDescriptor[] filteredPds = null;
		if (hasInstAwareBpps) {
			if (pvs == null) {
				pvs = mbd.getPropertyValues();
			}
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof InstantiationAwareBeanPostProcessor) {
					InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
					PropertyValues pvsToUse = ibp.postProcessProperties(pvs, bw.getWrappedInstance(), beanName);
					if (pvsToUse == null) {
						if (filteredPds == null) {
							filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
						}
						pvsToUse = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
						if (pvsToUse == null) {
							return;
						}
					}
					pvs = pvsToUse;
				}
			}
		}

		// 第五部分
		if (needsDepCheck) {
			if (filteredPds == null) {
				filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
			}
			// 依赖检查
			checkDependencies(beanName, mbd, filteredPds, pvs);
		}

		// 第六部分
		if (pvs != null) {
			// 应用属性
			applyPropertyValues(beanName, mbd, bw, pvs);
		}
	}

	/**
	 * Fill in any missing property values with references to other beans in this factory if
	 * autowire is set to "byName".
	 *
	 * @param beanName the name of the bean we're wiring up. Useful for debugging messages; not used
	 *                 functionally.
	 * @param mbd      bean definition to update through autowiring
	 * @param bw       the BeanWrapper from which we can obtain information about the bean
	 * @param pvs      the PropertyValues to register wired objects with
	 */
	protected void autowireByName(
			String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {

		// 获取属性名称列表 (非普通类型)
		String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
		// 循环赋值
		for (String propertyName : propertyNames) {
			// 属性是否是 bean
			if (containsBean(propertyName)) {
				// 获取 bean
				Object bean = getBean(propertyName);
				pvs.add(propertyName, bean);
				// 注册依赖
				registerDependentBean(propertyName, beanName);
				if (logger.isTraceEnabled()) {
					logger.trace("Added autowiring by name from bean name '" + beanName +
							"' via property '" + propertyName + "' to bean named '" + propertyName + "'");
				}
			}
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("Not autowiring property '" + propertyName + "' of bean '" + beanName +
							"' by name: no matching bean found");
				}
			}
		}
	}

	/**
	 * Abstract method defining "autowire by type" (bean properties by type) behavior.
	 * <p>This is like PicoContainer default, in which there must be exactly one bean
	 * of the property type in the bean factory. This makes bean factories simple to configure for
	 * small namespaces, but doesn't work as well as standard Spring behavior for bigger
	 * applications.
	 *
	 * @param beanName the name of the bean to autowire by type
	 * @param mbd      the merged bean definition to update through autowiring
	 * @param bw       the BeanWrapper from which we can obtain information about the bean
	 * @param pvs      the PropertyValues to register wired objects with
	 */
	protected void autowireByType(
			String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {

		// 类型转换器
		TypeConverter converter = getCustomTypeConverter();
		if (converter == null) {
			converter = bw;
		}

		// 需要注入的 beanName
		Set<String> autowiredBeanNames = new LinkedHashSet<>(4);
		// 获取属性名称列表 (非普通类型)
		String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
		// 循环处理 属性名称的bean
		for (String propertyName : propertyNames) {
			try {
				// 获取属性描述符
				PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
				// Don't try autowiring by type for type Object: never makes sense,
				// even if it technically is a unsatisfied, non-simple property.
				// 类型判断 不会给 Object 类型的数据做处理
				if (Object.class != pd.getPropertyType()) {
					// 函数参数
					MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
					// Do not allow eager init for type matching in case of a prioritized post-processor.
					// 类型是否是 PriorityOrdered
					boolean eager = !(bw.getWrappedInstance() instanceof PriorityOrdered);
					// 依赖描述对象
					DependencyDescriptor desc = new AutowireByTypeDependencyDescriptor(methodParam, eager);

					// 获得依赖对象
					// 实现在 org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency
					Object autowiredArgument = resolveDependency(desc, beanName, autowiredBeanNames, converter);
					if (autowiredArgument != null) {
						// 设置值
						pvs.add(propertyName, autowiredArgument);
					}
					// 依赖注册到容器
					for (String autowiredBeanName : autowiredBeanNames) {
						registerDependentBean(autowiredBeanName, beanName);
						if (logger.isTraceEnabled()) {
							logger.trace("Autowiring by type from bean name '" + beanName + "' via property '" +
									propertyName + "' to bean named '" + autowiredBeanName + "'");
						}
					}
					// 清理对象
					autowiredBeanNames.clear();
				}
			}
			catch (BeansException ex) {
				throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, propertyName, ex);
			}
		}
	}


	/**
	 * Return an array of non-simple bean properties that are unsatisfied. These are probably
	 * unsatisfied references to other beans in the factory. Does not include simple properties like
	 * primitives or Strings.
	 *
	 * @param mbd the merged bean definition the bean was created with
	 * @param bw  the BeanWrapper the bean was created with
	 *
	 * @return an array of bean property names
	 *
	 * @see org.springframework.beans.BeanUtils#isSimpleProperty
	 */
	protected String[] unsatisfiedNonSimpleProperties(AbstractBeanDefinition mbd, BeanWrapper bw) {
		Set<String> result = new TreeSet<>();
		// 获取 PropertyValues
		PropertyValues pvs = mbd.getPropertyValues();
		// 获取 属性描述符对象列表
		PropertyDescriptor[] pds = bw.getPropertyDescriptors();
		// 循环属性描述符 , 通过条件判断来确定是否是一个不简单的属性
		for (PropertyDescriptor pd : pds) {
			// 1. 可写函数是否存在
			// 2. 是否是依赖检查中排除的
			// 3. pvc 中是否包含
			// 4. 是否是一个简单类型
			if (pd.getWriteMethod() != null && !isExcludedFromDependencyCheck(pd) && !pvs.contains(pd.getName()) &&
					!BeanUtils.isSimpleProperty(pd.getPropertyType())) {
				result.add(pd.getName());
			}
		}
		return StringUtils.toStringArray(result);
	}

	/**
	 * Extract a filtered set of PropertyDescriptors from the given BeanWrapper, excluding ignored
	 * dependency types or properties defined on ignored dependency interfaces.
	 *
	 * @param bw    the BeanWrapper the bean was created with
	 * @param cache whether to cache filtered PropertyDescriptors for the given bean Class
	 *
	 * @return the filtered PropertyDescriptors
	 *
	 * @see #isExcludedFromDependencyCheck
	 * @see #filterPropertyDescriptorsForDependencyCheck(org.springframework.beans.BeanWrapper)
	 */
	protected PropertyDescriptor[] filterPropertyDescriptorsForDependencyCheck(BeanWrapper bw, boolean cache) {
		PropertyDescriptor[] filtered = this.filteredPropertyDescriptorsCache.get(bw.getWrappedClass());
		if (filtered == null) {
			filtered = filterPropertyDescriptorsForDependencyCheck(bw);
			if (cache) {
				PropertyDescriptor[] existing =
						this.filteredPropertyDescriptorsCache.putIfAbsent(bw.getWrappedClass(), filtered);
				if (existing != null) {
					filtered = existing;
				}
			}
		}
		return filtered;
	}

	/**
	 * Extract a filtered set of PropertyDescriptors from the given BeanWrapper, excluding ignored
	 * dependency types or properties defined on ignored dependency interfaces.
	 *
	 * @param bw the BeanWrapper the bean was created with
	 *
	 * @return the filtered PropertyDescriptors
	 *
	 * @see #isExcludedFromDependencyCheck
	 */
	protected PropertyDescriptor[] filterPropertyDescriptorsForDependencyCheck(BeanWrapper bw) {
		List<PropertyDescriptor> pds = new ArrayList<>(Arrays.asList(bw.getPropertyDescriptors()));
		pds.removeIf(this::isExcludedFromDependencyCheck);
		return pds.toArray(new PropertyDescriptor[0]);
	}

	/**
	 * Determine whether the given bean property is excluded from dependency checks.
	 * <p>This implementation excludes properties defined by CGLIB and
	 * properties whose type matches an ignored dependency type or which are defined by an ignored
	 * dependency interface.
	 *
	 * @param pd the PropertyDescriptor of the bean property
	 *
	 * @return whether the bean property is excluded
	 *
	 * @see #ignoreDependencyType(Class)
	 * @see #ignoreDependencyInterface(Class)
	 */
	protected boolean isExcludedFromDependencyCheck(PropertyDescriptor pd) {
		return (AutowireUtils.isExcludedFromDependencyCheck(pd) ||
				this.ignoredDependencyTypes.contains(pd.getPropertyType()) ||
				AutowireUtils.isSetterDefinedInInterface(pd, this.ignoredDependencyInterfaces));
	}

	/**
	 * Perform a dependency check that all properties exposed have been set, if desired. Dependency
	 * checks can be objects (collaborating beans), simple (primitives and String), or all (both).
	 *
	 * @param beanName the name of the bean
	 * @param mbd      the merged bean definition the bean was created with
	 * @param pds      the relevant property descriptors for the target bean
	 * @param pvs      the property values to be applied to the bean
	 *
	 * @see #isExcludedFromDependencyCheck(java.beans.PropertyDescriptor)
	 */
	protected void checkDependencies(
			String beanName, AbstractBeanDefinition mbd, PropertyDescriptor[] pds, @Nullable PropertyValues pvs)
			throws UnsatisfiedDependencyException {

		int dependencyCheck = mbd.getDependencyCheck();
		for (PropertyDescriptor pd : pds) {
			if (pd.getWriteMethod() != null && (pvs == null || !pvs.contains(pd.getName()))) {
				boolean isSimple = BeanUtils.isSimpleProperty(pd.getPropertyType());
				boolean unsatisfied = (dependencyCheck == AbstractBeanDefinition.DEPENDENCY_CHECK_ALL) ||
						(isSimple && dependencyCheck == AbstractBeanDefinition.DEPENDENCY_CHECK_SIMPLE) ||
						(!isSimple && dependencyCheck == AbstractBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
				if (unsatisfied) {
					throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, pd.getName(),
							"Set this property value or disable dependency checking for this bean."
					);
				}
			}
		}
	}

	/**
	 * Apply the given property values, resolving any runtime references to other beans in this bean
	 * factory. Must use deep copy, so we don't permanently modify this property.
	 *
	 * 应用属性
	 * @param beanName the bean name passed for better exception information
	 * @param mbd      the merged bean definition
	 * @param bw       the BeanWrapper wrapping the target object
	 * @param pvs      the new property values
	 */
	protected void applyPropertyValues(String beanName, BeanDefinition mbd, BeanWrapper bw, PropertyValues pvs) {
		// 第一部分
		if (pvs.isEmpty()) {
			return;
		}

		if (System.getSecurityManager() != null && bw instanceof BeanWrapperImpl) {
			((BeanWrapperImpl) bw).setSecurityContext(getAccessControlContext());
		}


		// 第二部分
		MutablePropertyValues mpvs = null;
		// 原始属性列表
		List<PropertyValue> original;
		// 类型判断
		if (pvs instanceof MutablePropertyValues) {
			// 强制类型转换
			mpvs = (MutablePropertyValues) pvs;
			if (mpvs.isConverted()) {
				// Shortcut: use the pre-converted values as-is.
				try {
					// 包装对象的属性设置
					bw.setPropertyValues(mpvs);
					return;
				}
				catch (BeansException ex) {
					throw new BeanCreationException(
							mbd.getResourceDescription(), beanName, "Error setting property values", ex);
				}
			}
			original = mpvs.getPropertyValueList();
		}
		else {
			original = Arrays.asList(pvs.getPropertyValues());
		}

		// 第三部分
		// 自定义转换器
		TypeConverter converter = getCustomTypeConverter();
		if (converter == null) {
			converter = bw;
		}
		//  创建BeanDefinitionValueResolver
		BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this, beanName, mbd, converter);

		// 第四部分
		// Create a deep copy, resolving any references for values.
		// 解析后的对象集合
		List<PropertyValue> deepCopy = new ArrayList<>(original.size());
		boolean resolveNecessary = false;
		for (PropertyValue pv : original) {
			// 解析过的属性
			if (pv.isConverted()) {
				deepCopy.add(pv);
			}
			// 没有解析过的属性
			else {
				// 属性名称
				String propertyName = pv.getName();
				// 属性值,直接读取到的
				Object originalValue = pv.getValue();
				// 是不是 AutowiredPropertyMarker 对象
				if (originalValue == AutowiredPropertyMarker.INSTANCE) {
					// 获取写方法. (set) 方法
					Method writeMethod = bw.getPropertyDescriptor(propertyName).getWriteMethod();
					if (writeMethod == null) {
						throw new IllegalArgumentException("Autowire marker for property without write method: " + pv);
					}
					// 装配描述对象
					originalValue = new DependencyDescriptor(new MethodParameter(writeMethod, 0), true);
				}
				// 解析值
				Object resolvedValue = valueResolver.resolveValueIfNecessary(pv, originalValue);
				Object convertedValue = resolvedValue;

				/**
				 * 1. isWritableProperty: 属性可写
				 * 2. isNestedOrIndexedProperty: 是否循环嵌套
				 */
				boolean convertible = bw.isWritableProperty(propertyName) &&
						!PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName);
				if (convertible) {
					// 转换器解析
					convertedValue = convertForProperty(resolvedValue, propertyName, bw, converter);
				}
				// Possibly store converted value in merged bean definition,
				// in order to avoid re-conversion for every created bean instance.
				if (resolvedValue == originalValue) {
					if (convertible) {
						pv.setConvertedValue(convertedValue);
					}
					deepCopy.add(pv);
				}
				// 类型解析
				else if (convertible && originalValue instanceof TypedStringValue &&
						!((TypedStringValue) originalValue).isDynamic() &&
						!(convertedValue instanceof Collection || ObjectUtils.isArray(convertedValue))) {
					pv.setConvertedValue(convertedValue);
					deepCopy.add(pv);
				}
				else {
					resolveNecessary = true;
					deepCopy.add(new PropertyValue(pv, convertedValue));
				}
			}
		}
		// 第五部分
		if (mpvs != null && !resolveNecessary) {
			// 设置转换成功的标记
			mpvs.setConverted();
		}

		// Set our (possibly massaged) deep copy.
		try {
			// 属性值设置
			bw.setPropertyValues(new MutablePropertyValues(deepCopy));
		}
		catch (BeansException ex) {
			throw new BeanCreationException(
					mbd.getResourceDescription(), beanName, "Error setting property values", ex);
		}
	}

	/**
	 * Convert the given value for the specified target property.
	 */
	@Nullable
	private Object convertForProperty(
			@Nullable Object value, String propertyName, BeanWrapper bw, TypeConverter converter) {

		if (converter instanceof BeanWrapperImpl) {
			return ((BeanWrapperImpl) converter).convertForProperty(value, propertyName);
		}
		else {
			PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
			MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
			return converter.convertIfNecessary(value, pd.getPropertyType(), methodParam);
		}
	}


	/**
	 * Initialize the given bean instance, applying factory callbacks as well as init methods and
	 * bean post processors.
	 * <p>Called from {@link #createBean} for traditionally defined beans,
	 * and from {@link #initializeBean} for existing bean instances.
	 *
	 * 实例化bean
	 * @param beanName the bean name in the factory (for debugging purposes)
	 * @param bean     the new bean instance we may need to initialize
	 * @param mbd      the bean definition that the bean was created with (can also be {@code null},
	 *                 if given an existing bean instance)
	 *
	 * @return the initialized bean instance (potentially wrapped)
	 *
	 * @see BeanNameAware
	 * @see BeanClassLoaderAware
	 * @see BeanFactoryAware
	 * @see #applyBeanPostProcessorsBeforeInitialization
	 * @see #invokeInitMethods
	 * @see #applyBeanPostProcessorsAfterInitialization
	 */
	protected Object initializeBean(final String beanName, final Object bean, @Nullable RootBeanDefinition mbd) {
		// 第一部分
		if (System.getSecurityManager() != null) {
			AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
				invokeAwareMethods(beanName, bean);
				return null;
			}, getAccessControlContext());
		}
		else {
			// aware 接口执行
			invokeAwareMethods(beanName, bean);
		}

		// 第二部分
		Object wrappedBean = bean;
		if (mbd == null || !mbd.isSynthetic()) {
			// BeanPostProcessor 前置方法执行
			wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
		}

		// 第三部分
		try {
			// 执行实例化函数
			invokeInitMethods(beanName, wrappedBean, mbd);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					(mbd != null ? mbd.getResourceDescription() : null),
					beanName, "Invocation of init method failed", ex
			);
		}

		// 第四部分
		if (mbd == null || !mbd.isSynthetic()) {
			// BeanPostProcessor 后置方法执行
			wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
		}

		return wrappedBean;
	}

	/**
	 * 执行 aware 接口
	 * @param beanName
	 * @param bean
	 */
	private void invokeAwareMethods(final String beanName, final Object bean) {
		if (bean instanceof Aware) {
			if (bean instanceof BeanNameAware) {
				((BeanNameAware) bean).setBeanName(beanName);
			}
			if (bean instanceof BeanClassLoaderAware) {
				ClassLoader bcl = getBeanClassLoader();
				if (bcl != null) {
					((BeanClassLoaderAware) bean).setBeanClassLoader(bcl);
				}
			}
			if (bean instanceof BeanFactoryAware) {
				((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
			}
		}
	}

	/**
	 * Give a bean a chance to react now all its properties are set, and a chance to know about its
	 * owning bean factory (this object). This means checking whether the bean implements
	 * InitializingBean or defines a custom init method, and invoking the necessary callback(s) if
	 * it does.
	 * 初始化方法执行
	 * @param beanName the bean name in the factory (for debugging purposes)
	 * @param bean     the new bean instance we may need to initialize
	 * @param mbd      the merged bean definition that the bean was created with (can also be {@code
	 *                 null}, if given an existing bean instance)
	 *
	 * @throws Throwable if thrown by init methods or by the invocation process
	 * @see #invokeCustomInitMethod
	 */
	protected void invokeInitMethods(String beanName, final Object bean, @Nullable RootBeanDefinition mbd)
			throws Throwable {


		// 第一部分
		// 是否是 InitializingBean
		boolean isInitializingBean = (bean instanceof InitializingBean);
		// 是否存在方法 "afterPropertiesSet"
		if (isInitializingBean && (mbd == null || !mbd.isExternallyManagedInitMethod("afterPropertiesSet"))) {
			if (logger.isTraceEnabled()) {
				logger.trace("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
			}
			if (System.getSecurityManager() != null) {
				try {
					// 执行 afterPropertiesSet
					AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
						((InitializingBean) bean).afterPropertiesSet();
						return null;
					}, getAccessControlContext());
				}
				catch (PrivilegedActionException pae) {
					throw pae.getException();
				}
			}
			else {
				// 执行 afterPropertiesSet
				((InitializingBean) bean).afterPropertiesSet();
			}
		}

		// 第二部分
		if (mbd != null && bean.getClass() != NullBean.class) {
			// 获取 initMethod 字符串
			String initMethodName = mbd.getInitMethodName();
			if (StringUtils.hasLength(initMethodName) &&
					!(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) &&
					!mbd.isExternallyManagedInitMethod(initMethodName)) {
				// 自定义的 init method
				invokeCustomInitMethod(beanName, bean, mbd);
			}
		}
	}

	/**
	 * Invoke the specified custom init method on the given bean. Called by invokeInitMethods.
	 * <p>Can be overridden in subclasses for custom resolution of init
	 * methods with arguments.
	 *
	 * 执行自定义的 initMethod 方法.
	 * @see #invokeInitMethods
	 */
	protected void invokeCustomInitMethod(String beanName, final Object bean, RootBeanDefinition mbd)
			throws Throwable {

		// 获取 initMethod 名称
		String initMethodName = mbd.getInitMethodName();
		Assert.state(initMethodName != null, "No init method set");
		// 反射获取方法
		Method initMethod = (mbd.isNonPublicAccessAllowed() ?
				BeanUtils.findMethod(bean.getClass(), initMethodName) :
				ClassUtils.getMethodIfAvailable(bean.getClass(), initMethodName));

		// 方法是否存在判断
		if (initMethod == null) {
			if (mbd.isEnforceInitMethod()) {
				throw new BeanDefinitionValidationException("Could not find an init method named '" +
						initMethodName + "' on bean with name '" + beanName + "'");
			}
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("No default init method named '" + initMethodName +
							"' found on bean with name '" + beanName + "'");
				}
				// Ignore non-existent default lifecycle methods.
				return;
			}
		}

		if (logger.isTraceEnabled()) {
			logger.trace("Invoking init method  '" + initMethodName + "' on bean with name '" + beanName + "'");
		}
		// 尝试获取接口方法
		Method methodToInvoke = ClassUtils.getInterfaceMethodIfPossible(initMethod);

		if (System.getSecurityManager() != null) {
			AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
				ReflectionUtils.makeAccessible(methodToInvoke);
				return null;
			});
			try {
				// 反射调用
				AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () ->
						methodToInvoke.invoke(bean), getAccessControlContext());
			}
			catch (PrivilegedActionException pae) {
				InvocationTargetException ex = (InvocationTargetException) pae.getException();
				throw ex.getTargetException();
			}
		}
		else {
			try {
				// 反射调用
				// setAccessible true
				ReflectionUtils.makeAccessible(methodToInvoke);
				methodToInvoke.invoke(bean);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}


	/**
	 * Applies the {@code postProcessAfterInitialization} callback of all registered
	 * BeanPostProcessors, giving them a chance to post-process the object obtained from
	 * FactoryBeans (for example, to auto-proxy them).
	 *从 FactoryBean 接口创建的 后置处理
	 * @see #applyBeanPostProcessorsAfterInitialization
	 */
	@Override
	protected Object postProcessObjectFromFactoryBean(Object object, String beanName) {
		return applyBeanPostProcessorsAfterInitialization(object, beanName);
	}

	/**
	 * Overridden to clear FactoryBean instance cache as well.
	 */
	@Override
	protected void removeSingleton(String beanName) {
		synchronized (getSingletonMutex()) {
			super.removeSingleton(beanName);
			this.factoryBeanInstanceCache.remove(beanName);
		}
	}

	/**
	 * Overridden to clear FactoryBean instance cache as well.
	 */
	@Override
	protected void clearSingletonCache() {
		synchronized (getSingletonMutex()) {
			super.clearSingletonCache();
			this.factoryBeanInstanceCache.clear();
		}
	}

	/**
	 * Expose the logger to collaborating delegates.
	 *
	 * @since 5.0.7
	 */
	Log getLogger() {
		return logger;
	}


	/**
	 * Special DependencyDescriptor variant for Spring's good old autowire="byType" mode. Always
	 * optional; never considering the parameter name for choosing a primary candidate.
	 */
	@SuppressWarnings("serial")
	private static class AutowireByTypeDependencyDescriptor extends DependencyDescriptor {

		public AutowireByTypeDependencyDescriptor(MethodParameter methodParameter, boolean eager) {
			super(methodParameter, false, eager);
		}

		@Override
		public String getDependencyName() {
			return null;
		}
	}


	/**
	 * {@link MethodCallback} used to find {@link FactoryBean} type information.
	 */
	private static class FactoryBeanMethodTypeFinder implements MethodCallback {

		/**
		 * 工厂方法(函数)名称
		 */
		private final String factoryMethodName;

		private ResolvableType result = ResolvableType.NONE;

		FactoryBeanMethodTypeFinder(String factoryMethodName) {
			this.factoryMethodName = factoryMethodName;
		}

		@Override
		public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
			// 是否是 FactoryBean
			if (isFactoryBeanMethod(method)) {
				// 获取 返回值 解析类型对象
				ResolvableType returnType = ResolvableType.forMethodReturnType(method);
				ResolvableType candidate = returnType.as(FactoryBean.class).getGeneric();
				if (this.result == ResolvableType.NONE) {
					this.result = candidate;
				}
				else {
					// result 解析得到类型
					Class<?> resolvedResult = this.result.resolve();
					// 获取 父类型
					Class<?> commonAncestor = ClassUtils.determineCommonAncestor(candidate.resolve(), resolvedResult);
					// 是否相同的验证
					if (!ObjectUtils.nullSafeEquals(resolvedResult, commonAncestor)) {
						// 解析结果
						this.result = ResolvableType.forClass(commonAncestor);
					}
				}
			}
		}

		private boolean isFactoryBeanMethod(Method method) {
			return (method.getName().equals(this.factoryMethodName) &&
					FactoryBean.class.isAssignableFrom(method.getReturnType()));
		}

		ResolvableType getResult() {
			Class<?> resolved = this.result.resolve();
			boolean foundResult = resolved != null && resolved != Object.class;
			return (foundResult ? this.result : ResolvableType.NONE);
		}
	}

}
