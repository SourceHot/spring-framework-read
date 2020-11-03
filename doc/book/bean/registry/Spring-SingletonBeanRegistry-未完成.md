# Spring SingletonBeanRegistry

- 类全路径: `org.springframework.beans.factory.config.SingletonBeanRegistry`

- 类图:
![SingletonBeanRegistry.png](./images/SingletonBeanRegistry.png)




## 方法列表



```
public interface SingletonBeanRegistry {

   /**
    * 注册单例bean
    */
   void registerSingleton(String beanName, Object singletonObject);

   /**
    * 获取单例bean
    */
   @Nullable
   Object getSingleton(String beanName);

   /**
    * 是否存在单例对象
    */
   boolean containsSingleton(String beanName);

   /**
    * 获取所有的单例对象beanName
    */
   String[] getSingletonNames();

   /**
    * 单例bean的数量
    */
   int getSingletonCount();


   Object getSingletonMutex();

}
```



## DefaultSingletonBeanRegistry

- 顶层实现为: `DefaultSingletonBeanRegistry`. 接下来围绕DefaultSingletonBeanRegistry进行分析
- 首先先关注内部属性



```java
	/**
	 *  Cache of singleton objects: bean name to bean instance.
	 *
	 * 单例对象容器, key: beanName , value: bean实例
	 * */
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

	/**
	 *  Cache of singleton factories: bean name to ObjectFactory.
	 * key: beanName
	 * value: 对象工厂
	 * */
	private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

	/**
	 *  Cache of early singleton objects: bean name to bean instance.
	 *
	 * early 概念, 可能是一个没有被实例化的或者说没有被调用的对象
	 * */
	private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);

	/**
	 *
	 * Set of registered singletons, containing the bean names in registration order.
	 *
	 * 注册过的单例单例对象的beanName
	 *  */
	private final Set<String> registeredSingletons = new LinkedHashSet<>(256);

	/**
	 *  Names of beans that are currently in creation.
	 *
	 * 当前正在实例化的beanName
	 *
	 * */
	private final Set<String> singletonsCurrentlyInCreation =
			Collections.newSetFromMap(new ConcurrentHashMap<>(16));

	/**
	 *
	 * Names of beans currently excluded from in creation checks.
	 * 排除的beanName
	 * */
	private final Set<String> inCreationCheckExclusions =
			Collections.newSetFromMap(new ConcurrentHashMap<>(16));

	/**
	 *  Disposable bean instances: bean name to disposable instance.
	 *  一次性的bean
	 *  key: beanName
	 *  value: bean instances
	 *  */
	private final Map<String, Object> disposableBeans = new LinkedHashMap<>();

	/**
	 *  Map between containing bean names: bean name to Set of bean names that the bean contains.
	 *
	 *  */
	private final Map<String, Set<String>> containedBeanMap = new ConcurrentHashMap<>(16);

	/**
	 * Map between dependent bean names: bean name to Set of dependent bean names.
	 *
	 * key: bean
	 * value: 依赖列表
	 * */
	private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<>(64);

	/**
	 *  Map between depending bean names: bean name to Set of bean names for the bean's dependencies.
	 *
	 * key: beanName
	 * value: bean 依赖的beanName
	 *
	 *  */
	private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<>(64);

	/**
	 * List of suppressed Exceptions, available for associating related causes.
	 * 异常列表
	 * */
	@Nullable
	private Set<Exception> suppressedExceptions;

	/**
	 * Flag that indicates whether we're currently within destroySingletons.
	 *
	 * */
	private boolean singletonsCurrentlyInDestruction = false;
```





`DefaultSingletonBeanRegistry` 相关的方法操作的都是上述的内部变量. 接下来开始对方法进行逐步分析



### registerSingleton

- 先来看一看 注册单例对象
- 逻辑
  - 判断beanName 和 单例对象是否存在
  - 判断当前BeanName是否注册过
    - 没有注册就注册
    - 注册抛出异常



```java
@Override
public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
   Assert.notNull(beanName, "Bean name must not be null");
   Assert.notNull(singletonObject, "Singleton object must not be null");
   synchronized (this.singletonObjects) {
      Object oldObject = this.singletonObjects.get(beanName);
      if (oldObject != null) {
         throw new IllegalStateException("Could not register object [" + singletonObject +
               "] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
      }
      addSingleton(beanName, singletonObject);
   }
}
```





### addSingleton

- 在注册单例对象的时候后续调用 `addSingleton`方法. 该方法的作用是将 beanName 和 beanObject 放入容器, 删除beanName所在的一些容器。具体来看看代码

```java
protected void addSingleton(String beanName, Object singletonObject) {
   synchronized (this.singletonObjects) {
      // 设置 单例对象 map
      this.singletonObjects.put(beanName, singletonObject);
      // 删除 单例的beanFactory
      this.singletonFactories.remove(beanName);
      // 删除 延迟加载的bean
      this.earlySingletonObjects.remove(beanName);
      // 放入已注册的beanName
      this.registeredSingletons.add(beanName);
   }
}
```






### addSingletonFactory

- 添加单例的ObjectFactory



```java
	protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
		Assert.notNull(singletonFactory, "Singleton factory must not be null");
		synchronized (this.singletonObjects) {
			// 单例bean容器中是否存在
			if (!this.singletonObjects.containsKey(beanName)) {
				// 添加单例对象工厂
				this.singletonFactories.put(beanName, singletonFactory);
				// 删除单例BeanName
				this.earlySingletonObjects.remove(beanName);
				// 注册单例beanName
				this.registeredSingletons.add(beanName);
			}
		}
	}

```

