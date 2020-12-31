# Spring AbstractApplicationEventMulticaster
- 类全路径: `org.springframework.context.event.AbstractApplicationEventMulticaster`





- 首先来看成员变量信息



## 成员变量



```java
private final ListenerRetriever defaultRetriever = new ListenerRetriever(false);

final Map<ListenerCacheKey, ListenerRetriever> retrieverCache = new ConcurrentHashMap<>(64);

/**
 * 类加载器
 */
@Nullable
private ClassLoader beanClassLoader;

/**
 * BeanFactory
 */
@Nullable
private ConfigurableBeanFactory beanFactory;

/**
 * lock
 */
private Object retrievalMutex = this.defaultRetriever;
```





在成员变量中有两个重要对象

1. `ListenerCacheKey` : 监听器的缓存key
2. `ListenerRetriever `: 监听器包装





## ListenerCacheKey

- 类全路径: `org.springframework.context.event.AbstractApplicationEventMulticaster.ListenerCacheKey`



- 成员变量

```java
/**
 * 事件类型
 */
private final ResolvableType eventType;

/**
 *  触发事件的类
 */
@Nullable
private final Class<?> sourceType;
```



- `ListenerCacheKey` 作用是用来区别 event 







## ListenerRetriever

- 类全路径: `org.springframework.context.event.AbstractApplicationEventMulticaster.ListenerRetriever`

- 成员变量

```java
private class ListenerRetriever {
   /**
    * 应用监听器实例列表
    */
   public final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();

   /**
    * 应用监听器名称列表
    */
   public final Set<String> applicationListenerBeans = new LinkedHashSet<>();

   private final boolean preFiltered;
}
```



- `ListenerRetriever` 作用是存储 应用监听器对象， 存储的可能
  1. 监听器实例
  2. 监听器名称





在`ListenerRetriever` 中提供了一个监听器获取的方法





```java
public Collection<ApplicationListener<?>> getApplicationListeners() {

   // 返回结果定义 成员变量的合集
   // 1. applicationListeners 存放的实例直接放入容器
   // 2. applicationListenerBeans 存放的容器 , 从 BeanFactory 中获取后放入
   List<ApplicationListener<?>> allListeners = new ArrayList<>(
         this.applicationListeners.size() + this.applicationListenerBeans.size());

   // 放入成员变量
   allListeners.addAll(this.applicationListeners);
   // 监听器名称处理
   if (!this.applicationListenerBeans.isEmpty()) {
      // 获取 BeanFactory
      BeanFactory beanFactory = getBeanFactory();
      for (String listenerBeanName : this.applicationListenerBeans) {
         try {
            // 容器中获取
            ApplicationListener<?> listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
            if (this.preFiltered || !allListeners.contains(listener)) {
               // 放入缓存
               allListeners.add(listener);
            }
         }
         catch (NoSuchBeanDefinitionException ex) {
            // Singleton listener instance (without backing bean definition) disappeared -
            // probably in the middle of the destruction phase
         }
      }
   }
   if (!this.preFiltered || !this.applicationListenerBeans.isEmpty()) {
      AnnotationAwareOrderComparator.sort(allListeners);
   }
   return allListeners;
}
```



- 返回值是
  1. 成员变量的 应用监听器
  2. 成员变量的应用监听器名称对应的应用见提供器列表





## 方法分析

### getApplicationListeners

- 方法签名: `org.springframework.context.event.AbstractApplicationEventMulticaster#getApplicationListeners(org.springframework.context.ApplicationEvent, org.springframework.core.ResolvableType)`
- 方法作用: 获取应用监听器列表



获取方式

1. 创建缓存键对象:`ListenerCacheKey` 从缓存`retrieverCache`中直接获取
2. 调用`retrieveApplicationListeners` 方法获取





```java
protected Collection<ApplicationListener<?>> getApplicationListeners(
      ApplicationEvent event, ResolvableType eventType) {

   // 获取 事件源信息
   Object source = event.getSource();
   // 事件源信息类型
   Class<?> sourceType = (source != null ? source.getClass() : null);
   // 缓存对象key的创建
   ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);

   // 从容器中获取
   // Quick check for existing entry on ConcurrentHashMap...
   ListenerRetriever retriever = this.retrieverCache.get(cacheKey);
   // 不为空直接从缓存值中获取
   if (retriever != null) {
      // 从缓存值中获取 应用监听器列表
      return retriever.getApplicationListeners();
   }

   if (this.beanClassLoader == null ||
         (ClassUtils.isCacheSafe(event.getClass(), this.beanClassLoader) &&
               (sourceType == null || ClassUtils.isCacheSafe(sourceType, this.beanClassLoader)))) {
      // Fully synchronized building and caching of a ListenerRetriever
      synchronized (this.retrievalMutex) {
         retriever = this.retrieverCache.get(cacheKey);
         if (retriever != null) {
            return retriever.getApplicationListeners();
         }
         retriever = new ListenerRetriever(true);
         Collection<ApplicationListener<?>> listeners =
               retrieveApplicationListeners(eventType, sourceType, retriever);
         this.retrieverCache.put(cacheKey, retriever);
         return listeners;
      }
   }
   else {
      // No ListenerRetriever caching -> no synchronization necessary
      return retrieveApplicationListeners(eventType, sourceType, null);
   }
}
```







### retrieveApplicationListeners

- 方法签名: `org.springframework.context.event.AbstractApplicationEventMulticaster#retrieveApplicationListeners`

- 方法作用: 搜索监听器





搜索方式

1. 从帮助类`ListenerRetriever`中获取应用监听器实例，在这个实例中判断是否符合传入参数的事件类型(`supportsEvent(listener, eventType, sourceType)`) 符合则加入容器
2. 处理形式和👆步骤1基本类似, 从实例搜索变成了名称搜索. 循环监听器名称列表从容器中获取应用实例后进行判断**监听器是否支持事件**，支持加入容器
3. 排序





```java
private Collection<ApplicationListener<?>> retrieveApplicationListeners(
      ResolvableType eventType, @Nullable Class<?> sourceType, @Nullable ListenerRetriever retriever) {

   // 返回值列表
   List<ApplicationListener<?>> allListeners = new ArrayList<>();
   Set<ApplicationListener<?>> listeners;
   Set<String> listenerBeans;
   synchronized (this.retrievalMutex) {
      // 应用监听器实例列表
      listeners = new LinkedHashSet<>(this.defaultRetriever.applicationListeners);
      // 应用监听器名称列表
      listenerBeans = new LinkedHashSet<>(this.defaultRetriever.applicationListenerBeans);
   }

   // Add programmatically registered listeners, including ones coming
   // from ApplicationListenerDetector (singleton beans and inner beans).
   for (ApplicationListener<?> listener : listeners) {
      // 判断当前监听器是否支持传入的事件
      if (supportsEvent(listener, eventType, sourceType)) {
         if (retriever != null) {
            // 帮助容器中添加数据
            retriever.applicationListeners.add(listener);
         }
         // 放入返回对象中
         allListeners.add(listener);
      }
   }

   // Add listeners by bean name, potentially overlapping with programmatically
   // registered listeners above - but here potentially with additional metadata.
   if (!listenerBeans.isEmpty()) {
      ConfigurableBeanFactory beanFactory = getBeanFactory();
      // applicationListener name 处理
      for (String listenerBeanName : listenerBeans) {
         try {
            // 事件监听器 是否支持 当前的事件

            // 支持的处理
            if (supportsEvent(beanFactory, listenerBeanName, eventType)) {
               // 获取 bean
               ApplicationListener<?> listener =
                     beanFactory.getBean(listenerBeanName, ApplicationListener.class);
               if (!allListeners.contains(listener) && supportsEvent(listener, eventType, sourceType)) {
                  // 帮助容器中添加数据
                  if (retriever != null) {
                     if (beanFactory.isSingleton(listenerBeanName)) {
                        retriever.applicationListeners.add(listener);
                     }
                     else {
                        retriever.applicationListenerBeans.add(listenerBeanName);
                     }
                  }
                  // 放入返回对象中
                  allListeners.add(listener);
               }
            }
            // 不支持的处理
            else {
               // Remove non-matching listeners that originally came from
               // ApplicationListenerDetector, possibly ruled out by additional
               // BeanDefinition metadata (e.g. factory method generics) above.
               Object listener = beanFactory.getSingleton(listenerBeanName);
            // 将 ApplicationListener 移除容器
               if (retriever != null) {
                  retriever.applicationListeners.remove(listener);
               }
               allListeners.remove(listener);
            }
         }
         catch (NoSuchBeanDefinitionException ex) {
            // Singleton listener instance (without backing bean definition) disappeared -
            // probably in the middle of the destruction phase
         }
      }
   }

   // 排序后放入容器
   AnnotationAwareOrderComparator.sort(allListeners);
   if (retriever != null && retriever.applicationListenerBeans.isEmpty()) {
      retriever.applicationListeners.clear();
      retriever.applicationListeners.addAll(allListeners);
   }
   return allListeners;
}
```



### supportsEvent

- 方法签名: `org.springframework.context.event.AbstractApplicationEventMulticaster#supportsEvent(org.springframework.beans.factory.config.ConfigurableBeanFactory, java.lang.String, org.springframework.core.ResolvableType)`

- 方法作用: 判断是否支持事件

判断逻辑

2. 监听器是否是 GenericApplicationListener
3. 监听器是否是 SmartApplicationListener
3. 通过 `supportsEvent` 方法判断
4. BeanDefinition 的判断
   1. `ResolvableType` 为空 或者 `ResolvableType` 类型和参数同源





```java
private boolean supportsEvent(
      ConfigurableBeanFactory beanFactory, String listenerBeanName, ResolvableType eventType) {

   // 从容器中获取类型
   Class<?> listenerType = beanFactory.getType(listenerBeanName);
   // 1. 判空
   // 2. 监听器是否是 GenericApplicationListener
   // 3. 监听器是否是 SmartApplicationListener
   if (listenerType == null || GenericApplicationListener.class.isAssignableFrom(listenerType) ||
         SmartApplicationListener.class.isAssignableFrom(listenerType)) {
      return true;
   }

   // 监听器是否支持事件
   if (!supportsEvent(listenerType, eventType)) {
      return false;
   }
   try {
      BeanDefinition bd = beanFactory.getMergedBeanDefinition(listenerBeanName);
      ResolvableType genericEventType = bd.getResolvableType().as(ApplicationListener.class).getGeneric();
      
      // genericEventType 是否 none
      // beanDefinition 是否是 eventType
      return (genericEventType == ResolvableType.NONE || genericEventType.isAssignableFrom(eventType));
   }
   catch (NoSuchBeanDefinitionException ex) {
      // Ignore - no need to check resolvable type for manually registered singleton
      return true;
   }
}
```







### supportsEvent

- 方法签名: `org.springframework.context.event.AbstractApplicationEventMulticaster#supportsEvent(java.lang.Class<?>, org.springframework.core.ResolvableType)`

```java
protected boolean supportsEvent(Class<?> listenerType, ResolvableType eventType) {
   // 监听器中获取事件类型
   ResolvableType declaredEventType = GenericApplicationListenerAdapter.resolveDeclaredEventType(listenerType);
   // 事件类型判断是否同源
   return (declaredEventType == null || declaredEventType.isAssignableFrom(eventType));
}
```