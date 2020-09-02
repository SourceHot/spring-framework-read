# Spring BeanFactory 
- Author: [HuiFer](https://github.com/huifer)
- 源码阅读仓库: [SourceHot-spring](https://github.com/SourceHot/spring-framework-read)

## BeanFactory 概述
- `org.springframework.beans.factory.BeanFactory`

### 类图

![beanFactory](./images/BeanFactory.png)



### 方法列表
- 贴出部分代码. 仅表示方法作用
```java
public interface BeanFactory {
    // 从容器中根据beanname获取
	Object getBean(String name) throws BeansException;
    // 延迟加载对象
	<T> ObjectProvider<T> getBeanProvider(Class<T> requiredType);
    // 是否存在beanName
	boolean containsBean(String name);
    // 这个 beanName 是否是单例的. 映射成 bean
	boolean isSingleton(String name) throws NoSuchBeanDefinitionException;
    // 是否多例.
	boolean isPrototype(String name) throws NoSuchBeanDefinitionException;
    // 类型是否匹配
	boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException;
    // 获取bean的类型
	Class<?> getType(String name) throws NoSuchBeanDefinitionException;
    // 获取别名
	String[] getAliases(String name);
}
```



## 解析

### 用例

bean 的实例化有如下几种方法 

1. 静态方法
2. 工厂方法创建
3. FactoryBean 接口创建





### 代码部分

```java
public class UserBean {

  private String name;
  private Integer age;

  public static UserBean createInstance() {
    UserBean userBean = new UserBean();
    userBean.setAge(18);
    userBean.setName("zhangsan");

    return userBean;
  }
    // get set 忽略
}
```



```java
public interface UserBeanFactory {
  UserBean factory();
}

public class UserBeanFactoryImpl implements
        UserBeanFactory {


    @Override
    public UserBean factory() {
        return UserBean.createInstance();
    }
}

```





```java
public class UserFactoryBean implements FactoryBean<UserBean> {

  @Override
  public boolean isSingleton() {
    return true;
  }

  @Override
  public UserBean getObject() throws Exception {
    return UserBean.createInstance();
  }

  @Override
  public Class<?> getObjectType() {
    return UserBean.class;
  }
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">


   <!--  静态方法-->
   <bean id="static-method-user"
        class="org.source.hot.spring.overview.ioc.bean.init.UserBean"
        factory-method="createInstance"/>

   <!--  工厂方法创建-->
   <bean id="factory-use" class="org.source.hot.spring.overview.ioc.bean.init.UserBean"
        factory-bean="userFactory" factory-method="factory"></bean>
   <!--user 工厂bean-->
   <bean id="userFactory"
        class="org.source.hot.spring.overview.ioc.bean.init.UserBeanFactoryImpl"/>
   <!--factory bean-->
   <bean id="factory-bean-user"
        class="org.source.hot.spring.overview.ioc.bean.init.UserFactoryBean"/>

</beans>
```



```java
public class SpringBeanInstantiation {

  public static void main(String[] args) {
    BeanFactory context = new ClassPathXmlApplicationContext(
        "META-INF/beans/spring-bean-instantiation.xml");

    UserBean staticMethodBean = context.getBean("static-method-user", UserBean.class);
    UserBean factoryUser = context.getBean("factory-use", UserBean.class);
    UserBean factoryBean = context.getBean("factory-bean-user", UserBean.class);
    System.out.println();

  }
}
```





### 分析

- 对下面代码进行分析

```java
 UserBean staticMethodBean = context.getBean("static-method-user", UserBean.class);
```



- `org.springframework.context.support.AbstractApplicationContext#getBean(java.lang.String, java.lang.Class<T>)`

```java
@Override
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
	    // 判断 beanFactory 是否存活
		assertBeanFactoryActive();
		
		// 1. 获取 beanFactory
        // 2. 根据 beanName + class 获取 Bean
		return getBeanFactory().getBean(name, requiredType);
	}
```

- 从方法参数
  - name: beanName
  - requiredType: 唯一的类型. 对象类型





### assertBeanFactoryActive

- beanFactory 是否存活判断

```java
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
```



### getBeanFactory

- 获取beanFactory

  - 获取方法是一个抽象方法

    ```java
    public abstract ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;
    ```

    - 子类实现

      `org.springframework.context.support.AbstractRefreshableApplicationContext#getBeanFactory`

      ```java
      @Override
      public final ConfigurableListableBeanFactory getBeanFactory() {
         synchronized (this.beanFactoryMonitor) {
            if (this.beanFactory == null) {
               throw new IllegalStateException("BeanFactory not initialized or already closed - " +
                     "call 'refresh' before accessing beans via the ApplicationContext");
            }
            return this.beanFactory;
         }
      }
      ```

      - `org.springframework.context.support.GenericApplicationContext#getBeanFactory`

      ```java
      @Override
      public final ConfigurableListableBeanFactory getBeanFactory() {
         return this.beanFactory;
      }
      ```



- 获取到的对象是`org.springframework.beans.factory.support.DefaultListableBeanFactory`

  

![image-20200902102912716](images/image-20200902102912716.png)

- 整体类图

![image-20200902103154580](images/image-20200902103154580.png)





### getBean

- `org.springframework.beans.factory.support.AbstractBeanFactory#doGetBean`

  获取 bean 的核心





#### transformedBeanName

```java
protected String transformedBeanName(String name) {
    // 转换 beanName .
    // 1. 通过·BeanFactoryUtils.transformedBeanName· 求beanName
    // 2. 如果是有别名的(方法参数是别名) . 会从别名列表中获取对应的 beanName
    return canonicalName(BeanFactoryUtils.transformedBeanName(name));
}
```



```java
public static String transformedBeanName(String name) {
       Assert.notNull(name, "'name' must not be null");
       // 名字不是 & 开头直接返回
   if (!name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
      return name;
   }
   // 截取字符串 在返回
   return transformedBeanNameCache.computeIfAbsent(name, beanName -> {
      do {
         beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
      }
      while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
      return beanName;
   });
}
```

```java
public String canonicalName(String name) {
    String canonicalName = name;
    // Handle aliasing...
    String resolvedName;
    do {
        // 别名的获取
        resolvedName = this.aliasMap.get(canonicalName);
        if (resolvedName != null) {
            canonicalName = resolvedName;
        }
    }
    while (resolvedName != null);
    return canonicalName;
}
```

别名对象

```java
private final Map<String, String> aliasMap = new ConcurrentHashMap<>(16);
```

```java
<bean id="factory-bean-user"
     class="org.source.hot.spring.overview.ioc.bean.init.UserFactoryBean"/>

<alias name="factory-bean-user" alias="userFactoryBean"/>
```

aliasMap 和 别名标签的对应关系

![image-20200902105454958](images/image-20200902105454958.png)



alias标签的alias值作为别名的key ， alias 标签的 name 值作为 value





#### getSingleton

- `org.springframework.beans.factory.support.DefaultSingletonBeanRegistry#getSingleton(java.lang.String)`



```java
@Override
@Nullable
public Object getSingleton(String beanName) {
   return getSingleton(beanName, true);
}
```



- `org.springframework.beans.factory.support.DefaultSingletonBeanRegistry#getSingleton(java.lang.String, boolean)`



```java
@Nullable
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
    // 尝试从单例缓存中获取
    Object singletonObject = this.singletonObjects.get(beanName);
    // 单例对象是否null
    // 这个 beanName 是否正在创建
    if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
        // 锁
        synchronized (this.singletonObjects) {
            // 从延迟加载的map中获取
            singletonObject = this.earlySingletonObjects.get(beanName);
            // 对象是否空 ， 是否允许提前应用
            if (singletonObject == null && allowEarlyReference) {
                // 从对象工厂map中获取对象工厂
                ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                if (singletonFactory != null) {
                    // 对象获取后设置
                    singletonObject = singletonFactory.getObject();
                    this.earlySingletonObjects.put(beanName, singletonObject);
                    this.singletonFactories.remove(beanName);
                }
            }
        }
    }
    return singletonObject;
}
```

- 相关属性值



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
     *  Names of beans that are currently in creation.
     *
     * 当前正在实例化的beanName
     *
     * */
    private final Set<String> singletonsCurrentlyInCreation =
            Collections.newSetFromMap(new ConcurrentHashMap<>(16));
```







#### getObjectForBeanInstance

- `org.springframework.beans.factory.support.AbstractBeanFactory#getObjectForBeanInstance`









#### getObjectFromFactoryBean







- 从 FactoryBean 中获取对象

```java
private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, final String beanName)
        throws BeanCreationException {

    Object object;
    try {
        if (System.getSecurityManager() != null) {
            AccessControlContext acc = getAccessControlContext();
            try {
                // 从 FactoryBean 中获取bean
                object = AccessController.doPrivileged((PrivilegedExceptionAction<Object>) factory::getObject, acc);
            }
            catch (PrivilegedActionException pae) {
                throw pae.getException();
            }
        }
        else {
            // 获取obj
            object = factory.getObject();
        }
    }
    catch (FactoryBeanNotInitializedException ex) {
        throw new BeanCurrentlyInCreationException(beanName, ex.toString());
    }
    catch (Throwable ex) {
        throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
    }

    // Do not accept a null value for a FactoryBean that's not fully
    // initialized yet: Many FactoryBeans just return null then.
    if (object == null) {
        if (isSingletonCurrentlyInCreation(beanName)) {
            throw new BeanCurrentlyInCreationException(
                    beanName, "FactoryBean which is currently in creation returned null from getObject");
        }
        // 什么都没有的对象
        object = new NullBean();
    }
    return object;
}
```

