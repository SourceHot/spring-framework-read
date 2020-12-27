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

package org.springframework.context.support;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.support.ResourceEditorRegistrar;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.LifecycleProcessor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.context.weaving.LoadTimeWeaverAware;
import org.springframework.context.weaving.LoadTimeWeaverAwareProcessor;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Abstract implementation of the {@link org.springframework.context.ApplicationContext}
 * interface. Doesn't mandate the type of storage used for configuration; simply
 * implements common context functionality. Uses the Template Method design pattern,
 * requiring concrete subclasses to implement abstract methods.
 *
 * <p>In contrast to a plain BeanFactory, an ApplicationContext is supposed
 * to detect special beans defined in its internal bean factory:
 * Therefore, this class automatically registers
 * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessors},
 * {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessors},
 * and {@link org.springframework.context.ApplicationListener ApplicationListeners}
 * which are defined as beans in the context.
 *
 * <p>A {@link org.springframework.context.MessageSource} may also be supplied
 * as a bean in the context, with the name "messageSource"; otherwise, message
 * resolution is delegated to the parent context. Furthermore, a multicaster
 * for application events can be supplied as an "applicationEventMulticaster" bean
 * of type {@link org.springframework.context.event.ApplicationEventMulticaster}
 * in the context; otherwise, a default multicaster of type
 * {@link org.springframework.context.event.SimpleApplicationEventMulticaster} will be used.
 *
 * <p>Implements resource loading by extending
 * {@link org.springframework.core.io.DefaultResourceLoader}.
 * Consequently treats non-URL resource paths as class path resources
 * (supporting full class path resource names that include the package path,
 * e.g. "mypackage/myresource.dat"), unless the {@link #getResourceByPath}
 * method is overridden in a subclass.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since January 21, 2001
 * @see #refreshBeanFactory
 * @see #getBeanFactory
 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.event.ApplicationEventMulticaster
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.MessageSource
 */
public abstract class AbstractApplicationContext extends DefaultResourceLoader
        implements ConfigurableApplicationContext {

    /**
     * Name of the MessageSource bean in the factory.
     * If none is supplied, message resolution is delegated to the parent.
	 * MessageSource 的名字
     * @see MessageSource
     */
    public static final String MESSAGE_SOURCE_BEAN_NAME = "messageSource";

    /**
     * Name of the LifecycleProcessor bean in the factory.
     * If none is supplied, a DefaultLifecycleProcessor is used.
	 *
	 * LifecycleProcessor 的名字
     * @see org.springframework.context.LifecycleProcessor
     * @see org.springframework.context.support.DefaultLifecycleProcessor
     */
    public static final String LIFECYCLE_PROCESSOR_BEAN_NAME = "lifecycleProcessor";

    /**
     * Name of the ApplicationEventMulticaster bean in the factory.
     * If none is supplied, a default SimpleApplicationEventMulticaster is used.
	 *
	 * ApplicationEventMulticaster 的名称
	 * 事件传播器
     * @see org.springframework.context.event.ApplicationEventMulticaster
     * @see org.springframework.context.event.SimpleApplicationEventMulticaster
     */
    public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";

    static {
        // Eagerly load the ContextClosedEvent class to avoid weird classloader issues
        // on application shutdown in WebLogic 8.1. (Reported by Dustin Woods.)
        ContextClosedEvent.class.getName();
    }


    /** Logger used by this class. Available to subclasses. */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
	 * BeanFactoryPostProcessors to apply on refresh.
	 * bean 后置处理器列表
	 * */
    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new ArrayList<>();

    /**
	 * Flag that indicates whether this context is currently active.
	 * 是否存活状态的标记
	 * */
    private final AtomicBoolean active = new AtomicBoolean();

    /**
	 * Flag that indicates whether this context has been closed already.
	 * 是否关闭的标记
	 * */
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
	 *  Synchronization monitor for the "refresh" and "destroy".
	 *
	 * refresh 方法和 destroy 方法的锁
	 * */
    private final Object startupShutdownMonitor = new Object();

    /**
	 *  Statically specified listeners.
	 *
	 *  应用监听器列表
	 *  */
    private final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();

    /**
	 *  Unique id for this context, if any.
	 * 上下文id
	 * */
    private String id = ObjectUtils.identityToString(this);

    /**
	 *  Display name.
	 * 上下文名称
	 * */
    private String displayName = ObjectUtils.identityToString(this);

    /**
	 *  Parent context.
	 *  父上下文
	 *  */
    @Nullable
    private ApplicationContext parent;

    /**
	 * Environment used by this context.
	 * 环境配置
	 * */
    @Nullable
    private ConfigurableEnvironment environment;

    /**
	 * System time in milliseconds when this context started.
	 *
	 * 启动时间
	 * */
    private long startupDate;

    /**
	 * Reference to the JVM shutdown hook, if registered.
	 * 关闭的钩子线程
	 * */
    @Nullable
    private Thread shutdownHook;

    /**
	 *  ResourcePatternResolver used by this context.
	 *  资源解析器
	 *  */
    private final ResourcePatternResolver resourcePatternResolver;

    /**
	 * LifecycleProcessor for managing the lifecycle of beans within this context.
	 *
	 * 生命周期处理器
	 * */
    @Nullable
    private LifecycleProcessor lifecycleProcessor;

    /**
	 *  MessageSource we delegate our implementation of this interface to.
	 * 消息源 主要用于国际化
	 * */
    @Nullable
    private MessageSource messageSource;

    /**
	 * Helper class used in event publishing.
	 * 事件传播器
	 * */
    @Nullable
    private ApplicationEventMulticaster applicationEventMulticaster;

    /**
	 * Local listeners registered before refresh.
	 * 应用监听器列表
	 * */
    @Nullable
    private Set<ApplicationListener<?>> earlyApplicationListeners;

    /**
	 *  ApplicationEvents published before the multicaster setup.
	 * 需要提前发布的应用事件
	 * */
    @Nullable
    private Set<ApplicationEvent> earlyApplicationEvents;


    /**
     * Create a new AbstractApplicationContext with no parent.
     */
    public AbstractApplicationContext() {
        this.resourcePatternResolver = getResourcePatternResolver();
    }

    /**
     * Create a new AbstractApplicationContext with the given parent context.
     * @param parent the parent context
     */
    public AbstractApplicationContext(@Nullable ApplicationContext parent) {
        this();
        setParent(parent);
    }


    //---------------------------------------------------------------------
    // Implementation of ApplicationContext interface
    //---------------------------------------------------------------------

    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Set the unique id of this application context.
     * <p>Default is the object id of the context instance, or the name
     * of the context bean if the context is itself defined as a bean.
     * @param id the unique id of the context
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getApplicationName() {
        return "";
    }

    /**
     * Return a friendly name for this context.
     * @return a display name for this context (never {@code null})
     */
    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Set a friendly name for this context.
     * Typically done during initialization of concrete context implementations.
     * <p>Default is the object id of the context instance.
     */
    public void setDisplayName(String displayName) {
        Assert.hasLength(displayName, "Display name must not be empty");
        this.displayName = displayName;
    }

    /**
     * Return the parent context, or {@code null} if there is no parent
     * (that is, this context is the root of the context hierarchy).
     */
    @Override
    @Nullable
    public ApplicationContext getParent() {
        return this.parent;
    }

    /**
     * Set the parent of this application context.
     * <p>The parent {@linkplain ApplicationContext#getEnvironment() environment} is
     * {@linkplain ConfigurableEnvironment#merge(ConfigurableEnvironment) merged} with
     * this (child) application context environment if the parent is non-{@code null} and
     * its environment is an instance of {@link ConfigurableEnvironment}.
     * @see ConfigurableEnvironment#merge(ConfigurableEnvironment)
     */
    @Override
    public void setParent(@Nullable ApplicationContext parent) {
        this.parent = parent;
        if (parent != null) {
        	// 获取 父上下文的环境信息
            Environment parentEnvironment = parent.getEnvironment();
            // 当环境信息是ConfigurableEnvironment 进行合并
            if (parentEnvironment instanceof ConfigurableEnvironment) {
                getEnvironment().merge((ConfigurableEnvironment) parentEnvironment);
            }
        }
    }

    /**
     * Return the {@code Environment} for this application context in configurable
     * form, allowing for further customization.
     * <p>If none specified, a default environment will be initialized via
     * {@link #createEnvironment()}.
     */
    @Override
    public ConfigurableEnvironment getEnvironment() {
        if (this.environment == null) {
            this.environment = createEnvironment();
        }
        return this.environment;
    }

    /**
     * Set the {@code Environment} for this application context.
     * <p>Default value is determined by {@link #createEnvironment()}. Replacing the
     * default with this method is one option but configuration through {@link
     * #getEnvironment()} should also be considered. In either case, such modifications
     * should be performed <em>before</em> {@link #refresh()}.
     * @see org.springframework.context.support.AbstractApplicationContext#createEnvironment
     */
    @Override
    public void setEnvironment(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    /**
     * Create and return a new {@link StandardEnvironment}.
     * <p>Subclasses may override this method in order to supply
     * a custom {@link ConfigurableEnvironment} implementation.
     */
    protected ConfigurableEnvironment createEnvironment() {
        return new StandardEnvironment();
    }

    /**
     * Return this context's internal bean factory as AutowireCapableBeanFactory,
     * if already available.
     * @see #getBeanFactory()
     */
    @Override
    public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
        return getBeanFactory();
    }

    /**
     * Return the timestamp (ms) when this context was first loaded.
     */
    @Override
    public long getStartupDate() {
        return this.startupDate;
    }

    /**
     * Publish the given event to all listeners.
     * <p>Note: Listeners get initialized after the MessageSource, to be able
     * to access it within listener implementations. Thus, MessageSource
     * implementations cannot publish events.
     * @param event the event to publish (may be application-specific or a
     * standard framework event)
     */
    @Override
    public void publishEvent(ApplicationEvent event) {
        publishEvent(event, null);
    }

    /**
     * Publish the given event to all listeners.
     * <p>Note: Listeners get initialized after the MessageSource, to be able
     * to access it within listener implementations. Thus, MessageSource
     * implementations cannot publish events.
     * @param event the event to publish (may be an {@link ApplicationEvent}
     * or a payload object to be turned into a {@link PayloadApplicationEvent})
     */
    @Override
    public void publishEvent(Object event) {
        publishEvent(event, null);
    }

    /**
     * Publish the given event to all listeners.
	 *
	 * 推送事件
     * @param event the event to publish (may be an {@link ApplicationEvent}
     * or a payload object to be turned into a {@link PayloadApplicationEvent})
     * @param eventType the resolved event type, if known
     * @since 4.2
     */
    protected void publishEvent(Object event, @Nullable ResolvableType eventType) {
        Assert.notNull(event, "Event must not be null");

        // Decorate event as an ApplicationEvent if necessary
		// 应用事件对象
        ApplicationEvent applicationEvent;
        if (event instanceof ApplicationEvent) {
            applicationEvent = (ApplicationEvent) event;
        }
        else {
        	// 应用实践对象的封装
            applicationEvent = new PayloadApplicationEvent<>(this, event);
            if (eventType == null) {
            	// 事件类型
                eventType = ((PayloadApplicationEvent<?>) applicationEvent).getResolvableType();
            }
        }

        // Multicast right now if possible - or lazily once the multicaster is initialized
        if (this.earlyApplicationEvents != null) {
        	// 需要传播的事件列表中加入数据
            this.earlyApplicationEvents.add(applicationEvent);
        }
        else {
        	// 传播器传播事件
            getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
        }

        // 父上下存在的情况下依靠父上下文进行事件推送
        // Publish event via parent context as well...
        if (this.parent != null) {
            if (this.parent instanceof AbstractApplicationContext) {
            	// 发布事件
                ((AbstractApplicationContext) this.parent).publishEvent(event, eventType);
            }
            else {
				// 发布事件
                this.parent.publishEvent(event);
            }
        }
    }

    /**
     * Return the internal ApplicationEventMulticaster used by the context.
     * @return the internal ApplicationEventMulticaster (never {@code null})
     * @throws IllegalStateException if the context has not been initialized yet
     */
    ApplicationEventMulticaster getApplicationEventMulticaster() throws IllegalStateException {
        if (this.applicationEventMulticaster == null) {
            throw new IllegalStateException("ApplicationEventMulticaster not initialized - " +
                    "call 'refresh' before multicasting events via the context: " + this);
        }
        return this.applicationEventMulticaster;
    }

    /**
     * Return the internal LifecycleProcessor used by the context.
     * @return the internal LifecycleProcessor (never {@code null})
     * @throws IllegalStateException if the context has not been initialized yet
     */
    LifecycleProcessor getLifecycleProcessor() throws IllegalStateException {
        if (this.lifecycleProcessor == null) {
            throw new IllegalStateException("LifecycleProcessor not initialized - " +
                    "call 'refresh' before invoking lifecycle methods via the context: " + this);
        }
        return this.lifecycleProcessor;
    }


    //---------------------------------------------------------------------
    // Implementation of ConfigurableApplicationContext interface
    //---------------------------------------------------------------------

    /**
     * Return the ResourcePatternResolver to use for resolving location patterns
     * into Resource instances. Default is a
     * {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver},
     * supporting Ant-style location patterns.
     * <p>Can be overridden in subclasses, for extended resolution strategies,
     * for example in a web environment.
     * <p><b>Do not call this when needing to resolve a location pattern.</b>
     * Call the context's {@code getResources} method instead, which
     * will delegate to the ResourcePatternResolver.
     * @return the ResourcePatternResolver for this context
     * @see #getResources
     * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
     */
    protected ResourcePatternResolver getResourcePatternResolver() {
        return new PathMatchingResourcePatternResolver(this);
    }

    @Override
    public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor) {
        Assert.notNull(postProcessor, "BeanFactoryPostProcessor must not be null");
        this.beanFactoryPostProcessors.add(postProcessor);
    }

    /**
     * Return the list of BeanFactoryPostProcessors that will get applied
     * to the internal BeanFactory.
     */
    public List<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
        return this.beanFactoryPostProcessors;
    }

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        Assert.notNull(listener, "ApplicationListener must not be null");
        if (this.applicationEventMulticaster != null) {
            this.applicationEventMulticaster.addApplicationListener(listener);
        }
        this.applicationListeners.add(listener);
    }

    /**
     * Return the list of statically specified ApplicationListeners.
     */
    public Collection<ApplicationListener<?>> getApplicationListeners() {
        return this.applicationListeners;
    }

    @Override
    public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
            // Prepare this context for refreshing.
            // 准备刷新此上下文。
            prepareRefresh();

			// 创建出 beanFactory
            // Tell the subclass to refresh the internal bean factory.
            ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

            // Prepare the bean factory for use in this context.
			// 准备 beanFactory , 对 beanFactory 进行设置数据等
            prepareBeanFactory(beanFactory);

            try {
				// beanFactory 在子类中进行后置处理
                // Allows post-processing of the bean factory in context subclasses.
                postProcessBeanFactory(beanFactory);

                // BeanFactoryPostProcessor 方法调用
                // Invoke factory processors registered as beans in the context.
                invokeBeanFactoryPostProcessors(beanFactory);

                // 注册 beanPostProcessor
                // Register bean processors that intercept bean creation.
                registerBeanPostProcessors(beanFactory);

                // 实例化 message source 相关信息
                // Initialize message source for this context.
                initMessageSource();

                // 实例化 应用事件传播器
                // Initialize event multicaster for this context.
                initApplicationEventMulticaster();

                // Initialize other special beans in specific context subclasses.
                onRefresh();

                // Check for listener beans and register them.
				// 注册监听器
                registerListeners();

                // Instantiate all remaining (non-lazy-init) singletons.
				// 完成 beanFactory 的实例化
                finishBeanFactoryInitialization(beanFactory);

                // Last step: publish corresponding event.
				// 完成刷新
                finishRefresh();
            }

            catch (BeansException ex) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Exception encountered during context initialization - " +
                            "cancelling refresh attempt: " + ex);
                }

                // Destroy already created singletons to avoid dangling resources.
				// 摧毁bean
                destroyBeans();

                // Reset 'active' flag.
				// 取消刷新
                cancelRefresh(ex);

                // Propagate exception to caller.
                throw ex;
            }

            finally {
                // Reset common introspection caches in Spring's core, since we
                // might not ever need metadata for singleton beans anymore...
				// 重置通用缓存
                resetCommonCaches();
            }
        }
    }

    /**
     * Prepare this context for refreshing, setting its startup date and
     * active flag as well as performing any initialization of property sources.
     *
     * 准备刷新此上下文
     */
    protected void prepareRefresh() {
        // Switch to active.
        // 设置开始时间
        this.startupDate = System.currentTimeMillis();
        // 设置关闭标记位 false
        this.closed.set(false);
        // 设置激活标记位 true
        this.active.set(true);

        // 日志
        if (logger.isDebugEnabled()) {
            if (logger.isTraceEnabled()) {
                logger.trace("Refreshing " + this);
            }
            else {
                logger.debug("Refreshing " + getDisplayName());
            }
        }

        // 初始化属性, 占位符资源等数据处理
        // 抽象方法, 子类实现
        // Initialize any placeholder property sources in the context environment.
        initPropertySources();

        // 进行数据必填性验证
        // Validate that all properties marked as required are resolvable:
        // see ConfigurablePropertyResolver#setRequiredProperties
        getEnvironment().validateRequiredProperties();

        // 处理早期应用监听器列表 和 应用监听器列表
        // Store pre-refresh ApplicationListeners...
        if (this.earlyApplicationListeners == null) {
            this.earlyApplicationListeners = new LinkedHashSet<>(this.applicationListeners);
        }
        else {
            // Reset local application listeners to pre-refresh state.
            this.applicationListeners.clear();
            this.applicationListeners.addAll(this.earlyApplicationListeners);
        }

        // Allow for the collection of early ApplicationEvents,
        // to be published once the multicaster is available...
        this.earlyApplicationEvents = new LinkedHashSet<>();
    }
    /**
     * <p>Replace any stub property sources with actual instances.
     * @see org.springframework.core.env.PropertySource.StubPropertySource
     * @see org.springframework.web.context.support.WebApplicationContextUtils#initServletPropertySources
     */
    protected void initPropertySources() {
        // For subclasses: do nothing by default.
    }

    /**
     * Tell the subclass to refresh the internal bean factory.
     * @return the fresh BeanFactory instance
     * @see #refreshBeanFactory()
     * @see #getBeanFactory()
     */
    protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
        // 刷新 beanFactory , 子类实现
        refreshBeanFactory();
        // 获取 beanFactory , 子类实现
        return getBeanFactory();
    }

    /**
     * Configure the factory's standard context characteristics,
     * such as the context's ClassLoader and post-processors.
     * @param beanFactory the BeanFactory to configure
     */
    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // Tell the internal bean factory to use the context's class loader etc.
        // 设置 classLaoder
        beanFactory.setBeanClassLoader(getClassLoader());
        // 设置 el 表达式解析器
        beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
        // 添加属性编辑器注册工具
        beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

        // Configure the bean factory with context callbacks.
        // 添加 bean 后置处理器
        beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
        // 添加忽略的接口
        beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
        beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
        beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
        beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
        beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
        beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

        // 注册依赖
        // BeanFactory interface not registered as resolvable type in a plain factory.
        // MessageSource registered (and found for autowiring) as a bean.
        beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
        beanFactory.registerResolvableDependency(ResourceLoader.class, this);
        beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
        beanFactory.registerResolvableDependency(ApplicationContext.class, this);

        // 添加 bean 后置处理器
        // Register early post-processor for detecting inner beans as ApplicationListeners.
        beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

        // 判断是否存在 loadTimeWeaver bean
        // Detect a LoadTimeWeaver and prepare for weaving, if found.
        if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
            // 添加后置处理器
            beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
            // Set a temporary ClassLoader for type matching.
            // 设置临时的 classLoader
            beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
        }

        // environment bean 注册
        // Register default environment beans.
        if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
            beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
        }
        // systemProperties bean 注册
        if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
            beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
        }
        // systemEnvironment bean 注册
        if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
            beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
        }
    }

    /**
     * Modify the application context's internal bean factory after its standard
     * initialization. All bean definitions will have been loaded, but no beans
     * will have been instantiated yet. This allows for registering special
     * BeanPostProcessors etc in certain ApplicationContext implementations.
     * @param beanFactory the bean factory used by the application context
     */
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    }

    /**
     * Instantiate and invoke all registered BeanFactoryPostProcessor beans,
     * respecting explicit order if given.
     * <p>Must be called before singleton instantiation.
     */
    protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        // 后置处理器委托对象
        // 调用 BeanFactoryPostProcessor 方法
        PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());

        // Detect a LoadTimeWeaver and prepare for weaving, if found in the meantime
        // (e.g. through an @Bean method registered by ConfigurationClassPostProcessor)
        // 判断临时类加载器是否存在
        // 是否包含 loadTimeWeaver bean
        if (beanFactory.getTempClassLoader() == null && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
            // 添加 bean后置处理器
            beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
            // 添加临时类加载器
            beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
        }
    }

    /**
     * Instantiate and register all BeanPostProcessor beans,
     * respecting explicit order if given.
     * <p>Must be called before any instantiation of application beans.
     */
    protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        // 后置处理器委托类进行能注册
        PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
    }

    /**
     * Initialize the MessageSource.
     * Use parent's if none defined in this context.
     */
    protected void initMessageSource() {
        // 获取 beanFactory
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();
        // 判断容器中是否存在 messageSource 这个beanName
        // 存在的情况
        if (beanFactory.containsLocalBean(MESSAGE_SOURCE_BEAN_NAME)) {
            // 获取 messageSource 对象
            this.messageSource = beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME, MessageSource.class);

            // 设置 父 MessageSource
            // Make MessageSource aware of parent MessageSource.
            if (this.parent != null && this.messageSource instanceof HierarchicalMessageSource) {
                HierarchicalMessageSource hms = (HierarchicalMessageSource) this.messageSource;
                if (hms.getParentMessageSource() == null) {
                    // Only set parent context as parent MessageSource if no parent MessageSource
                    // registered already.
                    hms.setParentMessageSource(getInternalParentMessageSource());
                }
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Using MessageSource [" + this.messageSource + "]");
            }
        }
        // 不存在的情况
        else {
            //  MessageSource 实现类
            // Use empty MessageSource to be able to accept getMessage calls.
            DelegatingMessageSource dms = new DelegatingMessageSource();
            // 设置父 MessageSource
            dms.setParentMessageSource(getInternalParentMessageSource());
            this.messageSource = dms;
            // 注册 MessageSource
            beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
            if (logger.isTraceEnabled()) {
                logger.trace("No '" + MESSAGE_SOURCE_BEAN_NAME + "' bean, using [" + this.messageSource + "]");
            }
        }
    }

    /**
     * Initialize the ApplicationEventMulticaster.
     * Uses SimpleApplicationEventMulticaster if none defined in the context.
     * 实例化 应用事件传播器
     * @see org.springframework.context.event.SimpleApplicationEventMulticaster
     */
    protected void initApplicationEventMulticaster() {
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();
        // 存在的情况
        if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
            this.applicationEventMulticaster =
                    beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
            if (logger.isTraceEnabled()) {
                logger.trace("Using ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
            }
        }
        // 不存在的情况
        else {
            this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
            // 注册到容器
            beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
            if (logger.isTraceEnabled()) {
                logger.trace("No '" + APPLICATION_EVENT_MULTICASTER_BEAN_NAME + "' bean, using " +
                        "[" + this.applicationEventMulticaster.getClass().getSimpleName() + "]");
            }
        }
    }

    /**
     * Initialize the LifecycleProcessor.
     * Uses DefaultLifecycleProcessor if none defined in the context.
     *
     * 实例化生命周期处理接口
     * @see org.springframework.context.support.DefaultLifecycleProcessor
     */
    protected void initLifecycleProcessor() {
        // 获取 beanFactory
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();
        // 判断 lifecycleProcessor beanName 是否有对应的 bean 实例
        if (beanFactory.containsLocalBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
            // 设置 lifecycleProcessor
            this.lifecycleProcessor =
                    beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
            if (logger.isTraceEnabled()) {
                logger.trace("Using LifecycleProcessor [" + this.lifecycleProcessor + "]");
            }
        }
        else {
            // 创建默认的 生命周期处理接口的实现都西昂
            DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
            // 设置 beanFactory
            defaultProcessor.setBeanFactory(beanFactory);
            // 设置成员变量
            this.lifecycleProcessor = defaultProcessor;
            // 注册
            beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, this.lifecycleProcessor);
            if (logger.isTraceEnabled()) {
                logger.trace("No '" + LIFECYCLE_PROCESSOR_BEAN_NAME + "' bean, using " +
                        "[" + this.lifecycleProcessor.getClass().getSimpleName() + "]");
            }
        }
    }

    /**
     * Template method which can be overridden to add context-specific refresh work.
     * Called on initialization of special beans, before instantiation of singletons.
     * <p>This implementation is empty.
     * @throws BeansException in case of errors
     * @see #refresh()
     */
    protected void onRefresh() throws BeansException {
        // For subclasses: do nothing by default.
    }

    /**
     * Add beans that implement ApplicationListener as listeners.
     * Doesn't affect other listeners, which can be added without being beans.
     */
    protected void registerListeners() {
        // 获取 应用监听器列表
        // Register statically specified listeners first.
        for (ApplicationListener<?> listener : getApplicationListeners()) {
            // 获取事件广播器
            // 添加应用监听器
            getApplicationEventMulticaster().addApplicationListener(listener);
        }

        // 通过类型获取 应用监听器名称列表
        // Do not initialize FactoryBeans here: We need to leave all regular beans
        // uninitialized to let post-processors apply to them!
        String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
        // 将 应用监听器列表的名称注册到 事件广播器中
        for (String listenerBeanName : listenerBeanNames) {
            getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
        }

        // 早期应用事件发布
        // Publish early application events now that we finally have a multicaster...
        Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
        this.earlyApplicationEvents = null;
        if (earlyEventsToProcess != null) {
            for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
                // 发布事件
                getApplicationEventMulticaster().multicastEvent(earlyEvent);
            }
        }
    }

    /**
     * Finish the initialization of this context's bean factory,
     * initializing all remaining singleton beans.
     */
    protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
        // 判断是否存在转换服务
        //  1. 转换服务的beanName存在
        //  2. 转换服务的beanName 和 类型是否匹配
        // Initialize conversion service for this context.
        if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
                beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
            // 注册转换服务
            beanFactory.setConversionService(
                    beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
        }

        // 添加嵌套值解析器, 字符串解析其
        // Register a default embedded value resolver if no bean post-processor
        // (such as a PropertyPlaceholderConfigurer bean) registered any before:
        // at this point, primarily for resolution in annotation attribute values.
        if (!beanFactory.hasEmbeddedValueResolver()) {
            beanFactory.addEmbeddedValueResolver(strVal -> getEnvironment().resolvePlaceholders(strVal));
        }

        // 将类型是 LoadTimeWeaverAware 的bean全部初始化
        // Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
        String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
        for (String weaverAwareName : weaverAwareNames) {
            getBean(weaverAwareName);
        }

        // 删除临时类加载器
        // Stop using the temporary ClassLoader for type matching.
        beanFactory.setTempClassLoader(null);

        // 冻结部分配置
        // Allow for caching all bean definition metadata, not expecting further changes.
        beanFactory.freezeConfiguration();

        // 非懒加载的单例对象实例化
        // Instantiate all remaining (non-lazy-init) singletons.
        beanFactory.preInstantiateSingletons();
    }

    /**
     * Finish the refresh of this context, invoking the LifecycleProcessor's
     * onRefresh() method and publishing the
     * {@link org.springframework.context.event.ContextRefreshedEvent}.
     */
    protected void finishRefresh() {
        // 清空资源缓存
        // Clear context-level resource caches (such as ASM metadata from scanning).
        clearResourceCaches();

        // 实例化生命周期处理接口
        // Initialize lifecycle processor for this context.
        initLifecycleProcessor();

        // 生命周期处理接口进行刷新操作
        // Propagate refresh to lifecycle processor first.
        getLifecycleProcessor().onRefresh();

        // 推送事件: 上下文刷新事件
        // Publish the final event.
        publishEvent(new ContextRefreshedEvent(this));

        // 注册应用上下文
        // Participate in LiveBeansView MBean, if active.
        LiveBeansView.registerApplicationContext(this);
    }

    /**
     * Cancel this context's refresh attempt, resetting the {@code active} flag
     * after an exception got thrown.
     * @param ex the exception that led to the cancellation
     */
    protected void cancelRefresh(BeansException ex) {
        this.active.set(false);
    }

    /**
     * Reset Spring's common reflection metadata caches, in particular the
     * {@link ReflectionUtils}, {@link AnnotationUtils}, {@link ResolvableType}
     * and {@link CachedIntrospectionResults} caches.
     * @since 4.2
     * @see ReflectionUtils#clearCache()
     * @see AnnotationUtils#clearCache()
     * @see ResolvableType#clearCache()
     * @see CachedIntrospectionResults#clearClassLoader(ClassLoader)
     */
    protected void resetCommonCaches() {
        // 反射缓存
        ReflectionUtils.clearCache();
        // 注解缓存
        AnnotationUtils.clearCache();
        // 类型解析器缓存
        ResolvableType.clearCache();
        CachedIntrospectionResults.clearClassLoader(getClassLoader());
    }


    /**
     * Register a shutdown hook {@linkplain Thread#getName() named}
     * {@code SpringContextShutdownHook} with the JVM runtime, closing this
     * context on JVM shutdown unless it has already been closed at that time.
     * <p>Delegates to {@code doClose()} for the actual closing procedure.
     * @see Runtime#addShutdownHook
     * @see ConfigurableApplicationContext#SHUTDOWN_HOOK_THREAD_NAME
     * @see #close()
     * @see #doClose()
     */
    @Override
    public void registerShutdownHook() {
        if (this.shutdownHook == null) {
            // No shutdown hook registered yet.
            this.shutdownHook = new Thread(SHUTDOWN_HOOK_THREAD_NAME) {
                @Override
                public void run() {
                    synchronized (startupShutdownMonitor) {
                        // 真正的关闭方法
                        doClose();
                    }
                }
            };
            // jdk 提供的关闭应用时触发的钩子线程
            Runtime.getRuntime().addShutdownHook(this.shutdownHook);
        }
    }

    /**
     * Callback for destruction of this instance, originally attached
     * to a {@code DisposableBean} implementation (not anymore in 5.0).
     * <p>The {@link #close()} method is the native way to shut down
     * an ApplicationContext, which this method simply delegates to.
     * @deprecated as of Spring Framework 5.0, in favor of {@link #close()}
     */
    @Deprecated
    public void destroy() {
        close();
    }

    /**
     * Close this application context, destroying all beans in its bean factory.
     * <p>Delegates to {@code doClose()} for the actual closing procedure.
     * Also removes a JVM shutdown hook, if registered, as it's not needed anymore.
     * @see #doClose()
     * @see #registerShutdownHook()
     */
    @Override
    public void close() {
        synchronized (this.startupShutdownMonitor) {
            // 执行关闭
            doClose();
            // If we registered a JVM shutdown hook, we don't need it anymore now:
            // We've already explicitly closed the context.
            if (this.shutdownHook != null) {
                try {
                    // 移除 关闭线程
                    Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
                }
                catch (IllegalStateException ex) {
                    // ignore - VM is already shutting down
                }
            }
        }
    }

    /**
     * Actually performs context closing: publishes a ContextClosedEvent and
     * destroys the singletons in the bean factory of this application context.
     * <p>Called by both {@code close()} and a JVM shutdown hook, if any.
     * @see org.springframework.context.event.ContextClosedEvent
     * @see #destroyBeans()
     * @see #close()
     * @see #registerShutdownHook()
     */
    protected void doClose() {
        // Check whether an actual close attempt is necessary...
        if (this.active.get() && this.closed.compareAndSet(false, true)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Closing " + this);
            }

            // 在容器中移除当前上下文
            LiveBeansView.unregisterApplicationContext(this);

            try {
                // 发布关闭上下文事件
                // Publish shutdown event.
                publishEvent(new ContextClosedEvent(this));
            }
            catch (Throwable ex) {
                logger.warn("Exception thrown from ApplicationListener handling ContextClosedEvent", ex);
            }

            // Stop all Lifecycle beans, to avoid delays during individual destruction.
            if (this.lifecycleProcessor != null) {
                try {
                    // 生命周期处理器执行关闭函数
                    this.lifecycleProcessor.onClose();
                }
                catch (Throwable ex) {
                    logger.warn("Exception thrown from LifecycleProcessor on context close", ex);
                }
            }

            // 摧毁bean
            // Destroy all cached singletons in the context's BeanFactory.
            destroyBeans();

            // 关闭 beanFactory
            // Close the state of this context itself.
            closeBeanFactory();

            // Let subclasses do some final clean-up if they wish...
            // 子类拓展关闭相关方法
            onClose();

            // Reset local application listeners to pre-refresh state.
            if (this.earlyApplicationListeners != null) {
                this.applicationListeners.clear();
                this.applicationListeners.addAll(this.earlyApplicationListeners);
            }

            // 设置激活状态为 false
            // Switch to inactive.
            this.active.set(false);
        }
    }

    /**
     * Template method for destroying all beans that this context manages.
     * The default implementation destroy all cached singletons in this context,
     * invoking {@code DisposableBean.destroy()} and/or the specified
     * "destroy-method".
     * <p>Can be overridden to add context-specific bean destruction steps
     * right before or right after standard singleton destruction,
     * while the context's BeanFactory is still active.
     * @see #getBeanFactory()
     * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#destroySingletons()
     */
    protected void destroyBeans() {
        getBeanFactory().destroySingletons();
    }

    /**
     * Template method which can be overridden to add context-specific shutdown work.
     * The default implementation is empty.
     * <p>Called at the end of {@link #doClose}'s shutdown procedure, after
     * this context's BeanFactory has been closed. If custom shutdown logic
     * needs to execute while the BeanFactory is still active, override
     * the {@link #destroyBeans()} method instead.
     */
    protected void onClose() {
        // For subclasses: do nothing by default.
    }

    @Override
    public boolean isActive() {
        return this.active.get();
    }

    /**
     * Assert that this context's BeanFactory is currently active,
     * throwing an {@link IllegalStateException} if it isn't.
     * <p>Invoked by all {@link BeanFactory} delegation methods that depend
     * on an active context, i.e. in particular all bean accessor methods.
     * <p>The default implementation checks the {@link #isActive() 'active'} status
     * of this context overall. May be overridden for more specific checks, or for a
     * no-op if {@link #getBeanFactory()} itself throws an exception in such a case.
     *
     *
     * 判断 beanFactory 是否存活
     */
    protected void assertBeanFactoryActive() {
        // 是否存活
        if (!this.active.get()) {
            // 是否关闭
            if (this.closed.get()) {
                throw new IllegalStateException(getDisplayName() + " has been closed already");
            }
            else {
                throw new IllegalStateException(getDisplayName() + " has not been refreshed yet");
            }
        }
    }


    //---------------------------------------------------------------------
    // Implementation of BeanFactory interface
    //---------------------------------------------------------------------

    @Override
    public Object getBean(String name) throws BeansException {
        assertBeanFactoryActive();
        return getBeanFactory().getBean(name);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        // 判断 beanFactory 是否存活
        assertBeanFactoryActive();

        // 1. 获取 beanFactory
        // 2. 根据 beanName + class 获取 Bean
        return getBeanFactory().getBean(name, requiredType);
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        assertBeanFactoryActive();
        return getBeanFactory().getBean(name, args);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        assertBeanFactoryActive();
        return getBeanFactory().getBean(requiredType);
    }

    @Override
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        assertBeanFactoryActive();
        return getBeanFactory().getBean(requiredType, args);
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
        assertBeanFactoryActive();
        return getBeanFactory().getBeanProvider(requiredType);
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
        assertBeanFactoryActive();
        return getBeanFactory().getBeanProvider(requiredType);
    }

    @Override
    public boolean containsBean(String name) {
        return getBeanFactory().containsBean(name);
    }

    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        assertBeanFactoryActive();
        return getBeanFactory().isSingleton(name);
    }

    @Override
    public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        assertBeanFactoryActive();
        return getBeanFactory().isPrototype(name);
    }

    @Override
    public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
        assertBeanFactoryActive();
        return getBeanFactory().isTypeMatch(name, typeToMatch);
    }

    @Override
    public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        assertBeanFactoryActive();
        return getBeanFactory().isTypeMatch(name, typeToMatch);
    }

    @Override
    @Nullable
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        assertBeanFactoryActive();
        return getBeanFactory().getType(name);
    }

    @Override
    @Nullable
    public Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        assertBeanFactoryActive();
        return getBeanFactory().getType(name, allowFactoryBeanInit);
    }

    @Override
    public String[] getAliases(String name) {
        return getBeanFactory().getAliases(name);
    }


    //---------------------------------------------------------------------
    // Implementation of ListableBeanFactory interface
    //---------------------------------------------------------------------

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return getBeanFactory().containsBeanDefinition(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return getBeanFactory().getBeanDefinitionCount();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return getBeanFactory().getBeanDefinitionNames();
    }

    @Override
    public String[] getBeanNamesForType(ResolvableType type) {
        assertBeanFactoryActive();
        return getBeanFactory().getBeanNamesForType(type);
    }

    @Override
    public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
        assertBeanFactoryActive();
        return getBeanFactory().getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
    }

    @Override
    public String[] getBeanNamesForType(@Nullable Class<?> type) {
        assertBeanFactoryActive();
        return getBeanFactory().getBeanNamesForType(type);
    }

    @Override
    public String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        assertBeanFactoryActive();
        return getBeanFactory().getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
        assertBeanFactoryActive();
        return getBeanFactory().getBeansOfType(type);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
            throws BeansException {

        assertBeanFactoryActive();
        return getBeanFactory().getBeansOfType(type, includeNonSingletons, allowEagerInit);
    }

    @Override
    public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
        assertBeanFactoryActive();
        return getBeanFactory().getBeanNamesForAnnotation(annotationType);
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType)
            throws BeansException {

        assertBeanFactoryActive();
        return getBeanFactory().getBeansWithAnnotation(annotationType);
    }

    @Override
    @Nullable
    public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
            throws NoSuchBeanDefinitionException {

        assertBeanFactoryActive();
        return getBeanFactory().findAnnotationOnBean(beanName, annotationType);
    }


    //---------------------------------------------------------------------
    // Implementation of HierarchicalBeanFactory interface
    //---------------------------------------------------------------------

    @Override
    @Nullable
    public BeanFactory getParentBeanFactory() {
        return getParent();
    }

    @Override
    public boolean containsLocalBean(String name) {
        return getBeanFactory().containsLocalBean(name);
    }

    /**
     * Return the internal bean factory of the parent context if it implements
     * ConfigurableApplicationContext; else, return the parent context itself.
     * @see org.springframework.context.ConfigurableApplicationContext#getBeanFactory
     */
    @Nullable
    protected BeanFactory getInternalParentBeanFactory() {
        return (getParent() instanceof ConfigurableApplicationContext ?
                ((ConfigurableApplicationContext) getParent()).getBeanFactory() : getParent());
    }


    //---------------------------------------------------------------------
    // Implementation of MessageSource interface
    //---------------------------------------------------------------------

    @Override
    public String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale) {
        return getMessageSource().getMessage(code, args, defaultMessage, locale);
    }

    @Override
    public String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException {
        return getMessageSource().getMessage(code, args, locale);
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return getMessageSource().getMessage(resolvable, locale);
    }

    /**
     * Return the internal MessageSource used by the context.
     * @return the internal MessageSource (never {@code null})
     * @throws IllegalStateException if the context has not been initialized yet
     */
    private MessageSource getMessageSource() throws IllegalStateException {
        if (this.messageSource == null) {
            throw new IllegalStateException("MessageSource not initialized - " +
                    "call 'refresh' before accessing messages via the context: " + this);
        }
        return this.messageSource;
    }

    /**
     * Return the internal message source of the parent context if it is an
     * AbstractApplicationContext too; else, return the parent context itself.
     */
    @Nullable
    protected MessageSource getInternalParentMessageSource() {
        return (getParent() instanceof AbstractApplicationContext ?
                ((AbstractApplicationContext) getParent()).messageSource : getParent());
    }


    //---------------------------------------------------------------------
    // Implementation of ResourcePatternResolver interface
    //---------------------------------------------------------------------

    @Override
    public Resource[] getResources(String locationPattern) throws IOException {
        return this.resourcePatternResolver.getResources(locationPattern);
    }


    //---------------------------------------------------------------------
    // Implementation of Lifecycle interface
    //---------------------------------------------------------------------

    @Override
    public void start() {
        getLifecycleProcessor().start();
        publishEvent(new ContextStartedEvent(this));
    }

    @Override
    public void stop() {
        getLifecycleProcessor().stop();
        publishEvent(new ContextStoppedEvent(this));
    }

    @Override
    public boolean isRunning() {
        return (this.lifecycleProcessor != null && this.lifecycleProcessor.isRunning());
    }


    //---------------------------------------------------------------------
    // Abstract methods that must be implemented by subclasses
    //---------------------------------------------------------------------

    /**
     * Subclasses must implement this method to perform the actual configuration load.
     * The method is invoked by {@link #refresh()} before any other initialization work.
     * <p>A subclass will either create a new bean factory and hold a reference to it,
     * or return a single BeanFactory instance that it holds. In the latter case, it will
     * usually throw an IllegalStateException if refreshing the context more than once.
     * @throws BeansException if initialization of the bean factory failed
     * @throws IllegalStateException if already initialized and multiple refresh
     * attempts are not supported
     */
    protected abstract void refreshBeanFactory() throws BeansException, IllegalStateException;

    /**
     * Subclasses must implement this method to release their internal bean factory.
     * This method gets invoked by {@link #close()} after all other shutdown work.
     * <p>Should never throw an exception but rather log shutdown failures.
     */
    protected abstract void closeBeanFactory();

    /**
     * Subclasses must return their internal bean factory here. They should implement the
     * lookup efficiently, so that it can be called repeatedly without a performance penalty.
     * <p>Note: Subclasses should check whether the context is still active before
     * returning the internal bean factory. The internal factory should generally be
     * considered unavailable once the context has been closed.
     * @return this application context's internal bean factory (never {@code null})
     * @throws IllegalStateException if the context does not hold an internal bean factory yet
     * (usually if {@link #refresh()} has never been called) or if the context has been
     * closed already
     * @see #refreshBeanFactory()
     * @see #closeBeanFactory()
     */
    @Override
    public abstract ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;


    /**
     * Return information about this context.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getDisplayName());
        sb.append(", started on ").append(new Date(getStartupDate()));
        ApplicationContext parent = getParent();
        if (parent != null) {
            sb.append(", parent: ").append(parent.getDisplayName());
        }
        return sb.toString();
    }

}
