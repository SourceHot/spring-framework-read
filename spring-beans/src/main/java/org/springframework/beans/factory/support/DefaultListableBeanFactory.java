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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.inject.Provider;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.core.OrderComparator;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.core.log.LogMessage;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CompositeIterator;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Spring's default implementation of the {@link ConfigurableListableBeanFactory}
 * and {@link BeanDefinitionRegistry} interfaces: a full-fledged bean factory
 * based on bean definition metadata, extensible through post-processors.
 *
 * <p>Typical usage is registering all bean definitions first (possibly read
 * from a bean definition file), before accessing beans. Bean lookup by name
 * is therefore an inexpensive operation in a local bean definition table,
 * operating on pre-resolved bean definition metadata objects.
 *
 * <p>Note that readers for specific bean definition formats are typically
 * implemented separately rather than as bean factory subclasses:
 * see for example {@link PropertiesBeanDefinitionReader} and
 * {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}.
 *
 * <p>For an alternative implementation of the
 * {@link org.springframework.beans.factory.ListableBeanFactory} interface,
 * have a look at {@link StaticListableBeanFactory}, which manages existing
 * bean instances rather than creating new ones based on bean definitions.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Costin Leau
 * @author Chris Beams
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 16 April 2001
 * @see #registerBeanDefinition
 * @see #addBeanPostProcessor
 * @see #getBean
 * @see #resolveDependency
 */
@SuppressWarnings("serial")
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
		implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable {

	/**
	 * Map from serialized id to factory instance.
	 *
	 * key: 序列化id
	 *
	 * value: DefaultListableBeanFactory
	 *
	 * */
	private static final Map<String, Reference<DefaultListableBeanFactory>> serializableFactories =
			new ConcurrentHashMap<>(8);

	@Nullable
	private static Class<?> javaxInjectProviderClass;

	/**
	 * Map from dependency type to corresponding autowired value.
	 *
	 * key: 依赖类型
	 * value: 依赖类型对应的值
	 *
	 * */
	private final Map<Class<?>, Object> resolvableDependencies = new ConcurrentHashMap<>(16);

	/**
	 * Map of bean definition objects, keyed by bean name.
	 *
	 * bean定义容器
	 * key: beanName
	 * value: BeanDefinition
	 *
	 *  */
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

	/**
	 *  Map of singleton and non-singleton bean names, keyed by dependency type.
	 * 类型和别名的映射关系 单例和不是单例的都在
	 * key: 类型
	 * value: 别名
	 * */
	private final Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<>(64);

	/**
	 *  Map of singleton-only bean names, keyed by dependency type.
	 *  单例bean的别名映射
	 *
	 *  key: 类型
	 *  value: 别名
	 *
	 * */
	private final Map<Class<?>, String[]> singletonBeanNamesByType = new ConcurrentHashMap<>(64);

	/**
	 *  Optional id for this factory, for serialization purposes.
	 * 序列化 id (BeanFactory 的id)
	 * */
	@Nullable
	private String serializationId;

	/**
	 *  Whether to allow re-registration of a different definition with the same name.
	 * 是否允许名字不同但是bean定义相同
	 * */
	private boolean allowBeanDefinitionOverriding = true;

	/**
	 *  Whether to allow eager class loading even for lazy-init beans.
	 * 延迟加载的Bean是否立即加载
	 * */
	private boolean allowEagerClassLoading = true;

	/** Optional OrderComparator for dependency Lists and arrays. */
	@Nullable
	private Comparator<Object> dependencyComparator;

	/**
	 *  Resolver to use for checking if a bean definition is an autowire candidate.
	 *  用来进行 自动注入(自动装配)的解析类
	 * */
	private AutowireCandidateResolver autowireCandidateResolver = new SimpleAutowireCandidateResolver();

	/**
	 * List of bean definition names, in registration order.
	 * bean definition 名称列表
	 * */
	private volatile List<String> beanDefinitionNames = new ArrayList<>(256);

	/**
	 * List of names of manually registered singletons, in registration order.
	 * 按照注册顺序放入 单例的beanName , 手动注册的单例对象
	 *
	 * */
	private volatile Set<String> manualSingletonNames = new LinkedHashSet<>(16);

	/**
	 *
	 * Cached array of bean definition names in case of frozen configuration.
	 * 冻结的 bean definition 的名称列表
	 * */
	@Nullable
	private volatile String[] frozenBeanDefinitionNames;

	/**
	 *  Whether bean definition metadata may be cached for all beans.
	 * 是否需要给bean元数据进行缓存
	 * */
	private volatile boolean configurationFrozen = false;

	/**
	 * Create a new DefaultListableBeanFactory.
	 */
	public DefaultListableBeanFactory() {
		super();
	}


	/**
	 * Create a new DefaultListableBeanFactory with the given parent.
	 * @param parentBeanFactory the parent BeanFactory
	 */
	public DefaultListableBeanFactory(@Nullable BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}

	/**
	 * Return an id for serialization purposes, if specified, allowing this BeanFactory
	 * to be deserialized from this id back into the BeanFactory object, if needed.
	 * @since 4.1.2
	 */
	@Nullable
	public String getSerializationId() {
		return this.serializationId;
	}

	/**
	 * Specify an id for serialization purposes, allowing this BeanFactory to be
	 * deserialized from this id back into the BeanFactory object, if needed.
	 */
	public void setSerializationId(@Nullable String serializationId) {
		if (serializationId != null) {
			serializableFactories.put(serializationId, new WeakReference<>(this));
		}
		else if (this.serializationId != null) {
			serializableFactories.remove(this.serializationId);
		}
		this.serializationId = serializationId;
	}

	/**
	 * Return whether it should be allowed to override bean definitions by registering
	 * a different definition with the same name, automatically replacing the former.
	 * 是否允许beanName相同的bean定义注册,后注册的会覆盖前面注册的
	 * @since 4.1.2
	 */
	public boolean isAllowBeanDefinitionOverriding() {
		return this.allowBeanDefinitionOverriding;
	}

	/**
	 * Set whether it should be allowed to override bean definitions by registering
	 * a different definition with the same name, automatically replacing the former.
	 * If not, an exception will be thrown. This also applies to overriding aliases.
	 * <p>Default is "true".
	 * @see #registerBeanDefinition
	 */
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	/**
	 * Return whether the factory is allowed to eagerly load bean classes
	 * even for bean definitions that are marked as "lazy-init".
	 * @since 4.1.2
	 */
	public boolean isAllowEagerClassLoading() {
		return this.allowEagerClassLoading;
	}

	/**
	 * Set whether the factory is allowed to eagerly load bean classes
	 * even for bean definitions that are marked as "lazy-init".
	 * <p>Default is "true". Turn this flag off to suppress class loading
	 * for lazy-init beans unless such a bean is explicitly requested.
	 * In particular, by-type lookups will then simply ignore bean definitions
	 * without resolved class name, instead of loading the bean classes on
	 * demand just to perform a type check.
	 * @see AbstractBeanDefinition#setLazyInit
	 */
	public void setAllowEagerClassLoading(boolean allowEagerClassLoading) {
		this.allowEagerClassLoading = allowEagerClassLoading;
	}

	/**
	 * Return the dependency comparator for this BeanFactory (may be {@code null}.
	 * @since 4.0
	 */
	@Nullable
	public Comparator<Object> getDependencyComparator() {
		return this.dependencyComparator;
	}

	/**
	 * Set a {@link java.util.Comparator} for dependency Lists and arrays.
	 * @since 4.0
	 * @see org.springframework.core.OrderComparator
	 * @see org.springframework.core.annotation.AnnotationAwareOrderComparator
	 */
	public void setDependencyComparator(@Nullable Comparator<Object> dependencyComparator) {
		this.dependencyComparator = dependencyComparator;
	}

	/**
	 * Return the autowire candidate resolver for this BeanFactory (never {@code null}).
	 */
	public AutowireCandidateResolver getAutowireCandidateResolver() {
		return this.autowireCandidateResolver;
	}

	/**
	 * Set a custom autowire candidate resolver for this BeanFactory to use
	 * when deciding whether a bean definition should be considered as a
	 * candidate for autowiring.
	 */
	public void setAutowireCandidateResolver(final AutowireCandidateResolver autowireCandidateResolver) {
		Assert.notNull(autowireCandidateResolver, "AutowireCandidateResolver must not be null");
		if (autowireCandidateResolver instanceof BeanFactoryAware) {
			if (System.getSecurityManager() != null) {
				AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
					((BeanFactoryAware) autowireCandidateResolver).setBeanFactory(DefaultListableBeanFactory.this);
					return null;
				}, getAccessControlContext());
			}
			else {
				((BeanFactoryAware) autowireCandidateResolver).setBeanFactory(this);
			}
		}
		this.autowireCandidateResolver = autowireCandidateResolver;
	}

	@Override
	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		super.copyConfigurationFrom(otherFactory);
		if (otherFactory instanceof DefaultListableBeanFactory) {
			DefaultListableBeanFactory otherListableFactory = (DefaultListableBeanFactory) otherFactory;
			this.allowBeanDefinitionOverriding = otherListableFactory.allowBeanDefinitionOverriding;
			this.allowEagerClassLoading = otherListableFactory.allowEagerClassLoading;
			this.dependencyComparator = otherListableFactory.dependencyComparator;
			// A clone of the AutowireCandidateResolver since it is potentially BeanFactoryAware...
			setAutowireCandidateResolver(
					BeanUtils.instantiateClass(otherListableFactory.getAutowireCandidateResolver().getClass()));
			// Make resolvable dependencies (e.g. ResourceLoader) available here as well...
			this.resolvableDependencies.putAll(otherListableFactory.resolvableDependencies);
		}
	}

	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return getBean(requiredType, (Object[]) null);
	}


	//---------------------------------------------------------------------
	// Implementation of remaining BeanFactory methods
	//---------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBean(Class<T> requiredType, @Nullable Object... args) throws BeansException {
		Assert.notNull(requiredType, "Required type must not be null");
		// 解析bean
		Object resolved = resolveBean(ResolvableType.forRawClass(requiredType), args, false);
		if (resolved == null) {
			throw new NoSuchBeanDefinitionException(requiredType);
		}
		return (T) resolved;
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) throws BeansException {
		Assert.notNull(requiredType, "Required type must not be null");
		return getBeanProvider(ResolvableType.forRawClass(requiredType));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
		return new BeanObjectProvider<T>() {
			@Override
			public T getObject() throws BeansException {
				T resolved = resolveBean(requiredType, null, false);
				if (resolved == null) {
					throw new NoSuchBeanDefinitionException(requiredType);
				}
				return resolved;
			}

			@Override
			public T getObject(Object... args) throws BeansException {
				T resolved = resolveBean(requiredType, args, false);
				if (resolved == null) {
					throw new NoSuchBeanDefinitionException(requiredType);
				}
				return resolved;
			}

			@Override
			@Nullable
			public T getIfAvailable() throws BeansException {
				return resolveBean(requiredType, null, false);
			}

			@Override
			@Nullable
			public T getIfUnique() throws BeansException {
				return resolveBean(requiredType, null, true);
			}

			@Override
			public Stream<T> stream() {
				// 1. 根据 ResolvableType 提取所有的 beanName
				// 2. 获取bean
				// 3. 过滤 nullBean
				return Arrays.stream(getBeanNamesForTypedStream(requiredType))
						.map(name -> (T) getBean(name))
						.filter(bean -> !(bean instanceof NullBean));
			}

			@Override
			public Stream<T> orderedStream() {
				// 获取 beanName 列表
				String[] beanNames = getBeanNamesForTypedStream(requiredType);
				// key: beanName value: beanInstance
				Map<String, T> matchingBeans = new LinkedHashMap<>(beanNames.length);
				for (String beanName : beanNames) {
					Object beanInstance = getBean(beanName);
					if (!(beanInstance instanceof NullBean)) {
						matchingBeans.put(beanName, (T) beanInstance);
					}
				}
				Stream<T> stream = matchingBeans.values().stream();
				// 排序
				return stream.sorted(adaptOrderComparator(matchingBeans));
			}
		};
	}

	@Nullable
	private <T> T resolveBean(ResolvableType requiredType, @Nullable Object[] args, boolean nonUniqueAsNull) {
		// 第一部分
		// 解析 beanName
		NamedBeanHolder<T> namedBean = resolveNamedBean(requiredType, args, nonUniqueAsNull);
		if (namedBean != null) {
			// 获取实例
			return namedBean.getBeanInstance();
		}

		// 第二部分
		// 获取父工厂
		BeanFactory parent = getParentBeanFactory();
		if (parent instanceof DefaultListableBeanFactory) {
			// 父工厂的解析
			return ((DefaultListableBeanFactory) parent).resolveBean(requiredType, args, nonUniqueAsNull);
		}
		else if (parent != null) {
			// 延迟加载的情况下获取对象
			ObjectProvider<T> parentProvider = parent.getBeanProvider(requiredType);
			if (args != null) {
				return parentProvider.getObject(args);
			}
			else {
				return (nonUniqueAsNull ? parentProvider.getIfUnique() : parentProvider.getIfAvailable());
			}
		}
		return null;
	}

	/**
	 * 获取 beanName 列表
	 */
	private String[] getBeanNamesForTypedStream(ResolvableType requiredType) {
		return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, requiredType);
	}

	/**
	 * 判断是否存在 bean 定义
	 * @param beanName the name of the bean to look for
	 * @return
	 */
	@Override
	public boolean containsBeanDefinition(String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		// bean 定义容器中判断是否有 beanName 这个key
		return this.beanDefinitionMap.containsKey(beanName);
	}


	//---------------------------------------------------------------------
	// Implementation of ListableBeanFactory interface
	//---------------------------------------------------------------------

	@Override
	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	@Override
	public String[] getBeanDefinitionNames() {
		String[] frozenNames = this.frozenBeanDefinitionNames;
		if (frozenNames != null) {
			return frozenNames.clone();
		}
		else {
			return StringUtils.toStringArray(this.beanDefinitionNames);
		}
	}

	/**
	 * 根据类型找到beanNames
	 * @param type the generically typed class or interface to match
	 *
	 * @return
	 */
	@Override
	public String[] getBeanNamesForType(ResolvableType type) {
		return getBeanNamesForType(type, true, true);
	}

	/**
	 * 根据类型找到beanNames
	 * @return
	 */
	@Override
	public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		// 获取具体的类型
		Class<?> resolved = type.resolve();
		// 类型不为空 且存在泛型
		if (resolved != null && !type.hasGenerics()) {
			return getBeanNamesForType(resolved, includeNonSingletons, allowEagerInit);
		}
		else {
			return doGetBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		}
	}

	@Override
	public String[] getBeanNamesForType(@Nullable Class<?> type) {
		return getBeanNamesForType(type, true, true);
	}

	@Override
	public String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		// 参数验证 通过后做 doGetBeanNamesForType
		if (!isConfigurationFrozen() || type == null || !allowEagerInit) {
			return doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, allowEagerInit);
		}
		// 缓存容器的获取 , 根据是否包含非单例的条件来确定使用那个 beanClass -> name 容器
		Map<Class<?>, String[]> cache =
				(includeNonSingletons ? this.allBeanNamesByType : this.singletonBeanNamesByType);
		// 从容器中获取 类型对应的 beanName 列表
		String[] resolvedBeanNames = cache.get(type);
		// 不为空返回
		if (resolvedBeanNames != null) {
			return resolvedBeanNames;
		}
		// 继续尝试获取
		resolvedBeanNames = doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, true);
		if (ClassUtils.isCacheSafe(type, getBeanClassLoader())) {
			cache.put(type, resolvedBeanNames);
		}
		return resolvedBeanNames;
	}

	/**
	 *
	 * @param type
	 * @param includeNonSingletons 根据是否包含非单例
	 * @param allowEagerInit
	 * @return
	 */
	private String[] doGetBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		List<String> result = new ArrayList<>();


		// 第一部分
		// Check all bean definitions.
		// 循环所有的 beanName
		for (String beanName : this.beanDefinitionNames) {
			// Only consider bean as eligible if the bean name
			// is not defined as alias for some other bean.
			// 确定是否存在别名
			if (!isAlias(beanName)) {
				try {
					// 获取合并的 beanDefinition
					RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
					// Only check bean definition if it is complete.
					// bean 定义检查
					// 1. 是否 abstract 修饰
					// 2. beanClass 是否等于 Class
					// 3. 是否懒加载
					// 4. allowEagerClassLoading 是否true
					// 5. allowEagerInit 是否急切加载
					// 6. requiresEagerInitForType
					if (!mbd.isAbstract() && (allowEagerInit ||
							(mbd.hasBeanClass() || !mbd.isLazyInit() || isAllowEagerClassLoading()) &&
									!requiresEagerInitForType(mbd.getFactoryBeanName()))) {
						// 是否是 工厂bean
						boolean isFactoryBean = isFactoryBean(beanName, mbd);
						// bean 定义持有对象
						BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
						// 是否匹配
						boolean matchFound = false;
						// 是否允许 工厂bean 初始化
						boolean allowFactoryBeanInit = allowEagerInit || containsSingleton(beanName);
						// 是否懒加载
						boolean isNonLazyDecorated = dbd != null && !mbd.isLazyInit();
						if (!isFactoryBean) {
							if (includeNonSingletons || isSingleton(beanName, mbd, dbd)) {
								matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
							}
						}
						else {
							if (includeNonSingletons || isNonLazyDecorated ||
									(allowFactoryBeanInit && isSingleton(beanName, mbd, dbd))) {
								matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
							}
							if (!matchFound) {
								// In case of FactoryBean, try to match FactoryBean instance itself next.
								beanName = FACTORY_BEAN_PREFIX + beanName;
								matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
							}
						}
						if (matchFound) {
							result.add(beanName);
						}
					}
				}
				catch (CannotLoadBeanClassException | BeanDefinitionStoreException ex) {
					if (allowEagerInit) {
						throw ex;
					}
					// Probably a placeholder: let's ignore it for type matching purposes.
					LogMessage message = (ex instanceof CannotLoadBeanClassException) ?
							LogMessage.format("Ignoring bean class loading failure for bean '%s'", beanName) :
							LogMessage.format("Ignoring unresolvable metadata in bean definition '%s'", beanName);
					logger.trace(message, ex);
					onSuppressedException(ex);
				}
			}
		}


		// 第二部分
		// Check manually registered singletons too.
		for (String beanName : this.manualSingletonNames) {
			try {
				// In case of FactoryBean, match object created by FactoryBean.
				// 是否是 工厂 bean
				if (isFactoryBean(beanName)) {
					if ((includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type)) {
						result.add(beanName);
						// Match found for this bean: do not match FactoryBean itself anymore.
						continue;
					}
					// In case of FactoryBean, try to match FactoryBean itself next.
					// 如果是 工厂bean的情况下修正beanName
					beanName = FACTORY_BEAN_PREFIX + beanName;
				}
				// Match raw bean instance (might be raw FactoryBean).
				if (isTypeMatch(beanName, type)) {
					result.add(beanName);
				}
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Shouldn't happen - probably a result of circular reference resolution...
				logger.trace(LogMessage.format("Failed to check manually registered singleton with name '%s'", beanName), ex);
			}
		}

		return StringUtils.toStringArray(result);
	}

	private boolean isSingleton(String beanName, RootBeanDefinition mbd, @Nullable BeanDefinitionHolder dbd) {
		return (dbd != null ? mbd.isSingleton() : isSingleton(beanName));
	}

	/**
	 * Check whether the specified bean would need to be eagerly initialized
	 * in order to determine its type.
	 * @param factoryBeanName a factory-bean reference that the bean definition
	 * defines a factory method for
	 * @return whether eager initialization is necessary
	 */
	private boolean requiresEagerInitForType(@Nullable String factoryBeanName) {
		return (factoryBeanName != null && isFactoryBean(factoryBeanName) && !containsSingleton(factoryBeanName));
	}

	@Override
	public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
		return getBeansOfType(type, true, true);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {

		// 根据类型获取所有的beanName
		String[] beanNames = getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		// 返回结果
		Map<String, T> result = new LinkedHashMap<>(beanNames.length);
		// 循环beanName
		// 获取 bean 实例 放入返回结果.
		for (String beanName : beanNames) {
			try {
				Object beanInstance = getBean(beanName);
				if (!(beanInstance instanceof NullBean)) {
					result.put(beanName, (T) beanInstance);
				}
			}
			catch (BeanCreationException ex) {
				Throwable rootCause = ex.getMostSpecificCause();
				if (rootCause instanceof BeanCurrentlyInCreationException) {
					BeanCreationException bce = (BeanCreationException) rootCause;
					String exBeanName = bce.getBeanName();
					if (exBeanName != null && isCurrentlyInCreation(exBeanName)) {
						if (logger.isTraceEnabled()) {
							logger.trace("Ignoring match to currently created bean '" + exBeanName + "': " +
									ex.getMessage());
						}
						onSuppressedException(ex);
						// Ignore: indicates a circular reference when autowiring constructors.
						// We want to find matches other than the currently created bean itself.
						continue;
					}
				}
				throw ex;
			}
		}
		return result;
	}

	@Override
	public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
		// 返回结果
		List<String> result = new ArrayList<>();
		// bean 定义名称的列表
		for (String beanName : this.beanDefinitionNames) {
			// 获取 bean 定义
			BeanDefinition beanDefinition = getBeanDefinition(beanName);
			// 1. bean 定义的 abstract 判断
			// 2. 寻找注解是否存在
			if (!beanDefinition.isAbstract() && findAnnotationOnBean(beanName, annotationType) != null) {
				result.add(beanName);
			}
		}
		// 手动注入的 单例对象 beanName 的处理
		for (String beanName : this.manualSingletonNames) {
			// 1. result 中是否已包含 当前正在处理的 beanName
			// 2. 寻找注解是否存在
			if (!result.contains(beanName) && findAnnotationOnBean(beanName, annotationType) != null) {
				result.add(beanName);
			}
		}
		return StringUtils.toStringArray(result);
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
		// 通过注解类型寻找 beanName
		String[] beanNames = getBeanNamesForAnnotation(annotationType);
		// 结果对象
		Map<String, Object> result = new LinkedHashMap<>(beanNames.length);
		for (String beanName : beanNames) {
			// 获取 bean 实例
			Object beanInstance = getBean(beanName);
			if (!(beanInstance instanceof NullBean)) {
				result.put(beanName, beanInstance);
			}
		}
		return result;
	}

	@Override
	@Nullable
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException {

		return findMergedAnnotationOnBean(beanName, annotationType)
				.synthesize(MergedAnnotation::isPresent).orElse(null);
	}

	private <A extends Annotation> MergedAnnotation<A> findMergedAnnotationOnBean(
			String beanName, Class<A> annotationType) {

		// 获取 beanName 的类型
		Class<?> beanType = getType(beanName);
		if (beanType != null) {
			// 从 beanType 上获取注解的集合
			MergedAnnotation<A> annotation =
					MergedAnnotations.from(beanType, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
			if (annotation.isPresent()) {
				return annotation;
			}
		}
		// 是否存在 bean 定义
		if (containsBeanDefinition(beanName)) {
			// 获取 合并的bean定义
			RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
			// Check raw bean class, e.g. in case of a proxy.
			if (bd.hasBeanClass()) {
				Class<?> beanClass = bd.getBeanClass();
				if (beanClass != beanType) {
					MergedAnnotation<A> annotation =
							MergedAnnotations.from(beanClass, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
					if (annotation.isPresent()) {
						return annotation;
					}
				}
			}
			// Check annotations declared on factory method, if any.
			Method factoryMethod = bd.getResolvedFactoryMethod();
			if (factoryMethod != null) {
				MergedAnnotation<A> annotation =
						MergedAnnotations.from(factoryMethod, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
				if (annotation.isPresent()) {
					return annotation;
				}
			}
		}
		return MergedAnnotation.missing();
	}

	@Override
	public void registerResolvableDependency(Class<?> dependencyType, @Nullable Object autowiredValue) {
		Assert.notNull(dependencyType, "Dependency type must not be null");
		if (autowiredValue != null) {
			if (!(autowiredValue instanceof ObjectFactory || dependencyType.isInstance(autowiredValue))) {
				throw new IllegalArgumentException("Value [" + autowiredValue +
						"] does not implement specified dependency type [" + dependencyType.getName() + "]");
			}
			// 放入到依赖容器中
			this.resolvableDependencies.put(dependencyType, autowiredValue);
		}
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableListableBeanFactory interface
	//---------------------------------------------------------------------

	@Override
	public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
			throws NoSuchBeanDefinitionException {

		return isAutowireCandidate(beanName, descriptor, getAutowireCandidateResolver());
	}

	/**
	 * Determine whether the specified bean definition qualifies as an autowire candidate,
	 * to be injected into other beans which declare a dependency of matching type.
	 * 确定 beanName 是否符合候选条件
	 * @param beanName the name of the bean definition to check
	 * @param descriptor the descriptor of the dependency to resolve
	 * @param resolver the AutowireCandidateResolver to use for the actual resolution algorithm
	 * @return whether the bean should be considered as autowire candidate
	 */
	protected boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor, AutowireCandidateResolver resolver)
			throws NoSuchBeanDefinitionException {

		// 转换 beanName
		String beanDefinitionName = BeanFactoryUtils.transformedBeanName(beanName);
		// 是否存在 bean 定义
		if (containsBeanDefinition(beanDefinitionName)) {
			// 是否候选
			return isAutowireCandidate(beanName, getMergedLocalBeanDefinition(beanDefinitionName), descriptor, resolver);
		}
		// 是否是单例的
		else if (containsSingleton(beanName)) {
			// 是否候选
			return isAutowireCandidate(beanName, new RootBeanDefinition(getType(beanName)), descriptor, resolver);
		}

		// 父bean工厂执行 isAutowireCandidate
		BeanFactory parent = getParentBeanFactory();
		if (parent instanceof DefaultListableBeanFactory) {
			// No bean definition found in this factory -> delegate to parent.
			return ((DefaultListableBeanFactory) parent).isAutowireCandidate(beanName, descriptor, resolver);
		}
		else if (parent instanceof ConfigurableListableBeanFactory) {
			// If no DefaultListableBeanFactory, can't pass the resolver along.
			return ((ConfigurableListableBeanFactory) parent).isAutowireCandidate(beanName, descriptor);
		}
		else {
			return true;
		}
	}

	/**
	 * Determine whether the specified bean definition qualifies as an autowire candidate,
	 * to be injected into other beans which declare a dependency of matching type.
	 * @param beanName the name of the bean definition to check
	 * @param mbd the merged bean definition to check
	 * @param descriptor the descriptor of the dependency to resolve
	 * @param resolver the AutowireCandidateResolver to use for the actual resolution algorithm
	 * @return whether the bean should be considered as autowire candidate
	 */
	protected boolean isAutowireCandidate(String beanName, RootBeanDefinition mbd,
			DependencyDescriptor descriptor, AutowireCandidateResolver resolver) {

		// 转换 beanName
		String beanDefinitionName = BeanFactoryUtils.transformedBeanName(beanName);
		// 解析 bean class
		resolveBeanClass(mbd, beanDefinitionName);
		// 条件过滤
		// 1. 工厂方法是否唯一
		// 2. 工厂方法是否存在
		if (mbd.isFactoryMethodUnique && mbd.factoryMethodToIntrospect == null) {
			// ConstructorResolver 解析
			new ConstructorResolver(this).resolveFactoryMethodIfPossible(mbd);
		}
		// resolver 判断是否是候选注入对象
		return resolver.isAutowireCandidate(
				new BeanDefinitionHolder(mbd, beanName, getAliases(beanDefinitionName)), descriptor);
	}

	@Override
	public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		BeanDefinition bd = this.beanDefinitionMap.get(beanName);
		if (bd == null) {
			if (logger.isTraceEnabled()) {
				logger.trace("No bean named '" + beanName + "' found in " + this);
			}
			throw new NoSuchBeanDefinitionException(beanName);
		}
		return bd;
	}

	@Override
	public Iterator<String> getBeanNamesIterator() {
		CompositeIterator<String> iterator = new CompositeIterator<>();
		iterator.add(this.beanDefinitionNames.iterator());
		iterator.add(this.manualSingletonNames.iterator());
		return iterator;
	}

	@Override
	public void clearMetadataCache() {
		super.clearMetadataCache();
		clearByTypeCache();
	}

	@Override
	public void freezeConfiguration() {
		this.configurationFrozen = true;
		this.frozenBeanDefinitionNames = StringUtils.toStringArray(this.beanDefinitionNames);
	}

	@Override
	public boolean isConfigurationFrozen() {
		return this.configurationFrozen;
	}

	/**
	 * Considers all beans as eligible for metadata caching
	 * if the factory's configuration has been marked as frozen.
	 * @see #freezeConfiguration()
	 */
	@Override
	protected boolean isBeanEligibleForMetadataCaching(String beanName) {
		return (this.configurationFrozen || super.isBeanEligibleForMetadataCaching(beanName));
	}

	@Override
	public void preInstantiateSingletons() throws BeansException {
		if (logger.isTraceEnabled()) {
			logger.trace("Pre-instantiating singletons in " + this);
		}

		// Iterate over a copy to allow for init methods which in turn register new bean definitions.
		// While this may not be part of the regular factory bootstrap, it does otherwise work fine.
		// beanNames 内容是 beanDefinition 的名称
		List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

		// Trigger initialization of all non-lazy singleton beans...
		// 处理非懒加载的bean进行实例化
		for (String beanName : beanNames) {
			// 获取 bean定义
			RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
			// 条件过滤
			// 1. abstract 修饰
			// 2. 是否单例
			// 3. 是否懒加载
			if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
				// 是否是工厂bean
				if (isFactoryBean(beanName)) {
					// 获取 bean
					Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
					// 类型判断是否是 FactoryBean
					if (bean instanceof FactoryBean) {
						final FactoryBean<?> factory = (FactoryBean<?>) bean;
						// 是否立即加载
						boolean isEagerInit;
						// 计算 isEagerInit
						// 1. 是否是 SmartFactoryBean
						// 2. 执行SmartFactoryBean 的 isEagerInit 方法
						if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
							isEagerInit = AccessController.doPrivileged((PrivilegedAction<Boolean>)
											((SmartFactoryBean<?>) factory)::isEagerInit,
									getAccessControlContext());
						}
						else {
							isEagerInit = (factory instanceof SmartFactoryBean &&
									((SmartFactoryBean<?>) factory).isEagerInit());
						}
						if (isEagerInit) {
							getBean(beanName);
						}
					}
				}
				else {
					getBean(beanName);
				}
			}
		}

		// Trigger post-initialization callback for all applicable beans...
		// 触发回调方法
		for (String beanName : beanNames) {
			// 获取单例对象的实例
			Object singletonInstance = getSingleton(beanName);
			// 类型判断
			if (singletonInstance instanceof SmartInitializingSingleton) {
				// 执行 SmartInitializingSingleton 方法 afterSingletonsInstantiated
				final SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
				if (System.getSecurityManager() != null) {
					AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
						smartSingleton.afterSingletonsInstantiated();
						return null;
					}, getAccessControlContext());
				}
				else {
					smartSingleton.afterSingletonsInstantiated();
				}
			}
		}
	}

	@Override
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {

		Assert.hasText(beanName, "Bean name must not be empty");
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");

		// 第一部分
		// 类型验证
		if (beanDefinition instanceof AbstractBeanDefinition) {
			try {
				// bean定义验证
				((AbstractBeanDefinition) beanDefinition).validate();
			}
			catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
						"Validation of bean definition failed", ex);
			}
		}

		// 第二部分 beanName 存在 BeanDefinition 的情况
		// 从 map 中根据 beanName 获取 beanDefinition
		BeanDefinition existingDefinition = this.beanDefinitionMap.get(beanName);
		if (existingDefinition != null) {
			// bean name 是否允许重复注册
			if (!isAllowBeanDefinitionOverriding()) {
				throw new BeanDefinitionOverrideException(beanName, beanDefinition, existingDefinition);
			}
			// role 值比较
			else if (existingDefinition.getRole() < beanDefinition.getRole()) {
				// e.g. was ROLE_APPLICATION, now overriding with ROLE_SUPPORT or ROLE_INFRASTRUCTURE
				if (logger.isInfoEnabled()) {
					logger.info("Overriding user-defined bean definition for bean '" + beanName +
							"' with a framework-generated bean definition: replacing [" +
							existingDefinition + "] with [" + beanDefinition + "]");
				}
			}
			// map 中存储的 beanDefinition 是否和参数相同
			else if (!beanDefinition.equals(existingDefinition)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Overriding bean definition for bean '" + beanName +
							"' with a different definition: replacing [" + existingDefinition +
							"] with [" + beanDefinition + "]");
				}
			}
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("Overriding bean definition for bean '" + beanName +
							"' with an equivalent definition: replacing [" + existingDefinition +
							"] with [" + beanDefinition + "]");
				}
			}
			// 设置 beanName 和 beanDefinition 关系
			this.beanDefinitionMap.put(beanName, beanDefinition);
		}


		// 第三部分 bean 不存在 beanDefinition 的情况
		else {
			// 检查 bean 是否已经开始创建
			if (hasBeanCreationStarted()) {
				// Cannot modify startup-time collection elements anymore (for stable iteration)
				synchronized (this.beanDefinitionMap) {
					// 设置 beanName 和 beanDefinition 关系
					this.beanDefinitionMap.put(beanName, beanDefinition);
					// bean definition 的名称列表
					List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames.size() + 1);
					// 加入内存数据
					updatedDefinitions.addAll(this.beanDefinitionNames);
					// 加入当前的 beanName
					updatedDefinitions.add(beanName);
					// 对象替换
					this.beanDefinitionNames = updatedDefinitions;
					// 移除当前的beanName
					removeManualSingletonName(beanName);
				}
			}
			else {
				// Still in startup registration phase
				// 设置容器数据
				this.beanDefinitionMap.put(beanName, beanDefinition);
				this.beanDefinitionNames.add(beanName);
				// 移除当前的beanName
				removeManualSingletonName(beanName);
			}
			this.frozenBeanDefinitionNames = null;
		}


		// 第四部分
		if (existingDefinition != null || containsSingleton(beanName)) {
			// 刷新bean definition
			resetBeanDefinition(beanName);
		}
	}


	//---------------------------------------------------------------------
	// Implementation of BeanDefinitionRegistry interface
	//---------------------------------------------------------------------

	@Override
	public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		Assert.hasText(beanName, "'beanName' must not be empty");

		// 删除 beanName key
		BeanDefinition bd = this.beanDefinitionMap.remove(beanName);
		if (bd == null) {
			if (logger.isTraceEnabled()) {
				logger.trace("No bean named '" + beanName + "' found in " + this);
			}
			throw new NoSuchBeanDefinitionException(beanName);
		}

		if (hasBeanCreationStarted()) {
			// Cannot modify startup-time collection elements anymore (for stable iteration)
			synchronized (this.beanDefinitionMap) {
				List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames);
				// 删除需要移除的BeanName
				updatedDefinitions.remove(beanName);
				// 重新赋值
				this.beanDefinitionNames = updatedDefinitions;
			}
		}
		else {
			// Still in startup registration phase
			// 移除 beanName
			this.beanDefinitionNames.remove(beanName);
		}
		this.frozenBeanDefinitionNames = null;

		// 刷新bean definition
		resetBeanDefinition(beanName);
	}

	/**
	 * Reset all bean definition caches for the given bean,
	 * including the caches of beans that are derived from it.
	 * <p>Called after an existing bean definition has been replaced or removed,
	 * triggering {@link #clearMergedBeanDefinition}, {@link #destroySingleton}
	 * and {@link MergedBeanDefinitionPostProcessor#resetBeanDefinition} on the
	 * given bean and on all bean definitions that have the given bean as parent.
	 * @param beanName the name of the bean to reset
	 * @see #registerBeanDefinition
	 * @see #removeBeanDefinition
	 */
	protected void resetBeanDefinition(String beanName) {
		// Remove the merged bean definition for the given bean, if already created.
		// 清空合并的BeanDefinition
		clearMergedBeanDefinition(beanName);

		// Remove corresponding bean from singleton cache, if any. Shouldn't usually
		// be necessary, rather just meant for overriding a context's default beans
		// (e.g. the default StaticMessageSource in a StaticApplicationContext).
		// 摧毁单例bean
		destroySingleton(beanName);

		// Notify all post-processors that the specified bean definition has been reset.
		// 后置处理器执行 resetBeanDefinition 方法
		for (BeanPostProcessor processor : getBeanPostProcessors()) {
			if (processor instanceof MergedBeanDefinitionPostProcessor) {
				// 执行后置方法的 resetBeanDefinition
				((MergedBeanDefinitionPostProcessor) processor).resetBeanDefinition(beanName);
			}
		}

		// Reset all bean definitions that have the given bean as parent (recursively).
		// 处理其他的 bean name
		for (String bdName : this.beanDefinitionNames) {
			if (!beanName.equals(bdName)) {
				BeanDefinition bd = this.beanDefinitionMap.get(bdName);
				// Ensure bd is non-null due to potential concurrent modification
				// of the beanDefinitionMap.
				// beanName 等于 父名称
				if (bd != null && beanName.equals(bd.getParentName())) {
					// 递归刷新 beanName 对应的 beanDefinition
					resetBeanDefinition(bdName);
				}
			}
		}
	}

	/**
	 * Only allows alias overriding if bean definition overriding is allowed.
	 */
	@Override
	protected boolean allowAliasOverriding() {
		return isAllowBeanDefinitionOverriding();
	}

	@Override
	public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
		super.registerSingleton(beanName, singletonObject);
		updateManualSingletonNames(set -> set.add(beanName), set -> !this.beanDefinitionMap.containsKey(beanName));
		clearByTypeCache();
	}

	/**
	 * org.springframework.beans.factory.support.DefaultListableBeanFactory#destroySingletons()
	 */
	@Override
	public void destroySingletons() {
		// 父类的摧毁方法
		super.destroySingletons();
		// 更新单例bean容器
		updateManualSingletonNames(Set::clear, set -> !set.isEmpty());
		// 清理类型缓存
		clearByTypeCache();
	}

	/**
	 * 摧毁单例
	 * @param beanName the name of the bean
	 */
	@Override
	public void destroySingleton(String beanName) {
		// 摧毁方法的调用
		super.destroySingleton(beanName);
		// 删除 manualSingletonNames 中的BeanName
		removeManualSingletonName(beanName);
		//allBeanNamesByType clean
		//singletonBeanNamesByType clean
		clearByTypeCache();
	}

	private void removeManualSingletonName(String beanName) {
		updateManualSingletonNames(set -> set.remove(beanName), set -> set.contains(beanName));
	}

	/**
	 * Update the factory's internal set of manual singleton names.
	 * @param action the modification action
	 * @param condition a precondition for the modification action
	 * (if this condition does not apply, the action can be skipped)
	 */
	private void updateManualSingletonNames(Consumer<Set<String>> action, Predicate<Set<String>> condition) {
		if (hasBeanCreationStarted()) {
			// Cannot modify startup-time collection elements anymore (for stable iteration)
			synchronized (this.beanDefinitionMap) {
				// 输入的 beanName 是否在 manualSingletonNames 存在
				if (condition.test(this.manualSingletonNames)) {
					// 数据重写
					Set<String> updatedSingletons = new LinkedHashSet<>(this.manualSingletonNames);
					// 删除 beanName
					action.accept(updatedSingletons);
					// 数据重写
					this.manualSingletonNames = updatedSingletons;
				}
			}
		}
		else {
			// Still in startup registration phase
			// 输入的 beanName 是否在 manualSingletonNames 存在
			if (condition.test(this.manualSingletonNames)) {
				// 删除 beanName
				action.accept(this.manualSingletonNames);
			}
		}
	}

	/**
	 * Remove any assumptions about by-type mappings.
	 */
	private void clearByTypeCache() {
		this.allBeanNamesByType.clear();
		this.singletonBeanNamesByType.clear();
	}

	@Override
	public <T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException {
		Assert.notNull(requiredType, "Required type must not be null");
		NamedBeanHolder<T> namedBean = resolveNamedBean(ResolvableType.forRawClass(requiredType), null, false);
		if (namedBean != null) {
			return namedBean;
		}
		BeanFactory parent = getParentBeanFactory();
		if (parent instanceof AutowireCapableBeanFactory) {
			return ((AutowireCapableBeanFactory) parent).resolveNamedBean(requiredType);
		}
		throw new NoSuchBeanDefinitionException(requiredType);
	}


	//---------------------------------------------------------------------
	// Dependency resolution functionality
	//---------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	@Nullable
	private <T> NamedBeanHolder<T> resolveNamedBean(
			ResolvableType requiredType, @Nullable Object[] args, boolean nonUniqueAsNull) throws BeansException {

		Assert.notNull(requiredType, "Required type must not be null");
		// 第一部分
		// 按照类型找到beanName
		String[] candidateNames = getBeanNamesForType(requiredType);

		// 第二部分
		if (candidateNames.length > 1) {
			List<String> autowireCandidates = new ArrayList<>(candidateNames.length);
			for (String beanName : candidateNames) {
				// 1. 判断是否存在 bean 定义
				// 2. 获取 bean 定义的autowireCandidate属性
				if (!containsBeanDefinition(beanName) || getBeanDefinition(beanName).isAutowireCandidate()) {
					autowireCandidates.add(beanName);
				}
			}
			if (!autowireCandidates.isEmpty()) {
				candidateNames = StringUtils.toStringArray(autowireCandidates);
			}
		}

		// 第三部分
		if (candidateNames.length == 1) {
			String beanName = candidateNames[0];
			return new NamedBeanHolder<>(beanName, (T) getBean(beanName, requiredType.toClass(), args));
		}
		// 第四部分
		else if (candidateNames.length > 1) {
			// key: beanName value: beanInstance或者 class
			Map<String, Object> candidates = new LinkedHashMap<>(candidateNames.length);
			// 循环 beanName 获取bean实例
			for (String beanName : candidateNames) {
				// 判断是否单例 和构造函数参是是否为空
				if (containsSingleton(beanName) && args == null) {
					// 获取bean实例
					Object beanInstance = getBean(beanName);
					candidates.put(beanName, (beanInstance instanceof NullBean ? null : beanInstance));
				}
				else {
					candidates.put(beanName, getType(beanName));
				}
			}
			// 确定候选对象名称
			String candidateName = determinePrimaryCandidate(candidates, requiredType.toClass());
			if (candidateName == null) {
				// 确定优先级最高的候选对象名称
				candidateName = determineHighestPriorityCandidate(candidates, requiredType.toClass());
			}
			if (candidateName != null) {
				// 容器中获取 候选对象名称的值
				Object beanInstance = candidates.get(candidateName);
				if (beanInstance == null || beanInstance instanceof Class) {
					// 根据类型获取bean实例
					beanInstance = getBean(candidateName, requiredType.toClass(), args);
				}
				return new NamedBeanHolder<>(candidateName, (T) beanInstance);
			}
			if (!nonUniqueAsNull) {
				throw new NoUniqueBeanDefinitionException(requiredType, candidates.keySet());
			}
		}

		return null;
	}

	/**
	 * 解析得到依赖对象
	 * @param descriptor the descriptor for the dependency (field/method/constructor)
	 * @param requestingBeanName the name of the bean which declares the given dependency
	 * @param autowiredBeanNames a Set that all names of autowired beans (used for
	 * resolving the given dependency) are supposed to be added to
	 * @param typeConverter the TypeConverter to use for populating arrays and collections
	 * @return
	 * @throws BeansException
	 */
	@Override
	@Nullable
	public Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {

		descriptor.initParameterNameDiscovery(getParameterNameDiscoverer());
		if (Optional.class == descriptor.getDependencyType()) {
			// 创建 Optional 类型的对象
			return createOptionalDependency(descriptor, requestingBeanName);
		}
		else if (ObjectFactory.class == descriptor.getDependencyType() ||
				ObjectProvider.class == descriptor.getDependencyType()) {
			// 类型全部符合的情况下直接创建 DependencyObjectProvider
			return new DependencyObjectProvider(descriptor, requestingBeanName);
		}
		else if (javaxInjectProviderClass == descriptor.getDependencyType()) {
			// jsr 330 创建对象
			return new Jsr330Factory().createDependencyProvider(descriptor, requestingBeanName);
		}
		else {
			// 获取延迟加载对象
			Object result = getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary(
					descriptor, requestingBeanName);
			if (result == null) {
				// 解析依赖
				result = doResolveDependency(descriptor, requestingBeanName, autowiredBeanNames, typeConverter);
			}
			return result;
		}
	}

	@Nullable
	public Object doResolveDependency(DependencyDescriptor descriptor, @Nullable String beanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {

		// 设置线程变量 DependencyDescriptor
		InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
		try {
			// 第一部分
			// 解析 实际方式 beanFactory.getBean
			Object shortcut = descriptor.resolveShortcut(this);
			if (shortcut != null) {
				return shortcut;
			}


			// 第二部分
			// 获取依赖类型
			Class<?> type = descriptor.getDependencyType();
			// AutowireCandidateResolver 方法获取数据
			Object value = getAutowireCandidateResolver().getSuggestedValue(descriptor);
			if (value != null) {
				// el 表达式相关处理
				// 类型是否是 string
				if (value instanceof String) {
					// 解析字符串.
					String strVal = resolveEmbeddedValue((String) value);
					BeanDefinition bd = (beanName != null && containsBean(beanName) ?
							getMergedBeanDefinition(beanName) : null);
					// beanName , beanDefinition
					// el 表达式解析
					value = evaluateBeanDefinitionString(strVal, bd);
				}
				// 类型转换器
				TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
				try {
					// 转换返回
					return converter.convertIfNecessary(value, type, descriptor.getTypeDescriptor());
				}
				catch (UnsupportedOperationException ex) {
					// A custom TypeConverter which does not support TypeDescriptor resolution...
					// 类型转换
					// 1. field 转换
					// 2. method parameter 转换
					return (descriptor.getField() != null ?
							converter.convertIfNecessary(value, type, descriptor.getField()) :
							converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
				}
			}

			// 第三部分
			// 多个依赖的处理
			Object multipleBeans = resolveMultipleBeans(descriptor, beanName, autowiredBeanNames, typeConverter);
			if (multipleBeans != null) {
				return multipleBeans;
			}



			// 第四部分
			// 可能匹配的bean
			// key: beanName
			// value: beanInstance
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
			if (matchingBeans.isEmpty()) {
				if (isRequired(descriptor)) {
					// 尝试触发两个异常
					// NoSuchBeanDefinitionException
					// BeanNotOfRequiredTypeException
					raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
				}
				return null;
			}

			// 第五部分
			// 自动注入的 beanName
			String autowiredBeanName;
			// 自动注入的候选对象
			Object instanceCandidate;

			// 5.1)
			// beanInstance 数量大于1的情况下
			if (matchingBeans.size() > 1) {
				// 确定 自动注入的beanName
				autowiredBeanName = determineAutowireCandidate(matchingBeans, descriptor);
				if (autowiredBeanName == null) {
					// 1. 是否必须
					// 2. 是否多个bean(接口 array collection map 类型标识多个)
					if (isRequired(descriptor) || !indicatesMultipleBeans(type)) {
						// 依赖描述符解析
						// 两种可能
						// 1. 出现异常 NoUniqueBeanDefinitionException beanDefinition 不唯一
						// 2. 出现 null
						return descriptor.resolveNotUnique(descriptor.getResolvableType(), matchingBeans);
					}
					else {
						// In case of an optional Collection/Map, silently ignore a non-unique case:
						// possibly it was meant to be an empty collection of multiple regular beans
						// (before 4.3 in particular when we didn't even look for collection beans).
						return null;
					}
				}
				// 匹配容器中获取一个作为 候选实例
				instanceCandidate = matchingBeans.get(autowiredBeanName);
			}
			// 5.2)
			// 只有一个 beanInstance 的情况下处理
			else {
				// We have exactly one match.
				Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
				// 设置自动注入的beanName
				autowiredBeanName = entry.getKey();
				// 设置 自动注入的候选对象
				instanceCandidate = entry.getValue();
			}

			if (autowiredBeanNames != null) {
				// 自动注入的 beanName 列表中添加当前候选beanName
				autowiredBeanNames.add(autowiredBeanName);
			}
			// 类型是 class 的情况下
			if (instanceCandidate instanceof Class) {
				// 解析候选人核心方法   beanFactory.getBean 注意 这里多实现不同参数会有少许不同
				instanceCandidate = descriptor.resolveCandidate(autowiredBeanName, type, this);
			}


			// 第六部分
			// 返回值相关处理
			Object result = instanceCandidate;
			// 类型是否 NullBean
			if (result instanceof NullBean) {
				// 是否唯一
				if (isRequired(descriptor)) {
					// 尝试触发两个异常
					// NoSuchBeanDefinitionException
					// BeanNotOfRequiredTypeException
					raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
				}
				result = null;
			}
			// 如果 result 不属于 type 的子类 抛出异常 BeanNotOfRequiredTypeException
			if (!ClassUtils.isAssignableValue(type, result)) {
				throw new BeanNotOfRequiredTypeException(autowiredBeanName, type, instanceCandidate.getClass());
			}
			return result;
		}
		finally {
			// 线程变量设置
			ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
		}
	}

	@Nullable
	private Object resolveMultipleBeans(DependencyDescriptor descriptor, @Nullable String beanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) {

		// 获取依赖类型
		final Class<?> type = descriptor.getDependencyType();

		// 多个类型的不同操作
		// 是否是  StreamDependencyDescriptor
		if (descriptor instanceof StreamDependencyDescriptor) {
			// beanName + type + 依赖描述 找到可能的beanMap
			// key: beanName
			// value: beanInstance
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			// 获取候选的beanName 求对象实例, 过滤 NullBean
			Stream<Object> stream = matchingBeans.keySet().stream()
					.map(name -> descriptor.resolveCandidate(name, type, this))
					.filter(bean -> !(bean instanceof NullBean));
			if (((StreamDependencyDescriptor) descriptor).isOrdered()) {
				stream = stream.sorted(adaptOrderComparator(matchingBeans));
			}
			return stream;
		}
		// array 的操作
		else if (type.isArray()) {
			// 类的通用类型
			// class.getComponentType()
			Class<?> componentType = type.getComponentType();
			// 依赖描述的类型解析类
			ResolvableType resolvableType = descriptor.getResolvableType();
			// array 中的类型获取
			Class<?> resolvedArrayType = resolvableType.resolve(type);
			if (resolvedArrayType != type) {
				// componentType 重新赋值
				componentType = resolvableType.getComponentType().resolve();
			}
			if (componentType == null) {
				return null;
			}
			// beanName + type + 依赖描述 找到可能的beanMap
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, componentType,
					new MultiElementDescriptor(descriptor));
			if (matchingBeans.isEmpty()) {
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			// 类型转换器
			TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
			// 类型转换
			Object result = converter.convertIfNecessary(matchingBeans.values(), resolvedArrayType);
			if (result instanceof Object[]) {
				// 比较器
				Comparator<Object> comparator = adaptDependencyComparator(matchingBeans);
				if (comparator != null) {
					// 排序
					Arrays.sort((Object[]) result, comparator);
				}
			}
			return result;
		}

		// collection 的操作 + 是否是 接口
		else if (Collection.class.isAssignableFrom(type) && type.isInterface()) {
			// 元素类型
			Class<?> elementType = descriptor.getResolvableType().asCollection().resolveGeneric();
			if (elementType == null) {
				return null;
			}
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, elementType,
					new MultiElementDescriptor(descriptor));
			if (matchingBeans.isEmpty()) {
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			// 类型转换器
			TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
			// 类型转换
			Object result = converter.convertIfNecessary(matchingBeans.values(), type);
			if (result instanceof List) {
				// 比较器
				Comparator<Object> comparator = adaptDependencyComparator(matchingBeans);
				if (comparator != null) {
					// 排序
					((List<?>) result).sort(comparator);
				}
			}
			return result;
		}

		// map 的操作
		else if (Map.class == type) {
			ResolvableType mapType = descriptor.getResolvableType().asMap();
			Class<?> keyType = mapType.resolveGeneric(0);
			if (String.class != keyType) {
				return null;
			}
			Class<?> valueType = mapType.resolveGeneric(1);
			if (valueType == null) {
				return null;
			}
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, valueType,
					new MultiElementDescriptor(descriptor));
			if (matchingBeans.isEmpty()) {
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			return matchingBeans;
		}
		else {
			return null;
		}
	}

	private boolean isRequired(DependencyDescriptor descriptor) {
		return getAutowireCandidateResolver().isRequired(descriptor);
	}

	private boolean indicatesMultipleBeans(Class<?> type) {
		return (type.isArray() || (type.isInterface() &&
				(Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type))));
	}

	@Nullable
	private Comparator<Object> adaptDependencyComparator(Map<String, ?> matchingBeans) {
		Comparator<Object> comparator = getDependencyComparator();
		if (comparator instanceof OrderComparator) {
			return ((OrderComparator) comparator).withSourceProvider(
					createFactoryAwareOrderSourceProvider(matchingBeans));
		}
		else {
			return comparator;
		}
	}

	private Comparator<Object> adaptOrderComparator(Map<String, ?> matchingBeans) {
		// 获取依赖比较对象
		Comparator<Object> dependencyComparator = getDependencyComparator();
		// 转换 OrderComparator
		OrderComparator comparator = (dependencyComparator instanceof OrderComparator ?
				(OrderComparator) dependencyComparator : OrderComparator.INSTANCE);
		return comparator.withSourceProvider(createFactoryAwareOrderSourceProvider(matchingBeans));
	}

	private OrderComparator.OrderSourceProvider createFactoryAwareOrderSourceProvider(Map<String, ?> beans) {
		IdentityHashMap<Object, String> instancesToBeanNames = new IdentityHashMap<>();
		beans.forEach((beanName, instance) -> instancesToBeanNames.put(instance, beanName));
		return new FactoryAwareOrderSourceProvider(instancesToBeanNames);
	}

	/**
	 * Find bean instances that match the required type.
	 * Called during autowiring for the specified bean.
	 *
	 * beanName + type + 依赖描述 找到可能的beanMap
	 * @param beanName the name of the bean that is about to be wired
	 * @param requiredType the actual type of bean to look for
	 * (may be an array component type or collection element type)
	 * @param descriptor the descriptor of the dependency to resolve
	 * @return a Map of candidate names and candidate instances that match
	 * the required type (never {@code null})
	 * @throws BeansException in case of errors
	 * @see #autowireByType
	 * @see #autowireConstructor
	 */
	protected Map<String, Object> findAutowireCandidates(
			@Nullable String beanName, Class<?> requiredType, DependencyDescriptor descriptor) {

		// 找到候选的 beanName 列表
		String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
				this, requiredType, true, descriptor.isEager());

		// 结果对象
		Map<String, Object> result = new LinkedHashMap<>(candidateNames.length);

		// 针对 resolvableDependencies 的处理
		for (Map.Entry<Class<?>, Object> classObjectEntry : this.resolvableDependencies.entrySet()) {
			// 依赖对象集合中的 key
			Class<?> autowiringType = classObjectEntry.getKey();
			// 容器中的类型 是否来自 参数 requiredType
			if (autowiringType.isAssignableFrom(requiredType)) {
				// 容器中获取
				Object autowiringValue = classObjectEntry.getValue();
				// 工具类获取
				// 1. ObjectFactory 获取
				// 2. 代理创建
				autowiringValue = AutowireUtils.resolveAutowiringValue(autowiringValue, requiredType);
				// 判断是否是实现类
				if (requiredType.isInstance(autowiringValue)) {
					// 放入结果对象
					result.put(ObjectUtils.identityToString(autowiringValue), autowiringValue);
					break;
				}
			}
		}
		// 候选beanName 循环
		for (String candidate : candidateNames) {
			// 1. 是否自引用
			// 2. 是否是候选值
			if (!isSelfReference(beanName, candidate) && isAutowireCandidate(candidate, descriptor)) {
				// 加入result
				addCandidateEntry(result, candidate, descriptor, requiredType);
			}
		}

		// result 为空的情况下
		if (result.isEmpty()) {
			// 判断 参数 requiredType 到底是不是 多个bean的
			boolean multiple = indicatesMultipleBeans(requiredType);
			// Consider fallback matches if the first pass failed to find anything...
			// 创建 DependencyDescriptor . fallbackMatchAllowed 恒等 true
			DependencyDescriptor fallbackDescriptor = descriptor.forFallbackMatch();
			// 循环候选bean名称列表
			for (String candidate : candidateNames) {
				// 1. 是否子引用
				// 2. 是否是候选值
				// 3. multiple
				// 4. qualifier 判断是否拥有这个依赖
				if (!isSelfReference(beanName, candidate) && isAutowireCandidate(candidate, fallbackDescriptor) &&
						(!multiple || getAutowireCandidateResolver().hasQualifier(descriptor))) {
					// 加入result
					addCandidateEntry(result, candidate, descriptor, requiredType);
				}
			}
			if (result.isEmpty() && !multiple) {
				// Consider self references as a final pass...
				// but in the case of a dependency collection, not the very same bean itself.
				for (String candidate : candidateNames) {
					if (isSelfReference(beanName, candidate) &&
							(!(descriptor instanceof MultiElementDescriptor) || !beanName.equals(candidate)) &&
							isAutowireCandidate(candidate, fallbackDescriptor)) {
						// 加入result
						addCandidateEntry(result, candidate, descriptor, requiredType);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Add an entry to the candidate map: a bean instance if available or just the resolved
	 * type, preventing early bean initialization ahead of primary candidate selection.
	 */
	private void addCandidateEntry(Map<String, Object> candidates, String candidateName,
			DependencyDescriptor descriptor, Class<?> requiredType) {

		// 类型是否是 MultiElementDescriptor
		if (descriptor instanceof MultiElementDescriptor) {
			// beanFactory.getBean
			Object beanInstance = descriptor.resolveCandidate(candidateName, requiredType, this);
			// 是否是 NullBean
			if (!(beanInstance instanceof NullBean)) {
				candidates.put(candidateName, beanInstance);
			}
		}
		// 1. 是否单例
		// 2. 类型是否是 StreamDependencyDescriptor
		// 3. 是 StreamDependencyDescriptor 的情况下 ordered 是否为 true
		else if (containsSingleton(candidateName) || (descriptor instanceof StreamDependencyDescriptor &&
				((StreamDependencyDescriptor) descriptor).isOrdered())) {
			// beanFactory.getBean
			Object beanInstance = descriptor.resolveCandidate(candidateName, requiredType, this);
			candidates.put(candidateName, (beanInstance instanceof NullBean ? null : beanInstance));
		}
		else {
			candidates.put(candidateName, getType(candidateName));
		}
	}

	/**
	 * Determine the autowire candidate in the given set of beans.
	 * <p>Looks for {@code @Primary} and {@code @Priority} (in that order).
	 *
	 * 确定候选名称
	 * @param candidates a Map of candidate names and candidate instances
	 * that match the required type, as returned by {@link #findAutowireCandidates}
	 * @param descriptor the target dependency to match against
	 * @return the name of the autowire candidate, or {@code null} if none found
	 */
	@Nullable
	protected String determineAutowireCandidate(Map<String, Object> candidates, DependencyDescriptor descriptor) {
		// 依赖类型
		Class<?> requiredType = descriptor.getDependencyType();
		// 确定候选beanName
		String primaryCandidate = determinePrimaryCandidate(candidates, requiredType);
		// 不为空返回
		if (primaryCandidate != null) {
			return primaryCandidate;
		}
		// 确定优先级最高的候选对象名称
		String priorityCandidate = determineHighestPriorityCandidate(candidates, requiredType);
		// 不为空返回
		if (priorityCandidate != null) {
			return priorityCandidate;
		}
		// Fallback
		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			String candidateName = entry.getKey();
			Object beanInstance = entry.getValue();
			// bean 实例不为空
			// 解析的依赖容器中是否包含 bean 实例
			// bean name 判断是否相同
			if ((beanInstance != null && this.resolvableDependencies.containsValue(beanInstance)) ||
					matchesBeanName(candidateName, descriptor.getDependencyName())) {
				return candidateName;
			}
		}
		return null;
	}

	/**
	 * Determine the primary candidate in the given set of beans.
	 * 确定候选beanName
	 * @param candidates a Map of candidate names and candidate instances
	 * (or candidate classes if not created yet) that match the required type
	 * @param requiredType the target dependency type to match against
	 * @return the name of the primary candidate, or {@code null} if none found
	 * @see #isPrimary(String, Object)
	 */
	@Nullable
	protected String determinePrimaryCandidate(Map<String, Object> candidates, Class<?> requiredType) {
		// 返回值
		String primaryBeanName = null;

		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			// 容器中的BeanName
			String candidateBeanName = entry.getKey();
			// 容器中的 BeanInstance / BeanClass
			Object beanInstance = entry.getValue();
			// 是否是主要的 primary 修饰
			if (isPrimary(candidateBeanName, beanInstance)) {
				if (primaryBeanName != null) {
					// 是否存在 bean 定义, 检查对象是 容器中的 key
					boolean candidateLocal = containsBeanDefinition(candidateBeanName);
					//  是否存在 bean 定义, 检查对象是 primaryBeanName
					boolean primaryLocal = containsBeanDefinition(primaryBeanName);
					if (candidateLocal && primaryLocal) {
						throw new NoUniqueBeanDefinitionException(requiredType, candidates.size(),
								"more than one 'primary' bean found among candidates: " + candidates.keySet());
					}
					else if (candidateLocal) {
						primaryBeanName = candidateBeanName;
					}
				}
				else {
					primaryBeanName = candidateBeanName;
				}
			}
		}
		return primaryBeanName;
	}

	/**
	 * Determine the candidate with the highest priority in the given set of beans.
	 * <p>Based on {@code @javax.annotation.Priority}. As defined by the related
	 * {@link org.springframework.core.Ordered} interface, the lowest value has
	 * the highest priority.
	 * 确定优先级最高的候选对象名称
	 * @param candidates a Map of candidate names and candidate instances
	 * (or candidate classes if not created yet) that match the required type
	 * @param requiredType the target dependency type to match against
	 * @return the name of the candidate with the highest priority,
	 * or {@code null} if none found
	 * @see #getPriority(Object)
	 */
	@Nullable
	protected String determineHighestPriorityCandidate(Map<String, Object> candidates, Class<?> requiredType) {
		// 优先级最高的 beanName
		String highestPriorityBeanName = null;
		// 最大优先级
		Integer highestPriority = null;
		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			// 容器中的BeanName
			String candidateBeanName = entry.getKey();
			// 容器中的 BeanInstance / BeanClass
			Object beanInstance = entry.getValue();
			if (beanInstance != null) {
				// 优先级序号 当前对象的优先级
				Integer candidatePriority = getPriority(beanInstance);
				if (candidatePriority != null) {
					if (highestPriorityBeanName != null) {
						if (candidatePriority.equals(highestPriority)) {
							throw new NoUniqueBeanDefinitionException(requiredType, candidates.size(),
									"Multiple beans found with the same priority ('" + highestPriority +
											"') among candidates: " + candidates.keySet());
						}
						// 当前对象优先级 < 最高优先级 重新赋值
						else if (candidatePriority < highestPriority) {
							highestPriorityBeanName = candidateBeanName;
							highestPriority = candidatePriority;
						}
					}
					else {
						highestPriorityBeanName = candidateBeanName;
						highestPriority = candidatePriority;
					}
				}
			}
		}
		return highestPriorityBeanName;
	}

	/**
	 * Return whether the bean definition for the given bean name has been
	 * marked as a primary bean.
	 * @param beanName the name of the bean
	 * @param beanInstance the corresponding bean instance (can be null)
	 * @return whether the given bean qualifies as primary
	 */
	protected boolean isPrimary(String beanName, Object beanInstance) {
		// 转换 beanName
		String transformedBeanName = transformedBeanName(beanName);
		// 是否包含 bean 定义
		if (containsBeanDefinition(transformedBeanName)) {
			// 获取 合并后的 BeanDefinition 然后调用 isPrimary 获取返回值
			return getMergedLocalBeanDefinition(transformedBeanName).isPrimary();
		}
		// 获取父bean工厂
		BeanFactory parent = getParentBeanFactory();
		// 从父类葛洪常州判断是否 primary
		return (parent instanceof DefaultListableBeanFactory &&
				((DefaultListableBeanFactory) parent).isPrimary(transformedBeanName, beanInstance));
	}

	/**
	 * Return the priority assigned for the given bean instance by
	 * the {@code javax.annotation.Priority} annotation.
	 * <p>The default implementation delegates to the specified
	 * {@link #setDependencyComparator dependency comparator}, checking its
	 * {@link OrderComparator#getPriority method} if it is an extension of
	 * Spring's common {@link OrderComparator} - typically, an
	 * {@link org.springframework.core.annotation.AnnotationAwareOrderComparator}.
	 * If no such comparator is present, this implementation returns {@code null}.
	 * @param beanInstance the bean instance to check (can be {@code null})
	 * @return the priority assigned to that bean or {@code null} if none is set
	 */
	@Nullable
	protected Integer getPriority(Object beanInstance) {
		Comparator<Object> comparator = getDependencyComparator();
		if (comparator instanceof OrderComparator) {
			return ((OrderComparator) comparator).getPriority(beanInstance);
		}
		return null;
	}

	/**
	 * Determine whether the given candidate name matches the bean name or the aliases
	 * stored in this bean definition.
	 */
	protected boolean matchesBeanName(String beanName, @Nullable String candidateName) {
		return (candidateName != null &&
				(candidateName.equals(beanName) || ObjectUtils.containsElement(getAliases(beanName), candidateName)));
	}

	/**
	 * Determine whether the given beanName/candidateName pair indicates a self reference,
	 * i.e. whether the candidate points back to the original bean or to a factory method
	 * on the original bean.
	 *
	 * 是否子引用
	 */
	private boolean isSelfReference(@Nullable String beanName, @Nullable String candidateName) {
		// 1. beanName 不为空
		// 2. 候选值不为空
		// 3. beanName eq  候选值
		// 4. beanName 存在于 bean定义容器(beanDefinitionMap)中
		// 5. candidateName 对应的 beanDefinition 的  factoryBeanName 是否和 beanName 相同
		return (beanName != null && candidateName != null &&
				(beanName.equals(candidateName) || (containsBeanDefinition(candidateName) &&
						beanName.equals(getMergedLocalBeanDefinition(candidateName).getFactoryBeanName()))));
	}

	/**
	 * Raise a NoSuchBeanDefinitionException or BeanNotOfRequiredTypeException
	 * for an unresolvable dependency.
	 */
	private void raiseNoMatchingBeanFound(
			Class<?> type, ResolvableType resolvableType, DependencyDescriptor descriptor) throws BeansException {

		// 验证bean是否是不必要的类型.
		checkBeanNotOfRequiredType(type, descriptor);

		throw new NoSuchBeanDefinitionException(resolvableType,
				"expected at least 1 bean which qualifies as autowire candidate. " +
						"Dependency annotations: " + ObjectUtils.nullSafeToString(descriptor.getAnnotations()));
	}

	/**
	 * Raise a BeanNotOfRequiredTypeException for an unresolvable dependency, if applicable,
	 * i.e. if the target type of the bean would match but an exposed proxy doesn't.
	 */
	private void checkBeanNotOfRequiredType(Class<?> type, DependencyDescriptor descriptor) {
		// beanDefinition名称列表循环
		for (String beanName : this.beanDefinitionNames) {
			// 获取 合并的 bean定义
			RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
			// beanDefinition 的目标类型
			Class<?> targetType = mbd.getTargetType();
			// 1. 参数type来自targetType
			// 2. 候选值验证通过
			if (targetType != null && type.isAssignableFrom(targetType) &&
					isAutowireCandidate(beanName, mbd, descriptor, getAutowireCandidateResolver())) {
				// Probably a proxy interfering with target type match -> throw meaningful exception.
				// 获取单例对象
				Object beanInstance = getSingleton(beanName, false);
				// 获取类型
				// 1. getClass
				// 2. 类型推断方法获取
				Class<?> beanType = (beanInstance != null && beanInstance.getClass() != NullBean.class ?
						beanInstance.getClass() : predictBeanType(beanName, mbd));
				// 不必要类型的验证条件
				if (beanType != null && !type.isAssignableFrom(beanType)) {
					throw new BeanNotOfRequiredTypeException(beanName, type, beanType);
				}
			}
		}

		// 获取父工厂 进行验证
		BeanFactory parent = getParentBeanFactory();
		if (parent instanceof DefaultListableBeanFactory) {
			((DefaultListableBeanFactory) parent).checkBeanNotOfRequiredType(type, descriptor);
		}
	}

	/**
	 * Create an {@link Optional} wrapper for the specified dependency.
	 */
	private Optional<?> createOptionalDependency(
			DependencyDescriptor descriptor, @Nullable String beanName, final Object... args) {

		DependencyDescriptor descriptorToUse = new NestedDependencyDescriptor(descriptor) {
			@Override
			public boolean isRequired() {
				return false;
			}

			@Override
			public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory) {
				return (!ObjectUtils.isEmpty(args) ? beanFactory.getBean(beanName, args) :
						super.resolveCandidate(beanName, requiredType, beanFactory));
			}
		};
		Object result = doResolveDependency(descriptorToUse, beanName, null, null);
		return (result instanceof Optional ? (Optional<?>) result : Optional.ofNullable(result));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(ObjectUtils.identityToString(this));
		sb.append(": defining beans [");
		sb.append(StringUtils.collectionToCommaDelimitedString(this.beanDefinitionNames));
		sb.append("]; ");
		BeanFactory parent = getParentBeanFactory();
		if (parent == null) {
			sb.append("root of factory hierarchy");
		}
		else {
			sb.append("parent: ").append(ObjectUtils.identityToString(parent));
		}
		return sb.toString();
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		throw new NotSerializableException("DefaultListableBeanFactory itself is not deserializable - " +
				"just a SerializedBeanFactoryReference is");
	}


	//---------------------------------------------------------------------
	// Serialization support
	//---------------------------------------------------------------------

	protected Object writeReplace() throws ObjectStreamException {
		if (this.serializationId != null) {
			return new SerializedBeanFactoryReference(this.serializationId);
		}
		else {
			throw new NotSerializableException("DefaultListableBeanFactory has no serialization id");
		}
	}

	private interface BeanObjectProvider<T> extends ObjectProvider<T>, Serializable {
	}

	/**
	 * Minimal id reference to the factory.
	 * Resolved to the actual factory instance on deserialization.
	 */
	private static class SerializedBeanFactoryReference implements Serializable {

		private final String id;

		public SerializedBeanFactoryReference(String id) {
			this.id = id;
		}

		private Object readResolve() {
			Reference<?> ref = serializableFactories.get(this.id);
			if (ref != null) {
				Object result = ref.get();
				if (result != null) {
					return result;
				}
			}
			// Lenient fallback: dummy factory in case of original factory not found...
			DefaultListableBeanFactory dummyFactory = new DefaultListableBeanFactory();
			dummyFactory.serializationId = this.id;
			return dummyFactory;
		}
	}

	/**
	 * A dependency descriptor marker for nested elements.
	 */
	private static class NestedDependencyDescriptor extends DependencyDescriptor {

		public NestedDependencyDescriptor(DependencyDescriptor original) {
			super(original);
			increaseNestingLevel();
		}
	}

	/**
	 * A dependency descriptor for a multi-element declaration with nested elements.
	 */
	private static class MultiElementDescriptor extends NestedDependencyDescriptor {

		public MultiElementDescriptor(DependencyDescriptor original) {
			super(original);
		}
	}

	/**
	 * A dependency descriptor marker for stream access to multiple elements.
	 */
	private static class StreamDependencyDescriptor extends DependencyDescriptor {

		/**
		 * 是否允许排序
		 */
		private final boolean ordered;

		public StreamDependencyDescriptor(DependencyDescriptor original, boolean ordered) {
			super(original);
			this.ordered = ordered;
		}

		public boolean isOrdered() {
			return this.ordered;
		}
	}

	/**
	 * Serializable ObjectFactory/ObjectProvider for lazy resolution of a dependency.
	 */
	private class DependencyObjectProvider implements BeanObjectProvider<Object> {

		/**
		 * 依赖描述
		 */
		private final DependencyDescriptor descriptor;

		/**
		 * 是否是 Optional 类型
		 */
		private final boolean optional;

		@Nullable
		private final String beanName;

		public DependencyObjectProvider(DependencyDescriptor descriptor, @Nullable String beanName) {
			this.descriptor = new NestedDependencyDescriptor(descriptor);
			this.optional = (this.descriptor.getDependencyType() == Optional.class);
			this.beanName = beanName;
		}

		@Override
		public Object getObject() throws BeansException {
			// 获取bean实例
			// 是 optional 的情况下创建
			if (this.optional) {
				return createOptionalDependency(this.descriptor, this.beanName);
			}
			else {
				// 解析依赖进行创建对象
				Object result = doResolveDependency(this.descriptor, this.beanName, null, null);
				if (result == null) {
					throw new NoSuchBeanDefinitionException(this.descriptor.getResolvableType());
				}
				return result;
			}
		}

		@Override
		public Object getObject(final Object... args) throws BeansException {
			// 获取bean实例
			// 是 optional 的情况下创建
			if (this.optional) {
				return createOptionalDependency(this.descriptor, this.beanName, args);
			}
			else {
				// 创建 依赖描述对象， 重写方法 resolveCandidate , 从容器(BeanFactory)中获取
				DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {
					@Override
					public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory) {
						return beanFactory.getBean(beanName, args);
					}
				};

				// 解析依赖进行创建对象
				Object result = doResolveDependency(descriptorToUse, this.beanName, null, null);
				if (result == null) {
					throw new NoSuchBeanDefinitionException(this.descriptor.getResolvableType());
				}
				return result;
			}
		}

		@Override
		@Nullable
		public Object getIfAvailable() throws BeansException {
			if (this.optional) {
				return createOptionalDependency(this.descriptor, this.beanName);
			}
			else {
				DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {
					@Override
					public boolean isRequired() {
						return false;
					}
				};
				return doResolveDependency(descriptorToUse, this.beanName, null, null);
			}
		}

		@Override
		@Nullable
		public Object getIfUnique() throws BeansException {
			DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {
				@Override
				public boolean isRequired() {
					return false;
				}

				@Override
				@Nullable
				public Object resolveNotUnique(ResolvableType type, Map<String, Object> matchingBeans) {
					return null;
				}
			};
			if (this.optional) {
				return createOptionalDependency(descriptorToUse, this.beanName);
			}
			else {
				return doResolveDependency(descriptorToUse, this.beanName, null, null);
			}
		}

		@Nullable
		protected Object getValue() throws BeansException {
			if (this.optional) {
				return createOptionalDependency(this.descriptor, this.beanName);
			}
			else {
				return doResolveDependency(this.descriptor, this.beanName, null, null);
			}
		}

		@Override
		public Stream<Object> stream() {
			return resolveStream(false);
		}

		@Override
		public Stream<Object> orderedStream() {
			return resolveStream(true);
		}

		@SuppressWarnings("unchecked")
		private Stream<Object> resolveStream(boolean ordered) {
			DependencyDescriptor descriptorToUse = new StreamDependencyDescriptor(this.descriptor, ordered);
			Object result = doResolveDependency(descriptorToUse, this.beanName, null, null);
			return (result instanceof Stream ? (Stream<Object>) result : Stream.of(result));
		}
	}

	/**
	 * Separate inner class for avoiding a hard dependency on the {@code javax.inject} API.
	 * Actual {@code javax.inject.Provider} implementation is nested here in order to make it
	 * invisible for Graal's introspection of DefaultListableBeanFactory's nested classes.
	 */
	private class Jsr330Factory implements Serializable {

		public Object createDependencyProvider(DependencyDescriptor descriptor, @Nullable String beanName) {
			return new Jsr330Provider(descriptor, beanName);
		}

		private class Jsr330Provider extends DependencyObjectProvider implements Provider<Object> {

			public Jsr330Provider(DependencyDescriptor descriptor, @Nullable String beanName) {
				super(descriptor, beanName);
			}

			@Override
			@Nullable
			public Object get() throws BeansException {
				return getValue();
			}
		}
	}

	/**
	 * An {@link org.springframework.core.OrderComparator.OrderSourceProvider} implementation
	 * that is aware of the bean metadata of the instances to sort.
	 * <p>Lookup for the method factory of an instance to sort, if any, and let the
	 * comparator retrieve the {@link org.springframework.core.annotation.Order}
	 * value defined on it. This essentially allows for the following construct:
	 */
	private class FactoryAwareOrderSourceProvider implements OrderComparator.OrderSourceProvider {

		/**
		 * key: beanInstance
		 * value: beanName
		 */
		private final Map<Object, String> instancesToBeanNames;

		public FactoryAwareOrderSourceProvider(Map<Object, String> instancesToBeanNames) {
			this.instancesToBeanNames = instancesToBeanNames;
		}

		@Override
		@Nullable
		public Object getOrderSource(Object obj) {
			String beanName = this.instancesToBeanNames.get(obj);
			// beanName 不为空
			// 存在 beanName 对应的 bean 定义
			if (beanName == null || !containsBeanDefinition(beanName)) {
				return null;
			}
			// 获取 beanName 的合并定义
			RootBeanDefinition beanDefinition = getMergedLocalBeanDefinition(beanName);
			List<Object> sources = new ArrayList<>(2);
			// 获取工厂函数
			Method factoryMethod = beanDefinition.getResolvedFactoryMethod();
			if (factoryMethod != null) {
				sources.add(factoryMethod);
			}
			// 获取 bean class 加入 sources
			Class<?> targetType = beanDefinition.getTargetType();
			if (targetType != null && targetType != obj.getClass()) {
				sources.add(targetType);
			}
			return sources.toArray();
		}
	}

	static {
		try {
			javaxInjectProviderClass =
					ClassUtils.forName("javax.inject.Provider", DefaultListableBeanFactory.class.getClassLoader());
		}
		catch (ClassNotFoundException ex) {
			// JSR-330 API not available - Provider interface simply not supported then.
			javaxInjectProviderClass = null;
		}
	}

}
