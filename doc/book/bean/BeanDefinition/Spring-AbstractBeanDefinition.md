# Spring AbstractBeanDefinition

- 类全路径: `org.springframework.beans.factory.support.AbstractBeanDefinition`



- `AbstractBeanDefinition`主要定义了一个bean应该有那些属性, 其内部变量尤为重要。方法大多数是`get&set`方法



## 内部变量



```java
/**
 * scope 默认值
 */
public static final String SCOPE_DEFAULT = "";

/**
 * 自动注入方式: no
 */
public static final int AUTOWIRE_NO = AutowireCapableBeanFactory.AUTOWIRE_NO;

/**
 * 自动注入方式: 根据名称
 */
public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

/**
 * 自动注入方式: 根据类型
 */
public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

/**
 * 自动注入方式: 构造函数注入
 */
public static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;

/**
 * 过时方法, 类型+构造函数注入
 */
@Deprecated
public static final int AUTOWIRE_AUTODETECT = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;

/**
 * 依赖检查级别:不检查
 */
public static final int DEPENDENCY_CHECK_NONE = 0;

/**
 * 依赖检查级别: 对依赖对象检查
 */
public static final int DEPENDENCY_CHECK_OBJECTS = 1;

/**
 * 依赖检查级别: 对原始类型
 */
public static final int DEPENDENCY_CHECK_SIMPLE = 2;

/**
 * 依赖检查级别: 检查所有
 */
public static final int DEPENDENCY_CHECK_ALL = 3;

/**
 * Constant that indicates the container should attempt to infer the
 * {@link #setDestroyMethodName destroy method name} for a bean as opposed to
 * explicit specification of a method name. The value {@value} is specifically
 * designed to include characters otherwise illegal in a method name, ensuring
 * no possibility of collisions with legitimately named methods having the same
 * name.
 * <p>Currently, the method names detected during destroy method inference
 * are "close" and "shutdown", if present on the specific bean class.
 */
public static final String INFER_METHOD = "(inferred)";

/**
 * 外部依赖的对象
 * key: 注入的名称
 * value: AutowireCandidateQualifier
 */
private final Map<String, AutowireCandidateQualifier> qualifiers = new LinkedHashMap<>();

/**
 * bean class
 */
@Nullable
private volatile Object beanClass;

/**
 * 作用域 , 默认"" , 单例
 */
@Nullable
private String scope = SCOPE_DEFAULT;

/**
 * 是否 abstract 标记
 */
private boolean abstractFlag = false;

/**
 * 是否懒加载
 */
@Nullable
private Boolean lazyInit;

/**
 * 自动注入方式, 默认 no
 */
private int autowireMode = AUTOWIRE_NO;

/**
 * 依赖检查级别, 默认不检查
 */
private int dependencyCheck = DEPENDENCY_CHECK_NONE;

/**
 * 依赖的bean
 */
@Nullable
private String[] dependsOn;

/**
 * 是否自动注入
 */
private boolean autowireCandidate = true;

/**
 * 是否是主要的bean, 针对多bean实例的情况下使用
 */
private boolean primary = false;

/**
 * 实例提供者,用来返回java对象的实例(bean实例)
 */
@Nullable
private Supplier<?> instanceSupplier;

/**
 * 是否禁止公共访问
 */
private boolean nonPublicAccessAllowed = true;

private boolean lenientConstructorResolution = true;

/**
 * factory bean name
 */
@Nullable
private String factoryBeanName;

/**
 * factory method name
 */
@Nullable
private String factoryMethodName;

/**
 * 构造标签的对象
 */
@Nullable
private ConstructorArgumentValues constructorArgumentValues;

/**
 * 属性列表对象
 */
@Nullable
private MutablePropertyValues propertyValues;

/**
 * 重写方法列表
 */
private MethodOverrides methodOverrides = new MethodOverrides();

/**
 * 初始化方法
 */
@Nullable
private String initMethodName;

/**
 * 摧毁方法
 */
@Nullable
private String destroyMethodName;

/**
 *
 */
private boolean enforceInitMethod = true;

private boolean enforceDestroyMethod = true;

private boolean synthetic = false;

/**
 * bean role
 */
private int role = BeanDefinition.ROLE_APPLICATION;

/**
 * bean 描述
 */
@Nullable
private String description;

/**
 * 资源对象
 */
@Nullable
private Resource resource;
```



## 方法分析



### resolveBeanClass

- 通过 classLoader 将 class 加载出来. 

```java
@Nullable
public Class<?> resolveBeanClass(@Nullable ClassLoader classLoader) throws ClassNotFoundException {
   // 获取beanClassName
   String className = getBeanClassName();
   if (className == null) {
      return null;
   }
   // 加载类
   Class<?> resolvedClass = ClassUtils.forName(className, classLoader);
   this.beanClass = resolvedClass;
   // 返回
   return resolvedClass;
}
```





### getBeanClassName

- 获取当前bean的类型名称



```java
@Override
@Nullable
public String getBeanClassName() {
   Object beanClassObject = this.beanClass;
   if (beanClassObject instanceof Class) {
      return ((Class<?>) beanClassObject).getName();
   }
   else {
      return (String) beanClassObject;
   }
}
```



### getResolvedAutowireMode

- 获取 自动注入的方式

1. 判断是否是 根据类型注入+构造函数注入

   1. 通过判断是否有无参构造函数.

      - 如果存在无参构造返回 根据类型自动注入

      - 不存在则通过构造函数注入

2. 返回设置的注入方式

```java
public int getResolvedAutowireMode() {
   if (this.autowireMode == AUTOWIRE_AUTODETECT) {
      // Work out whether to apply setter autowiring or constructor autowiring.
      // If it has a no-arg constructor it's deemed to be setter autowiring,
      // otherwise we'll try constructor autowiring.
      // 获取构造函数列表
      Constructor<?>[] constructors = getBeanClass().getConstructors();
      for (Constructor<?> constructor : constructors) {
         // 构造函数参数没有为 根据类型
         if (constructor.getParameterCount() == 0) {
            return AUTOWIRE_BY_TYPE;
         }
      }
      // 构造函数注入
      return AUTOWIRE_CONSTRUCTOR;
   }
   else {
      return this.autowireMode;
   }
}
```



### validate

- bean 定义验证

1. 验证是否存在需要重写的方法
2. 验证 FactoryMethodName 是否存在
3. 方法重写+验证

```java
public void validate() throws BeanDefinitionValidationException {
   // 是否存在重写方法, factory_method_name 是否为空
   if (hasMethodOverrides() && getFactoryMethodName() != null) {
      throw new BeanDefinitionValidationException(
            "Cannot combine factory method with container-generated method overrides: " +
                  "the factory method must create the concrete bean instance.");
   }
   //  bean class 是否等于 Class
   if (hasBeanClass()) {
      // 方法重写+验证
      prepareMethodOverrides();
   }
}
```



### prepareMethodOverrides

- 判断是否需要重写方法. 
  - 循环设置 `MethodOverride` 对象

```java
public void prepareMethodOverrides() throws BeanDefinitionValidationException {
   // Check that lookup methods exist and determine their overloaded status.

   // 是否需要重写
   if (hasMethodOverrides()) {
      // 重写
      getMethodOverrides().getOverrides().forEach(this::prepareMethodOverride);
   }
}
```



### prepareMethodOverride

- 对需要覆盖的方法进行处理. 设置对象 `MethodOverride`



```java
protected void prepareMethodOverride(MethodOverride mo) throws BeanDefinitionValidationException {
   // 返回需要重写方法的数量
   int count = ClassUtils.getMethodCountForName(getBeanClass(), mo.getMethodName());
   // 重写方法数量 = 0 异常
   if (count == 0) {
      throw new BeanDefinitionValidationException(
            "Invalid method override: no method with name '" + mo.getMethodName() +
                  "' on class [" + getBeanClassName() + "]");
   }
   // 重写方法数量等于1 设置重写值=false
   else if (count == 1) {
      // Mark override as not overloaded, to avoid the overhead of arg type checking.
      mo.setOverloaded(false);
   }
}
```

