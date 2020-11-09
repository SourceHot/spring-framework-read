# Spring BeanDefinition

- 类全路径: `org.springframework.beans.factory.config.BeanDefinition`

- 先了解`BeanDefinition` 的静态变量



## 静态变量

- `SCOPE_SINGLETON` 作用域: 单例
- `SCOPE_PROTOTYPE`作用域: 原型
- `ROLE_XXX` SPRING_BEAN ROLE 相关信息, 暂时不确定其作用



```java
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
```





## 方法列表

- 看完静态变量后来关注其方法列表. 



```java
/**
 * 获取父类的名字
 */
@Nullable
String getParentName();

/**
 * 设置父类名称
 */
void setParentName(@Nullable String parentName);

/**
 *
 * 获取 bean 类型名称 (xxx.Class)
 */
@Nullable
String getBeanClassName();

/**
 * 设置 beanClass
 */
void setBeanClassName(@Nullable String beanClassName);

/**
 * 获取作用域
 */
@Nullable
String getScope();

/**
 * 设置作用域
 */
void setScope(@Nullable String scope);

/**
 * 是否延迟加载
 */
boolean isLazyInit();

/**
 * 设置是否延迟加载
 */
void setLazyInit(boolean lazyInit);

/**
 * 获取依赖名称列表
 */
@Nullable
String[] getDependsOn();

/**
 * 设置需要的依赖
 */
void setDependsOn(@Nullable String... dependsOn);

/**
 * 是否需要自动连接到别的bean
 */
boolean isAutowireCandidate();

/**
 * 设置是否需要自动连接到背的bean
 */
void setAutowireCandidate(boolean autowireCandidate);

/**
 * 是否主要,针对多个相同类型的情况下使用
 */
boolean isPrimary();

/**
 * 设置是否是主要的bean
 */
void setPrimary(boolean primary);

/**
 * 获取 factory bean 名称
 */
@Nullable
String getFactoryBeanName();

/**
 * 设置 factory bean 名称
 */
void setFactoryBeanName(@Nullable String factoryBeanName);

/**
 * 获取工厂方法名称
 */
@Nullable
String getFactoryMethodName();

/**
 * 设置工厂方法名称
 */
void setFactoryMethodName(@Nullable String factoryMethodName);

/**
 * 获取 构造标签的对象{@code <constructor-arg/>}
 */
ConstructorArgumentValues getConstructorArgumentValues();

/**
 * 是否存在构造标签的java对象
 */
default boolean hasConstructorArgumentValues() {
   return !getConstructorArgumentValues().isEmpty();
}

/**
 * 获取属性值对象
 */
MutablePropertyValues getPropertyValues();

/**
 * 是否存在属性值对象
 */
default boolean hasPropertyValues() {
   return !getPropertyValues().isEmpty();
}

/**
 * 获取初始化函数方法名称
 */
@Nullable
String getInitMethodName();

/**
 *设置初始化函数方法名称(bean初始化时调用)
 */
void setInitMethodName(@Nullable String initMethodName);

/**
 * 获取摧毁方法名称
 */
@Nullable
String getDestroyMethodName();

/**
 * 设置摧毁方法名称(bean摧毁时调用)
 */
void setDestroyMethodName(@Nullable String destroyMethodName);

/**
 * 获取 beanRole
 */
int getRole();

/**
 * 设置 beanRole
 */
void setRole(int role);

/**
 * 获取 bean 描述信息
 */
@Nullable
String getDescription();

/**
 * 设置 bean 描述信息
 */
void setDescription(@Nullable String description);


// Read-only attributes

/**
 * 获取 ResolvableType 对象
 */
ResolvableType getResolvableType();

/**
 * 判断是否单例对象
 */
boolean isSingleton();

/**
 * 判断是否原型对象
 */
boolean isPrototype();

/**
 * 判断是否 abstract
 */
boolean isAbstract();

/**
 * 获取 资源描述
 */
@Nullable
String getResourceDescription();

/**
 * 获取远程bean定义
 */
@Nullable
BeanDefinition getOriginatingBeanDefinition();
```