# Spring Bean 生命周期
- Author: [HuiFer](https://github.com/huifer)
- 源码阅读仓库: [SourceHot-spring](https://github.com/SourceHot/spring-framework-read)
- 生命周期: 从无到有再到无


## demo
- bean
```java

/**
 * bean生命周期
 */
public class BeanLifeCycle
        implements DisposableBean,
        InitializingBean,
        BeanFactoryAware,
        BeanNameAware {
    private String name;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("设置 beanFactory");
    }

    @Override
    public void setBeanName(String name) {
        System.out.println("设置 beanName");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("销毁");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("设置属性后");
    }

    public void initMethod() {
        System.out.println("init method");
    }
    public void destroyMethod(){
        System.out.println("destroy Method");
    }

    public BeanLifeCycle() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

```

- xml配置
```xml
	<bean class="org.sourcehot.spring.bean.BeanLifeCycle" init-method="initMethod"
		  destroy-method="destroyMethod"
	>
	</bean>
```
- 执行方法
```java
/**
 * bean 生命周期
 */
public class BeanLifeCycleSourceCode {
    public static void main(String[] args) {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("BeanLifeCycleSourceCode.xml");
        context.close();

    }
}

```


- 上述内容为 Bean 初始化到销毁的一个过程,看一下执行结果

```text
设置 beanName
设置 beanFactory
in postProcessBeforeInitialization method 
设置属性后
init method
in postProcessAfterInitialization method 
销毁
destroy Method
```

## 解析
```java
    protected Object initializeBean(final String beanName, final Object bean, @Nullable RootBeanDefinition mbd) {
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                invokeAwareMethods(beanName, bean);
                return null;
            }, getAccessControlContext());
        }
        else {
            // 执行 aware 相关方法
            invokeAwareMethods(beanName, bean);
        }

        Object wrappedBean = bean;
        if (mbd == null || !mbd.isSynthetic()) {
            wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
        }

        try {
            // 执行 init method
            invokeInitMethods(beanName, wrappedBean, mbd);
        }
        catch (Throwable ex) {
            throw new BeanCreationException(
                    (mbd != null ? mbd.getResourceDescription() : null),
                    beanName, "Invocation of init method failed", ex);
        }
        if (mbd == null || !mbd.isSynthetic()) {
            wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
        }

        return wrappedBean;
    }

```

- invokeAwareMethods aware 系列接口执行
```java
    /**
     * 执行 aware 方法
     * @see BeanNameAware
     * @see BeanClassLoaderAware
     * @see BeanFactoryAware
     */
    private void invokeAwareMethods(final String beanName, final Object bean) {
        if (bean instanceof Aware) {
            // 是否实现 BeanNameAware
            if (bean instanceof BeanNameAware) {
                ((BeanNameAware) bean).setBeanName(beanName);
            }
            // 是否实现 BeanClassLoaderAware
            if (bean instanceof BeanClassLoaderAware) {
                ClassLoader bcl = getBeanClassLoader();
                if (bcl != null) {
                    ((BeanClassLoaderAware) bean).setBeanClassLoader(bcl);
                }
            }
            // 是否实现 BeanFactoryAware
            if (bean instanceof BeanFactoryAware) {
                ((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
            }
        }
    }

```

- invokeInitMethods init-method 方法执行
    - `org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.invokeInitMethods`
```java
    protected void invokeInitMethods(String beanName, final Object bean, @Nullable RootBeanDefinition mbd)
            throws Throwable {

        // 判断 bean 是否实现 InitializingBean
        boolean isInitializingBean = (bean instanceof InitializingBean);
        // 并且有 afterPropertiesSet 方法
        if (isInitializingBean && (mbd == null || !mbd.isExternallyManagedInitMethod("afterPropertiesSet"))) {
            if (logger.isTraceEnabled()) {
                logger.trace("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
            }
            if (System.getSecurityManager() != null) {
                try {
                    AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                        // 执行 InitializingBean.afterPropertiesSet()
                        ((InitializingBean) bean).afterPropertiesSet();
                        return null;
                    }, getAccessControlContext());
                }
                catch (PrivilegedActionException pae) {
                    throw pae.getException();
                }
            }
            else {
                // 执行 InitializingBean.afterPropertiesSet()
                ((InitializingBean) bean).afterPropertiesSet();
            }
        }

        if (mbd != null && bean.getClass() != NullBean.class) {
            // 是否有 init-method 属性
            String initMethodName = mbd.getInitMethodName();
            if (StringUtils.hasLength(initMethodName) &&
                    !(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) &&
                    !mbd.isExternallyManagedInitMethod(initMethodName)) {
                //  执行 init-method 方法
                invokeCustomInitMethod(beanName, bean, mbd);
            }
        }
    }

```
- 执行标签`<bean init-method=""/>`中的方法,通过反射执行
    - `org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.invokeCustomInitMethod`
```java
    protected void invokeCustomInitMethod(String beanName, final Object bean, RootBeanDefinition mbd)
            throws Throwable {

        // 获取 init-method 属性值
        String initMethodName = mbd.getInitMethodName();
        Assert.state(initMethodName != null, "No init method set");
        // 获取 init-method 对应的方法对象
        Method initMethod = (mbd.isNonPublicAccessAllowed() ?
                BeanUtils.findMethod(bean.getClass(), initMethodName) :
                ClassUtils.getMethodIfAvailable(bean.getClass(), initMethodName));

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
        // init-method 方法对象放射
        Method methodToInvoke = ClassUtils.getInterfaceMethodIfPossible(initMethod);

        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                ReflectionUtils.makeAccessible(methodToInvoke);
                return null;
            });
            try {
                // 执行 init-method 方法
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
                ReflectionUtils.makeAccessible(methodToInvoke);
                // 执行 init-method 方法
                methodToInvoke.invoke(bean);
            }
            catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }

```
- 初始化方法知道了,那么摧毁方法`destroy-method`应该差不多.
- 什么时候执行摧毁方法?答: 在容易关闭或摧毁时
- 通过`context.close();`将容器关闭,接下来进行`close`方法的分析

- `org.springframework.context.support.AbstractApplicationContext.close`
```java
    @Override
    public void close() {
        synchronized (this.startupShutdownMonitor) {
            // 关闭
            doClose();
            // If we registered a JVM shutdown hook, we don't need it anymore now:
            // We've already explicitly closed the context.
            if (this.shutdownHook != null) {
                try {
                    Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
                }
                catch (IllegalStateException ex) {
                    // ignore - VM is already shutting down
                }
            }
        }
    }

```
- `org.springframework.context.support.AbstractApplicationContext.doClose`
```java
    protected void doClose() {
        // Check whether an actual close attempt is necessary...
        if (this.active.get() && this.closed.compareAndSet(false, true)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Closing " + this);
            }

            LiveBeansView.unregisterApplicationContext(this);

            try {
                // Publish shutdown event.
                // 通知事件
                publishEvent(new ContextClosedEvent(this));
            }
            catch (Throwable ex) {
                logger.warn("Exception thrown from ApplicationListener handling ContextClosedEvent", ex);
            }

            // Stop all Lifecycle beans, to avoid delays during individual destruction.
            if (this.lifecycleProcessor != null) {
                try {
                    this.lifecycleProcessor.onClose();
                }
                catch (Throwable ex) {
                    logger.warn("Exception thrown from LifecycleProcessor on context close", ex);
                }
            }

            // Destroy all cached singletons in the context's BeanFactory.
            // 销毁bean
            destroyBeans();

            // Close the state of this context itself.
            // 关闭bean工厂
            closeBeanFactory();

            // Let subclasses do some final clean-up if they wish...
            // 空实现
            onClose();

            // Reset local application listeners to pre-refresh state.
            if (this.earlyApplicationListeners != null) {
                // 清空监听器
                this.applicationListeners.clear();
                this.applicationListeners.addAll(this.earlyApplicationListeners);
            }

            // Switch to inactive.
            // 存货状态置false
            this.active.set(false);
        }
    }

```
- 关闭顺序:
    1. 销毁bean
    1. 关闭beanFactory
    1. 设置存活状态
    
    
- `org.springframework.context.support.AbstractApplicationContext.destroyBeans`

```java
    protected void destroyBeans() {
        getBeanFactory().destroySingletons();
    }
```
- 摧毁单例bean
    - `org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.destroySingletons`
```java
    /**
     * 摧毁单例对象
     */
    public void destroySingletons() {
        if (logger.isTraceEnabled()) {
            logger.trace("Destroying singletons in " + this);
        }
        synchronized (this.singletonObjects) {
            this.singletonsCurrentlyInDestruction = true;
        }

        String[] disposableBeanNames;
        synchronized (this.disposableBeans) {
            // 获取摧毁的单例对象的beanName
            disposableBeanNames = StringUtils.toStringArray(this.disposableBeans.keySet());
        }
        for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
            destroySingleton(disposableBeanNames[i]);
        }

        this.containedBeanMap.clear();
        this.dependentBeanMap.clear();
        this.dependenciesForBeanMap.clear();

        // 清空
        clearSingletonCache();
    }

```


- destroySingleton
    - `org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.destroySingleton`
    
```JAVA
    public void destroySingleton(String beanName) {
        // Remove a registered singleton of the given name, if any.
        // 移除单例对象的名字
        removeSingleton(beanName);

        // Destroy the corresponding DisposableBean instance.
        DisposableBean disposableBean;
        synchronized (this.disposableBeans) {
            disposableBean = (DisposableBean) this.disposableBeans.remove(beanName);
        }
        // 销毁
        destroyBean(beanName, disposableBean);
    }
```

- 普通 bean 摧毁
    - `org.springframework.beans.factory.config.ConfigurableBeanFactory.destroyBean`
        - `org.springframework.beans.factory.support.AbstractBeanFactory.destroyBean(java.lang.String, java.lang.Object, org.springframework.beans.factory.support.RootBeanDefinition)`


```java
    protected void destroyBean(String beanName, Object bean, RootBeanDefinition mbd) {
        new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), getAccessControlContext()).destroy();
    }

```
- 摧毁方法核心`org.springframework.beans.factory.support.DisposableBeanAdapter.destroy`

```java
    /**
     * 摧毁方法
     */
    @Override
    public void destroy() {
        if (!CollectionUtils.isEmpty(this.beanPostProcessors)) {
            for (DestructionAwareBeanPostProcessor processor : this.beanPostProcessors) {
                processor.postProcessBeforeDestruction(this.bean, this.beanName);
            }
        }

        if (this.invokeDisposableBean) {
            if (logger.isTraceEnabled()) {
                logger.trace("Invoking destroy() on bean with name '" + this.beanName + "'");
            }
            try {
                if (System.getSecurityManager() != null) {
                    AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                        // 实现 DisposableBean 接口调用
                        ((DisposableBean) this.bean).destroy();
                        return null;
                    }, this.acc);
                }
                else {
                    // 实现 DisposableBean 接口调用
                    ((DisposableBean) this.bean).destroy();
                }
            }
            catch (Throwable ex) {
                String msg = "Invocation of destroy method failed on bean with name '" + this.beanName + "'";
                if (logger.isDebugEnabled()) {
                    logger.warn(msg, ex);
                }
                else {
                    logger.warn(msg + ": " + ex);
                }
            }
        }

        if (this.destroyMethod != null) {
            invokeCustomDestroyMethod(this.destroyMethod);
        }
        else if (this.destroyMethodName != null) {
            Method methodToInvoke = determineDestroyMethod(this.destroyMethodName);
            if (methodToInvoke != null) {
                // 反射执行 destroy-method 属性的对应的方法
                invokeCustomDestroyMethod(ClassUtils.getInterfaceMethodIfPossible(methodToInvoke));
            }
        }
    }

```

- 至此 bean 的初始化,摧毁都完成了~~~