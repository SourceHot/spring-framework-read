# Spring SimpleJndiBeanFactory
- 类全路径: `org.springframework.jndi.support.SimpleJndiBeanFactory`
- 在 `SimpleJndiBeanFactory` 存在三个属性变量, 关注三个属性的作用

## 成员变量
- 属性变量介绍
    1. shareableResources: 存储资源名称, 在这里存储 beanName
    2. singletonObjects: 存储 beanName 和 bean 实例
    3. esourceTypes: 存储 beanName 和 bean 类型

下面是源码文档. 
```java
	/**
	 * JNDI names of resources that are known to be shareable, i.e. can be cached
	 * 存储资源名称
	 * */
	private final Set<String> shareableResources = new HashSet<>();

	/**
	 * Cache of shareable singleton objects: bean name to bean instance.
	 * 存储单例bean和bean实例
	 * */
	private final Map<String, Object> singletonObjects = new HashMap<>();

	/** 
	 * Cache of the types of nonshareable resources: bean name to bean type. 
	 * key: beanName 
	 * value: bean class
	 * */
	private final Map<String, Class<?>> resourceTypes = new HashMap<>();
```

## 方法分析
### getBean
- 先来看 `getBean` 方法, 逻辑如下
    1. 判断是否单例(判断是否在单例map中存有key)
    2. 获取bean
        1. 从单例容器中获取
        2. JNDI 查找
        
```java
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		try {
			// 判断是否是单例bean
			if (isSingleton(name)) {
				return doGetSingleton(name, requiredType);
			}
			// 寻找
			else {
				return lookup(name, requiredType);
			}
		}
		catch (NameNotFoundException ex) {
			throw new NoSuchBeanDefinitionException(name, "not found in JNDI environment");
		}
		catch (TypeMismatchNamingException ex) {
			throw new BeanNotOfRequiredTypeException(name, ex.getRequiredType(), ex.getActualType());
		}
		catch (NamingException ex) {
			throw new BeanDefinitionStoreException("JNDI environment", name, "JNDI lookup failed", ex);
		}
	}
```


### doGetSingleton 
- 获取单例对象
    1. 从容其中直接获取
    2. 通过 JNDI 获取 `lookup` 
        - 笔者对`org.springframework.jndi.JndiLocatorSupport.lookup(java.lang.String, java.lang.Class<T>)`方法的理解不是很深入, 这一块内容还请各位读者自行理解了. 
```java
	@SuppressWarnings("unchecked")
	private <T> T doGetSingleton(String name, @Nullable Class<T> requiredType) throws NamingException {
		synchronized (this.singletonObjects) {
			// 从单例对象容器中根据名称获取
			Object singleton = this.singletonObjects.get(name);
			// 对象不为空
			if (singleton != null) {
				// 类型相同
				if (requiredType != null && !requiredType.isInstance(singleton)) {
					throw new TypeMismatchNamingException(convertJndiName(name), requiredType, singleton.getClass());
				}
				return (T) singleton;
			}
			T jndiObject = lookup(name, requiredType);
			// 放入容器
			this.singletonObjects.put(name, jndiObject);
			return jndiObject;
		}
	}

```