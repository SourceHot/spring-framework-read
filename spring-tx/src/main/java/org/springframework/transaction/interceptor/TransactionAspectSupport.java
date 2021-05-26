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

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import io.vavr.control.Try;
import kotlin.reflect.KFunction;
import kotlin.reflect.jvm.ReflectJvmMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.core.KotlinDetector;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.ReactiveAdapter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.lang.Nullable;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.TransactionUsageException;
import org.springframework.transaction.reactive.TransactionContextManager;
import org.springframework.transaction.support.CallbackPreferringPlatformTransactionManager;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

/**
 * Base class for transactional aspects, such as the {@link TransactionInterceptor} or an AspectJ
 * aspect.
 *
 * <p>This enables the underlying Spring transaction infrastructure to be used easily
 * to implement an aspect for any aspect system.
 *
 * <p>Subclasses are responsible for calling methods in this class in the correct order.
 *
 * <p>If no transaction name has been specified in the {@link TransactionAttribute},
 * the exposed name will be the {@code fully-qualified class name + "." + method name} (by
 * default).
 *
 * <p>Uses the <b>Strategy</b> design pattern. A {@link PlatformTransactionManager} or
 * {@link ReactiveTransactionManager} implementation will perform the actual transaction management,
 * and a {@link TransactionAttributeSource} (e.g. annotation-based) is used for determining
 * transaction definitions for a particular class or method.
 *
 * <p>A transaction aspect is serializable if its {@code PlatformTransactionManager}
 * and {@code TransactionAttributeSource} are serializable.
 *
 * 事务切面支持
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Stéphane Nicoll
 * @author Sam Brannen
 * @author Mark Paluch
 * @see PlatformTransactionManager
 * @see ReactiveTransactionManager
 * @see #setTransactionManager
 * @see #setTransactionAttributes
 * @see #setTransactionAttributeSource
 * @since 1.1
 */
public abstract class TransactionAspectSupport implements BeanFactoryAware, InitializingBean {

	// NOTE: This class must not implement Serializable because it serves as base
	// class for AspectJ aspects (which are not allowed to implement Serializable)!


	/**
	 * Key to use to store the default transaction manager.
	 *
	 * 用于存储默认事务管理器的键。
	 */
	private static final Object DEFAULT_TRANSACTION_MANAGER_KEY = new Object();

	/**
	 * Vavr library present on the classpath?
	 * 是否存在io.vavr.control.Try类
	 */
	private static final boolean vavrPresent = ClassUtils.isPresent(
			"io.vavr.control.Try", TransactionAspectSupport.class.getClassLoader());

	/**
	 * Reactive Streams API present on the classpath?
	 *
	 * 是否存在org.reactivestreams.Publisher类
	 */
	private static final boolean reactiveStreamsPresent =
			ClassUtils.isPresent("org.reactivestreams.Publisher",
					TransactionAspectSupport.class.getClassLoader());

	/**
	 * Holder to support the {@code currentTransactionStatus()} method, and to support communication
	 * between different cooperating advices (e.g. before and after advice) if the aspect involves
	 * more than a single method (as will be the case for around advice).
	 *
	 * 线程变量,用于存储事务信息
	 */
	private static final ThreadLocal<TransactionInfo> transactionInfoHolder =
			new NamedThreadLocal<>("Current aspect-driven transaction");

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 响应式适配器注册表
	 */
	@Nullable
	private final ReactiveAdapterRegistry reactiveAdapterRegistry;

	/**
	 * 事务管理器缓存
	 */
	private final ConcurrentMap<Object, TransactionManager> transactionManagerCache =
			new ConcurrentReferenceHashMap<>(4);

	/**
	 * 响应式事务支持(反应式事务支持)
	 *
	 */
	private final ConcurrentMap<Method, ReactiveTransactionSupport> transactionSupportCache =
			new ConcurrentReferenceHashMap<>(1024);

	/**
	 * 事务管理器名称
	 */
	@Nullable
	private String transactionManagerBeanName;

	/**
	 * 事务管理器
	 */
	@Nullable
	private TransactionManager transactionManager;

	/**
	 * 事务属性源
	 */
	@Nullable
	private TransactionAttributeSource transactionAttributeSource;

	/**
	 * Bean工程，用于存储Bean实例或者创建Bean实例。
	 */
	@Nullable
	private BeanFactory beanFactory;

	protected TransactionAspectSupport() {
		if (reactiveStreamsPresent) {
			this.reactiveAdapterRegistry = ReactiveAdapterRegistry.getSharedInstance();
		}
		else {
			this.reactiveAdapterRegistry = null;
		}
	}

	/**
	 * Subclasses can use this to return the current TransactionInfo. Only subclasses that cannot
	 * handle all operations in one method, such as an AspectJ aspect involving distinct before and
	 * after advice, need to use this mechanism to get at the current TransactionInfo. An around
	 * advice such as an AOP Alliance MethodInterceptor can hold a reference to the TransactionInfo
	 * throughout the aspect method.
	 * <p>A TransactionInfo will be returned even if no transaction was created.
	 * The {@code TransactionInfo.hasTransaction()} method can be used to query this.
	 * <p>To find out about specific transaction characteristics, consider using
	 * TransactionSynchronizationManager's {@code isSynchronizationActive()} and/or {@code
	 * isActualTransactionActive()} methods.
	 *
	 * @return the TransactionInfo bound to this thread, or {@code null} if none
	 * @see TransactionInfo#hasTransaction()
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isSynchronizationActive()
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isActualTransactionActive()
	 */
	@Nullable
	protected static TransactionInfo currentTransactionInfo() throws NoTransactionException {
		return transactionInfoHolder.get();
	}

	/**
	 * Return the transaction status of the current method invocation. Mainly intended for code that
	 * wants to set the current transaction rollback-only but not throw an application exception.
	 *
	 * @throws NoTransactionException if the transaction info cannot be found, because the method
	 *                                was invoked outside an AOP invocation context
	 */
	public static TransactionStatus currentTransactionStatus() throws NoTransactionException {
		TransactionInfo info = currentTransactionInfo();
		if (info == null || info.transactionStatus == null) {
			throw new NoTransactionException(
					"No transaction aspect-managed TransactionStatus in scope");
		}
		return info.transactionStatus;
	}

	/**
	 * Return the name of the default transaction manager bean.
	 */
	@Nullable
	protected final String getTransactionManagerBeanName() {
		return this.transactionManagerBeanName;
	}

	/**
	 * Specify the name of the default transaction manager bean. This can either point to a
	 * traditional {@link PlatformTransactionManager} or a {@link ReactiveTransactionManager} for
	 * reactive transaction management.
	 */
	public void setTransactionManagerBeanName(@Nullable String transactionManagerBeanName) {
		this.transactionManagerBeanName = transactionManagerBeanName;
	}

	/**
	 * Return the default transaction manager, or {@code null} if unknown. This can either be a
	 * traditional {@link PlatformTransactionManager} or a {@link ReactiveTransactionManager} for
	 * reactive transaction management.
	 */
	@Nullable
	public TransactionManager getTransactionManager() {
		return this.transactionManager;
	}

	/**
	 * Specify the <em>default</em> transaction manager to use to drive transactions. This can
	 * either be a traditional {@link PlatformTransactionManager} or a {@link
	 * ReactiveTransactionManager} for reactive transaction management.
	 * <p>The default transaction manager will be used if a <em>qualifier</em>
	 * has not been declared for a given transaction or if an explicit name for the default
	 * transaction manager bean has not been specified.
	 *
	 * @see #setTransactionManagerBeanName
	 */
	public void setTransactionManager(@Nullable TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Set properties with method names as keys and transaction attribute descriptors (parsed via
	 * TransactionAttributeEditor) as values: e.g. key = "myMethod", value =
	 * "PROPAGATION_REQUIRED,readOnly".
	 * <p>Note: Method names are always applied to the target class,
	 * no matter if defined in an interface or the class itself.
	 * <p>Internally, a NameMatchTransactionAttributeSource will be
	 * created from the given properties.
	 *
	 * @see #setTransactionAttributeSource
	 * @see TransactionAttributeEditor
	 * @see NameMatchTransactionAttributeSource
	 */
	public void setTransactionAttributes(Properties transactionAttributes) {
		NameMatchTransactionAttributeSource tas = new NameMatchTransactionAttributeSource();
		tas.setProperties(transactionAttributes);
		this.transactionAttributeSource = tas;
	}

	/**
	 * Set multiple transaction attribute sources which are used to find transaction attributes.
	 * Will build a CompositeTransactionAttributeSource for the given sources.
	 *
	 * @see CompositeTransactionAttributeSource
	 * @see MethodMapTransactionAttributeSource
	 * @see NameMatchTransactionAttributeSource
	 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
	 */
	public void setTransactionAttributeSources(
			TransactionAttributeSource... transactionAttributeSources) {
		this.transactionAttributeSource = new CompositeTransactionAttributeSource(
				transactionAttributeSources);
	}

	/**
	 * Return the transaction attribute source.
	 */
	@Nullable
	public TransactionAttributeSource getTransactionAttributeSource() {
		return this.transactionAttributeSource;
	}

	/**
	 * Set the transaction attribute source which is used to find transaction attributes. If
	 * specifying a String property value, a PropertyEditor will create a
	 * MethodMapTransactionAttributeSource from the value.
	 *
	 * @see TransactionAttributeSourceEditor
	 * @see MethodMapTransactionAttributeSource
	 * @see NameMatchTransactionAttributeSource
	 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
	 */
	public void setTransactionAttributeSource(
			@Nullable TransactionAttributeSource transactionAttributeSource) {
		this.transactionAttributeSource = transactionAttributeSource;
	}

	/**
	 * Return the BeanFactory to use for retrieving PlatformTransactionManager beans.
	 */
	@Nullable
	protected final BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	/**
	 * Set the BeanFactory to use for retrieving PlatformTransactionManager beans.
	 */
	@Override
	public void setBeanFactory(@Nullable BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Check that required properties were set.
	 */
	@Override
	public void afterPropertiesSet() {
		// 事务管理对象为空 并且 BeanFactory 为空
		if (getTransactionManager() == null && this.beanFactory == null) {
			throw new IllegalStateException(
					"Set the 'transactionManager' property or make sure to run within a BeanFactory "
							+
							"containing a TransactionManager bean!");
		}
		// 事务属性源对象为空
		if (getTransactionAttributeSource() == null) {
			throw new IllegalStateException(
					"Either 'transactionAttributeSource' or 'transactionAttributes' is required: " +
							"If there are no transactional methods, then don't use a transaction aspect.");
		}
	}


	/**
	 * General delegate for around-advice-based subclasses, delegating to several other template
	 * methods on this class. Able to handle {@link CallbackPreferringPlatformTransactionManager} as
	 * well as regular {@link PlatformTransactionManager} implementations.
	 * <p>
	 * 带着事务执行
	 *
	 * @param method      the Method being invoked
	 * @param targetClass the target class that we're invoking the method on
	 * @param invocation  the callback to use for proceeding with the target invocation
	 * @return the return value of the method, if any
	 * @throws Throwable propagated from the target invocation
	 */
	@Nullable
	protected Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,
			final InvocationCallback invocation) throws Throwable {

		// If the transaction attribute is null, the method is non-transactional.
		// 获取事务属性
		TransactionAttributeSource tas = getTransactionAttributeSource();
		// 获取事务属性
		final TransactionAttribute txAttr = (tas != null ? tas
				.getTransactionAttribute(method, targetClass) : null);
		// 确定事物管理器
		final TransactionManager tm = determineTransactionManager(txAttr);

		// 事务管理器是响应式的处理
		if (this.reactiveAdapterRegistry != null && tm instanceof ReactiveTransactionManager) {
			// 从响应式事务缓存中获取响应式事务支持对象
			ReactiveTransactionSupport txSupport = this.transactionSupportCache
					.computeIfAbsent(method, key -> {
						if (KotlinDetector.isKotlinType(method.getDeclaringClass())
								&& KotlinDelegate.isSuspend(method)) {
							throw new TransactionUsageException(
									"Unsupported annotated transaction on suspending function detected: "
											+ method +
											". Use TransactionalOperator.transactional extensions instead.");
						}
						// 响应式适配器注册表中获取响应式适配器
						ReactiveAdapter adapter = this.reactiveAdapterRegistry
								.getAdapter(method.getReturnType());
						if (adapter == null) {
							throw new IllegalStateException(
									"Cannot apply reactive transaction to non-reactive return type: "
											+
											method.getReturnType());
						}
						return new ReactiveTransactionSupport(adapter);
					});
			// 响应式事务执行方法
			return txSupport.invokeWithinTransaction(
					method, targetClass, invocation, txAttr, (ReactiveTransactionManager) tm);
		}

		// 事务类型转换
		PlatformTransactionManager ptm = asPlatformTransactionManager(tm);
		// 确定切点
		final String joinpointIdentification = methodIdentification(method, targetClass, txAttr);

		// 事务属性为空 事务类型不是CallbackPreferringPlatformTransactionManager
		if (txAttr == null || !(ptm instanceof CallbackPreferringPlatformTransactionManager)) {
			// Standard transaction demarcation with getTransaction and commit/rollback calls.
			// 创建一个新的事务信息
			TransactionInfo txInfo = createTransactionIfNecessary(ptm, txAttr,
					joinpointIdentification);

			Object retVal;
			try {
				// This is an around advice: Invoke the next interceptor in the chain.
				// This will normally result in a target object being invoked.
				// 回调方法
				retVal = invocation.proceedWithInvocation();
			}
			catch (Throwable ex) {
				// target invocation exception
				// 回滚异常
				completeTransactionAfterThrowing(txInfo, ex);
				throw ex;
			}
			finally {
				// 清除事务信息
				cleanupTransactionInfo(txInfo);
			}

			// vavr相关处理
			if (vavrPresent && VavrDelegate.isVavrTry(retVal)) {
				// Set rollback-only in case of Vavr failure matching our rollback rules...
				TransactionStatus status = txInfo.getTransactionStatus();
				if (status != null && txAttr != null) {
					retVal = VavrDelegate.evaluateTryFailure(retVal, txAttr, status);
				}
			}

			// 提交事务的后置操作
			commitTransactionAfterReturning(txInfo);
			return retVal;
		}
		// 事务管理器是CallbackPreferringPlatformTransactionManager时的处理
		else {
			// 异常持有器
			final ThrowableHolder throwableHolder = new ThrowableHolder();

			// It's a CallbackPreferringPlatformTransactionManager: pass a TransactionCallback in.
			try {
				// 执行
				Object result = ((CallbackPreferringPlatformTransactionManager) ptm)
						.execute(txAttr, status -> {
							// 根据事务属性 + 切点 + 事务状态创建事务属性对象
							TransactionInfo txInfo = prepareTransactionInfo(ptm, txAttr,
									joinpointIdentification, status);
							try {
								// 调用实际方法
								Object retVal = invocation.proceedWithInvocation();
								// vavr 支持处理
								if (vavrPresent && VavrDelegate.isVavrTry(retVal)) {
									// Set rollback-only in case of Vavr failure matching our rollback rules...
									retVal = VavrDelegate
											.evaluateTryFailure(retVal, txAttr, status);
								}
								return retVal;
							}
							catch (Throwable ex) {
								// 判断是否是需要处理的异常
								if (txAttr.rollbackOn(ex)) {
									// A RuntimeException: will lead to a rollback.
									if (ex instanceof RuntimeException) {
										throw (RuntimeException) ex;
									}
									else {
										throw new ThrowableHolderException(ex);
									}
								}
								else {
									// A normal return value: will lead to a commit.
									// 异常对象设置
									throwableHolder.throwable = ex;
									return null;
								}
							}
							finally {
								// 清除事务信息
								cleanupTransactionInfo(txInfo);
							}
						});

				// Check result state: It might indicate a Throwable to rethrow.
				// 异常持有器中持有异常抛出异常
				if (throwableHolder.throwable != null) {
					throw throwableHolder.throwable;
				}
				// 处理结果返回
				return result;
			}
			catch (ThrowableHolderException ex) {
				throw ex.getCause();
			}
			catch (TransactionSystemException ex2) {
				if (throwableHolder.throwable != null) {
					logger.error("Application exception overridden by commit exception",
							throwableHolder.throwable);
					ex2.initApplicationException(throwableHolder.throwable);
				}
				throw ex2;
			}
			catch (Throwable ex2) {
				if (throwableHolder.throwable != null) {
					logger.error("Application exception overridden by commit exception",
							throwableHolder.throwable);
				}
				throw ex2;
			}
		}
	}

	/**
	 * Clear the cache.
	 */
	protected void clearTransactionManagerCache() {
		this.transactionManagerCache.clear();
		this.beanFactory = null;
	}

	/**
	 * Determine the specific transaction manager to use for the given transaction.
	 *
	 * <p></p>
	 * 确定事务管理器
	 */
	@Nullable
	protected TransactionManager determineTransactionManager(
			@Nullable TransactionAttribute txAttr) {
		// Do not attempt to lookup tx manager if no tx attributes are set
		// 空判断返回一个事务管理器
		if (txAttr == null || this.beanFactory == null) {
			return getTransactionManager();
		}

		// 属性是否有限定词
		String qualifier = txAttr.getQualifier();
		// 如果有别名
		if (StringUtils.hasText(qualifier)) {
			// 从 ioc 容器中根据类型和名称获取事务管理器
			return determineQualifiedTransactionManager(this.beanFactory, qualifier);
		}
		else if (StringUtils.hasText(this.transactionManagerBeanName)) {
			// 从 ioc 容器中根据类型和名称获取事务管理器
			return determineQualifiedTransactionManager(this.beanFactory,
					this.transactionManagerBeanName);
		}
		else {
			// 获取成员变量 transactionManager
			TransactionManager defaultTransactionManager = getTransactionManager();
			// 如果没有
			if (defaultTransactionManager == null) {
				// 尝试从缓存中获取
				defaultTransactionManager = this.transactionManagerCache
						.get(DEFAULT_TRANSACTION_MANAGER_KEY);
				// 缓存里面没有从 ioc 容器中获取并且设置缓存
				if (defaultTransactionManager == null) {
					defaultTransactionManager = this.beanFactory.getBean(TransactionManager.class);
					// 放置到缓存中
					this.transactionManagerCache.putIfAbsent(
							DEFAULT_TRANSACTION_MANAGER_KEY, defaultTransactionManager);
				}
			}
			return defaultTransactionManager;
		}
	}

	/**
	 * 从 ioc 容器中根据类型和名称获取事务管理器
	 *
	 * @param beanFactory
	 * @param qualifier
	 * @return
	 */
	private TransactionManager determineQualifiedTransactionManager(BeanFactory beanFactory,
			String qualifier) {
		// 从缓存中获取
		TransactionManager txManager = this.transactionManagerCache.get(qualifier);
		// 缓存中获取失败
		if (txManager == null) {
			// 通过工具类获取
			txManager = BeanFactoryAnnotationUtils.qualifiedBeanOfType(
					beanFactory, TransactionManager.class, qualifier);
			// 将数据放入缓存
			this.transactionManagerCache.putIfAbsent(qualifier, txManager);
		}
		return txManager;
	}


	/**
	 * 类型转换,
	 *
	 * @param transactionManager
	 * @return
	 */
	@Nullable
	private PlatformTransactionManager asPlatformTransactionManager(
			@Nullable Object transactionManager) {
		// 判断事务管理对象为空 或者事务管理对象类型是PlatformTransactionManager
		if (transactionManager == null
				|| transactionManager instanceof PlatformTransactionManager) {
			return (PlatformTransactionManager) transactionManager;
		}
		else {
			throw new IllegalStateException(
					"Specified transaction manager is not a PlatformTransactionManager: "
							+ transactionManager);
		}
	}

	/**
	 * 获取切入点
	 *
	 * @param method
	 * @param targetClass
	 * @param txAttr
	 * @return
	 */
	private String methodIdentification(Method method, @Nullable Class<?> targetClass,
			@Nullable TransactionAttribute txAttr) {

		// 方法签名
		String methodIdentification = methodIdentification(method, targetClass);
		// 方法签名为空时
		if (methodIdentification == null) {
			// 事务属性类型是DefaultTransactionAttribute
			if (txAttr instanceof DefaultTransactionAttribute) {
				// 通过DefaultTransactionAttribute提供给的getDescriptor方法获取切入点
				methodIdentification = ((DefaultTransactionAttribute) txAttr).getDescriptor();
			}
			if (methodIdentification == null) {
				// 获取方法签名
				methodIdentification = ClassUtils.getQualifiedMethodName(method, targetClass);
			}
		}
		return methodIdentification;
	}

	/**
	 * Convenience method to return a String representation of this Method for use in logging. Can
	 * be overridden in subclasses to provide a different identifier for the given method.
	 * <p>The default implementation returns {@code null}, indicating the
	 * use of {@link DefaultTransactionAttribute#getDescriptor()} instead, ending up as {@link
	 * ClassUtils#getQualifiedMethodName(Method, Class)}.
	 *
	 * @param method      the method we're interested in
	 * @param targetClass the class that the method is being invoked on
	 * @return a String representation identifying this method
	 * @see org.springframework.util.ClassUtils#getQualifiedMethodName
	 */
	@Nullable
	protected String methodIdentification(Method method, @Nullable Class<?> targetClass) {
		return null;
	}

	/**
	 * Create a transaction if necessary based on the given TransactionAttribute.
	 * <p>Allows callers to perform custom TransactionAttribute lookups through
	 * the TransactionAttributeSource.
	 *
	 * @param txAttr                  the TransactionAttribute (may be {@code null})
	 * @param joinpointIdentification the fully qualified method name (used for monitoring and
	 *                                logging purposes)
	 * @return a TransactionInfo object, whether or not a transaction was created. The {@code
	 * hasTransaction()} method on TransactionInfo can be used to tell if there was a transaction
	 * created.
	 * @see #getTransactionAttributeSource()
	 */
	@SuppressWarnings("serial")
	protected TransactionInfo createTransactionIfNecessary(@Nullable PlatformTransactionManager tm,
			@Nullable TransactionAttribute txAttr, final String joinpointIdentification) {

		// If no name specified, apply method identification as transaction name.
		// 将切面地址作为 DelegatingTransactionAttribute 的 getName方法返回值
		if (txAttr != null && txAttr.getName() == null) {
			txAttr = new DelegatingTransactionAttribute(txAttr) {
				@Override
				public String getName() {
					return joinpointIdentification;
				}
			};
		}

		// 准备事务状态对象
		TransactionStatus status = null;
		if (txAttr != null) {
			if (tm != null) {
				// 获取事务状态对象
				status = tm.getTransaction(txAttr);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Skipping transactional joinpoint [" + joinpointIdentification +
							"] because no transaction manager has been configured");
				}
			}
		}
		// 处理出一个 TransactionInfo
		return prepareTransactionInfo(tm, txAttr, joinpointIdentification, status);
	}

	/**
	 * Prepare a TransactionInfo for the given attribute and status object.
	 *
	 * @param txAttr                  the TransactionAttribute (may be {@code null})
	 * @param joinpointIdentification the fully qualified method name (used for monitoring and
	 *                                logging purposes)
	 * @param status                  the TransactionStatus for the current transaction
	 * @return the prepared TransactionInfo object
	 */
	protected TransactionInfo prepareTransactionInfo(@Nullable PlatformTransactionManager tm,
			@Nullable TransactionAttribute txAttr, String joinpointIdentification,
			@Nullable TransactionStatus status) {

		// 初始化
		TransactionInfo txInfo = new TransactionInfo(tm, txAttr, joinpointIdentification);
		if (txAttr != null) {
			// We need a transaction for this method...
			if (logger.isTraceEnabled()) {
				logger.trace(
						"Getting transaction for [" + txInfo.getJoinpointIdentification() + "]");
			}
			// The transaction manager will flag an error if an incompatible tx already exists.
			txInfo.newTransactionStatus(status);
		}
		else {
			// The TransactionInfo.hasTransaction() method will return false. We created it only
			// to preserve the integrity of the ThreadLocal stack maintained in this class.
			if (logger.isTraceEnabled()) {
				logger.trace("No need to create transaction for [" + joinpointIdentification +
						"]: This method is not transactional.");
			}
		}

		// We always bind the TransactionInfo to the thread, even if we didn't create
		// a new transaction here. This guarantees that the TransactionInfo stack
		// will be managed correctly even if no transaction was created by this aspect.
		// 和线程绑定
		txInfo.bindToThread();
		return txInfo;
	}

	/**
	 * Execute after successful completion of call, but not after an exception was handled. Do
	 * nothing if we didn't create a transaction.
	 *
	 * @param txInfo information about the current transaction
	 */
	protected void commitTransactionAfterReturning(@Nullable TransactionInfo txInfo) {
		if (txInfo != null && txInfo.getTransactionStatus() != null) {
			if (logger.isTraceEnabled()) {
				logger.trace(
						"Completing transaction for [" + txInfo.getJoinpointIdentification() + "]");
			}
			txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
		}
	}

	/**
	 * Handle a throwable, completing the transaction. We may commit or roll back, depending on the
	 * configuration.
	 * <p>
	 * 回滚异常
	 *
	 * @param txInfo information about the current transaction
	 * @param ex     throwable encountered
	 */
	protected void completeTransactionAfterThrowing(@Nullable TransactionInfo txInfo,
			Throwable ex) {
		if (txInfo != null && txInfo.getTransactionStatus() != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Completing transaction for [" + txInfo.getJoinpointIdentification() +
						"] after exception: " + ex);
			}
			// 判断是否需要进行回滚
			if (txInfo.transactionAttribute != null && txInfo.transactionAttribute.rollbackOn(ex)) {
				try {
					// 做回滚操作
					txInfo.getTransactionManager().rollback(txInfo.getTransactionStatus());
				}
				catch (TransactionSystemException ex2) {
					logger.error("Application exception overridden by rollback exception", ex);
					ex2.initApplicationException(ex);
					throw ex2;
				}
				catch (RuntimeException | Error ex2) {
					logger.error("Application exception overridden by rollback exception", ex);
					throw ex2;
				}
			}
			else {
				// We don't roll back on this exception.
				// Will still roll back if TransactionStatus.isRollbackOnly() is true.
				try {
					// org.springframework.transaction.support.AbstractPlatformTransactionManager.commit 的方法
					txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
				}
				catch (TransactionSystemException ex2) {
					logger.error("Application exception overridden by commit exception", ex);
					ex2.initApplicationException(ex);
					throw ex2;
				}
				catch (RuntimeException | Error ex2) {
					logger.error("Application exception overridden by commit exception", ex);
					throw ex2;
				}
			}
		}
	}

	/**
	 * Reset the TransactionInfo ThreadLocal.
	 * <p>Call this in all cases: exception or normal return!
	 *
	 * @param txInfo information about the current transaction (may be {@code null})
	 */
	protected void cleanupTransactionInfo(@Nullable TransactionInfo txInfo) {
		if (txInfo != null) {
			txInfo.restoreThreadLocalStatus();
		}
	}


	/**
	 * Simple callback interface for proceeding with the target invocation. Concrete
	 * interceptors/aspects adapt this to their invocation mechanism.
	 */
	@FunctionalInterface
	protected interface InvocationCallback {

		Object proceedWithInvocation() throws Throwable;
	}

	/**
	 * Opaque object used to hold transaction information. Subclasses must pass it back to methods
	 * on this class, but not see its internals.
	 */
	protected static final class TransactionInfo {
		/**
		 * 事务管理器
		 */
		@Nullable
		private final PlatformTransactionManager transactionManager;

		/**
		 * 事务属性
		 */
		@Nullable
		private final TransactionAttribute transactionAttribute;

		/**
		 * 接入点,切点
		 */
		private final String joinpointIdentification;

		/**
		 * 事务状态
		 */
		@Nullable
		private TransactionStatus transactionStatus;

		/**
		 * 历史事务信息
		 */
		@Nullable
		private TransactionInfo oldTransactionInfo;

		public TransactionInfo(@Nullable PlatformTransactionManager transactionManager,
				@Nullable TransactionAttribute transactionAttribute,
				String joinpointIdentification) {

			this.transactionManager = transactionManager;
			this.transactionAttribute = transactionAttribute;
			this.joinpointIdentification = joinpointIdentification;
		}

		public PlatformTransactionManager getTransactionManager() {
			Assert.state(this.transactionManager != null, "No PlatformTransactionManager set");
			return this.transactionManager;
		}

		@Nullable
		public TransactionAttribute getTransactionAttribute() {
			return this.transactionAttribute;
		}

		/**
		 * Return a String representation of this joinpoint (usually a Method call) for use in
		 * logging.
		 */
		public String getJoinpointIdentification() {
			return this.joinpointIdentification;
		}

		public void newTransactionStatus(@Nullable TransactionStatus status) {
			this.transactionStatus = status;
		}

		@Nullable
		public TransactionStatus getTransactionStatus() {
			return this.transactionStatus;
		}

		/**
		 * Return whether a transaction was created by this aspect, or whether we just have a
		 * placeholder to keep ThreadLocal stack integrity.
		 */
		public boolean hasTransaction() {
			return (this.transactionStatus != null);
		}

		private void bindToThread() {
			// Expose current TransactionStatus, preserving any existing TransactionStatus
			// for restoration after this transaction is complete.
			this.oldTransactionInfo = transactionInfoHolder.get();
			transactionInfoHolder.set(this);
		}

		private void restoreThreadLocalStatus() {
			// Use stack to restore old transaction TransactionInfo.
			// Will be null if none was set.
			// 老的数据放回去
			transactionInfoHolder.set(this.oldTransactionInfo);
		}

		@Override
		public String toString() {
			return (this.transactionAttribute != null ? this.transactionAttribute.toString()
					: "No transaction");
		}
	}

	/**
	 * Internal holder class for a Throwable in a callback transaction model.
	 * 异常持有器
	 */
	private static class ThrowableHolder {

		@Nullable
		public Throwable throwable;
	}


	/**
	 * Internal holder class for a Throwable, used as a RuntimeException to be thrown from a
	 * TransactionCallback (and subsequently unwrapped again).
	 *
	 * 运行时异常对象
	 */
	@SuppressWarnings("serial")
	private static class ThrowableHolderException extends RuntimeException {

		public ThrowableHolderException(Throwable throwable) {
			super(throwable);
		}

		@Override
		public String toString() {
			return getCause().toString();
		}
	}


	/**
	 * Inner class to avoid a hard dependency on the Vavr library at runtime.
	 *
	 * Vavr 相关类
	 */
	private static class VavrDelegate {

		public static boolean isVavrTry(Object retVal) {
			return (retVal instanceof Try);
		}

		public static Object evaluateTryFailure(Object retVal, TransactionAttribute txAttr,
				TransactionStatus status) {
			return ((Try<?>) retVal).onFailure(ex -> {
				if (txAttr.rollbackOn(ex)) {
					status.setRollbackOnly();
				}
			});
		}
	}

	/**
	 * Inner class to avoid a hard dependency on Kotlin at runtime.
	 *
	 * kotlin 相关类
	 */
	private static class KotlinDelegate {

		static private boolean isSuspend(Method method) {
			KFunction<?> function = ReflectJvmMapping.getKotlinFunction(method);
			return function != null && function.isSuspend();
		}
	}

	/**
	 * Opaque object used to hold transaction information for reactive methods.
	 */
	private static final class ReactiveTransactionInfo {

		/**
		 * 响应式事务管理器
		 */
		@Nullable
		private final ReactiveTransactionManager transactionManager;

		/**
		 * 事务属性
		 */
		@Nullable
		private final TransactionAttribute transactionAttribute;

		/**
		 * 切入点,切面
		 */
		private final String joinpointIdentification;

		/**
		 * 响应式事务
		 */
		@Nullable
		private ReactiveTransaction reactiveTransaction;

		public ReactiveTransactionInfo(@Nullable ReactiveTransactionManager transactionManager,
				@Nullable TransactionAttribute transactionAttribute,
				String joinpointIdentification) {

			this.transactionManager = transactionManager;
			this.transactionAttribute = transactionAttribute;
			this.joinpointIdentification = joinpointIdentification;
		}

		public ReactiveTransactionManager getTransactionManager() {
			Assert.state(this.transactionManager != null, "No ReactiveTransactionManager set");
			return this.transactionManager;
		}

		@Nullable
		public TransactionAttribute getTransactionAttribute() {
			return this.transactionAttribute;
		}

		/**
		 * Return a String representation of this joinpoint (usually a Method call) for use in
		 * logging.
		 */
		public String getJoinpointIdentification() {
			return this.joinpointIdentification;
		}

		public void newReactiveTransaction(@Nullable ReactiveTransaction transaction) {
			this.reactiveTransaction = transaction;
		}

		@Nullable
		public ReactiveTransaction getReactiveTransaction() {
			return this.reactiveTransaction;
		}

		@Override
		public String toString() {
			return (this.transactionAttribute != null ? this.transactionAttribute.toString()
					: "No transaction");
		}
	}

	/**
	 * Delegate for Reactor-based management of transactional methods with a reactive return type.
	 *
	 *
	 * 响应式事务处理类
	 */
	private class ReactiveTransactionSupport {

		private final ReactiveAdapter adapter;

		public ReactiveTransactionSupport(ReactiveAdapter adapter) {
			this.adapter = adapter;
		}

		/**
		 * 带有事务的情况下执行函数
		 */
		public Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,
				InvocationCallback invocation, @Nullable TransactionAttribute txAttr,
				ReactiveTransactionManager rtm) {

			String joinpointIdentification = methodIdentification(method, targetClass, txAttr);

			// Optimize for Mono
			if (Mono.class.isAssignableFrom(method.getReturnType())) {
				return TransactionContextManager.currentContext().flatMap(context ->
						createTransactionIfNecessary(rtm, txAttr, joinpointIdentification)
								.flatMap(it -> {
									try {
										// Need re-wrapping until we get hold of the exception through usingWhen.
										return Mono.<Object, ReactiveTransactionInfo>usingWhen(
												Mono.just(it),
												txInfo -> {
													try {
														return (Mono<?>) invocation
																.proceedWithInvocation();
													}
													catch (Throwable ex) {
														return Mono.error(ex);
													}
												},
												this::commitTransactionAfterReturning,
												(txInfo, err) -> Mono.empty(),
												this::commitTransactionAfterReturning)
												.onErrorResume(ex ->
														completeTransactionAfterThrowing(it, ex)
																.then(Mono.error(ex)));
									}
									catch (Throwable ex) {
										// target invocation exception
										return completeTransactionAfterThrowing(it, ex)
												.then(Mono.error(ex));
									}
								}))
						.subscriberContext(TransactionContextManager.getOrCreateContext())
						.subscriberContext(TransactionContextManager.getOrCreateContextHolder());
			}

			// Any other reactive type, typically a Flux
			return this.adapter
					.fromPublisher(TransactionContextManager.currentContext().flatMapMany(context ->
							createTransactionIfNecessary(rtm, txAttr, joinpointIdentification)
									.flatMapMany(it -> {
										try {
											// Need re-wrapping until we get hold of the exception through usingWhen.
											return Flux
													.usingWhen(
															Mono.just(it),
															txInfo -> {
																try {
																	return this.adapter.toPublisher(
																			invocation
																					.proceedWithInvocation());
																}
																catch (Throwable ex) {
																	return Mono.error(ex);
																}
															},
															this::commitTransactionAfterReturning,
															(txInfo, ex) -> Mono.empty(),
															this::commitTransactionAfterReturning)
													.onErrorResume(ex ->
															completeTransactionAfterThrowing(it, ex)
																	.then(Mono.error(ex)));
										}
										catch (Throwable ex) {
											// target invocation exception
											return completeTransactionAfterThrowing(it, ex)
													.then(Mono.error(ex));
										}
									}))
							.subscriberContext(TransactionContextManager.getOrCreateContext())
							.subscriberContext(
									TransactionContextManager.getOrCreateContextHolder()));
		}

		@SuppressWarnings("serial")
		private Mono<ReactiveTransactionInfo> createTransactionIfNecessary(
				ReactiveTransactionManager tm,
				@Nullable TransactionAttribute txAttr, final String joinpointIdentification) {

			// If no name specified, apply method identification as transaction name.
			if (txAttr != null && txAttr.getName() == null) {
				txAttr = new DelegatingTransactionAttribute(txAttr) {
					@Override
					public String getName() {
						return joinpointIdentification;
					}
				};
			}

			final TransactionAttribute attrToUse = txAttr;
			Mono<ReactiveTransaction> tx = (attrToUse != null ? tm.getReactiveTransaction(attrToUse)
					: Mono.empty());
			return tx.map(it -> prepareTransactionInfo(tm, attrToUse, joinpointIdentification, it))
					.switchIfEmpty(
							Mono.defer(() -> Mono.just(prepareTransactionInfo(tm, attrToUse,
									joinpointIdentification, null))));
		}

		private ReactiveTransactionInfo prepareTransactionInfo(
				@Nullable ReactiveTransactionManager tm,
				@Nullable TransactionAttribute txAttr, String joinpointIdentification,
				@Nullable ReactiveTransaction transaction) {

			ReactiveTransactionInfo txInfo = new ReactiveTransactionInfo(tm, txAttr,
					joinpointIdentification);
			if (txAttr != null) {
				// We need a transaction for this method...
				if (logger.isTraceEnabled()) {
					logger.trace("Getting transaction for [" + txInfo.getJoinpointIdentification()
							+ "]");
				}
				// The transaction manager will flag an error if an incompatible tx already exists.
				txInfo.newReactiveTransaction(transaction);
			}
			else {
				// The TransactionInfo.hasTransaction() method will return false. We created it only
				// to preserve the integrity of the ThreadLocal stack maintained in this class.
				if (logger.isTraceEnabled()) {
					logger.trace(
							"Don't need to create transaction for [" + joinpointIdentification +
									"]: This method isn't transactional.");
				}
			}

			return txInfo;
		}

		private Mono<Void> commitTransactionAfterReturning(
				@Nullable ReactiveTransactionInfo txInfo) {
			if (txInfo != null && txInfo.getReactiveTransaction() != null) {
				if (logger.isTraceEnabled()) {
					logger.trace(
							"Completing transaction for [" + txInfo.getJoinpointIdentification()
									+ "]");
				}
				return txInfo.getTransactionManager().commit(txInfo.getReactiveTransaction());
			}
			return Mono.empty();
		}

		private Mono<Void> completeTransactionAfterThrowing(
				@Nullable ReactiveTransactionInfo txInfo, Throwable ex) {
			if (txInfo != null && txInfo.getReactiveTransaction() != null) {
				if (logger.isTraceEnabled()) {
					logger.trace(
							"Completing transaction for [" + txInfo.getJoinpointIdentification() +
									"] after exception: " + ex);
				}
				if (txInfo.transactionAttribute != null && txInfo.transactionAttribute
						.rollbackOn(ex)) {
					return txInfo.getTransactionManager().rollback(txInfo.getReactiveTransaction())
							.onErrorMap(ex2 -> {
										logger.error(
												"Application exception overridden by rollback exception",
												ex);
										if (ex2 instanceof TransactionSystemException) {
											((TransactionSystemException) ex2).initApplicationException(ex);
										}
										return ex2;
									}
							);
				}
				else {
					// We don't roll back on this exception.
					// Will still roll back if TransactionStatus.isRollbackOnly() is true.
					return txInfo.getTransactionManager().commit(txInfo.getReactiveTransaction())
							.onErrorMap(ex2 -> {
										logger.error("Application exception overridden by commit exception",
												ex);
										if (ex2 instanceof TransactionSystemException) {
											((TransactionSystemException) ex2).initApplicationException(ex);
										}
										return ex2;
									}
							);
				}
			}
			return Mono.empty();
		}
	}

}
