# Spring CachedIntrospectionResults 
- 类全路径: `org.springframework.beans.CachedIntrospectionResults`

在 `CachedIntrospectionResults` 中定义了大量的属性对象





| 属性名称                                  | 含义                                            |
| ----------------------------------------- | ----------------------------------------------- |
| `IGNORE_BEANINFO_PROPERTY_NAME`           | 字符串变量,标记`"spring.beaninfo.ignore"`字符串 |
| `acceptedClassLoaders`                    | 容器, 存储对象`ClassLoader`                     |
| `strongClassCache`                        | 容器,存储线程安全的bean                         |
| `softClassCache`                          | 容器,存储线程不安全的bean                       |
| `shouldIntrospectorIgnoreBeaninfoClasses` | 读取`IGNORE_BEANINFO_PROPERTY_NAME`后的结果值   |
| `beanInfoFactories`                       | `BeanInfoFactory` 容器                          |
| `BeanInfo`                                | BeanInfo 接口                                   |
| `propertyDescriptorCache`                 | 属性名称 -> 属性描述对象                        |
| `typeDescriptorCache`                     | 属性描述对象 -> 类型描述对象                    |




<details>
<summary>成员变量代码如下</summary>


```java
public final class CachedIntrospectionResults {

   /**
    * System property that instructs Spring to use the {@link Introspector#IGNORE_ALL_BEANINFO}
    * mode when calling the JavaBeans {@link Introspector}: "spring.beaninfo.ignore", with a
    * value of "true" skipping the search for {@code BeanInfo} classes (typically for scenarios
    * where no such classes are being defined for beans in the application in the first place).
    * <p>The default is "false", considering all {@code BeanInfo} metadata classes, like for
    * standard {@link Introspector#getBeanInfo(Class)} calls. Consider switching this flag to
    * "true" if you experience repeated ClassLoader access for non-existing {@code BeanInfo}
    * classes, in case such access is expensive on startup or on lazy loading.
    * <p>Note that such an effect may also indicate a scenario where caching doesn't work
    * effectively: Prefer an arrangement where the Spring jars live in the same ClassLoader
    * as the application classes, which allows for clean caching along with the application's
    * lifecycle in any case. For a web application, consider declaring a local
    * {@link org.springframework.web.util.IntrospectorCleanupListener} in {@code web.xml}
    * in case of a multi-ClassLoader layout, which will allow for effective caching as well.
    * @see Introspector#getBeanInfo(Class, int)
    */
   public static final String IGNORE_BEANINFO_PROPERTY_NAME = "spring.beaninfo.ignore";

   /**
    * Set of ClassLoaders that this CachedIntrospectionResults class will always
    * accept classes from, even if the classes do not qualify as cache-safe.
    *
    * 容器, 存储对象`ClassLoader`
    */
   static final Set<ClassLoader> acceptedClassLoaders =
         Collections.newSetFromMap(new ConcurrentHashMap<>(16));

   /**
    * Map keyed by Class containing CachedIntrospectionResults, strongly held.
    * This variant is being used for cache-safe bean classes.
    * 容器,存储线程安全的bean
    */
   static final ConcurrentMap<Class<?>, CachedIntrospectionResults> strongClassCache =
         new ConcurrentHashMap<>(64);

   /**
    * Map keyed by Class containing CachedIntrospectionResults, softly held.
    * This variant is being used for non-cache-safe bean classes.
    * `容器,存储线程不安全的bean
    */
   static final ConcurrentMap<Class<?>, CachedIntrospectionResults> softClassCache =
         new ConcurrentReferenceHashMap<>(64);

   /**
    * 读取 spring.beaninfo.ignore 配置
    * 读取`IGNORE_BEANINFO_PROPERTY_NAME`后的结果值
    *
    */
   private static final boolean shouldIntrospectorIgnoreBeaninfoClasses =
         SpringProperties.getFlag(IGNORE_BEANINFO_PROPERTY_NAME);

   private static final Log logger = LogFactory.getLog(CachedIntrospectionResults.class);

   /**
    * Stores the BeanInfoFactory instances.
    * `BeanInfoFactory` 容器
    *  */
   private static final List<BeanInfoFactory> beanInfoFactories = SpringFactoriesLoader.loadFactories(
         BeanInfoFactory.class, CachedIntrospectionResults.class.getClassLoader());

   /**
    *  The BeanInfo object for the introspected bean class.
    *  BeanInfo 接口
    * */
   private final BeanInfo beanInfo;

   /**
    * PropertyDescriptor objects keyed by property name String.
    * 属性名称 -> 属性描述对象
    * */
   private final Map<String, PropertyDescriptor> propertyDescriptorCache;

   /**
    * TypeDescriptor objects keyed by PropertyDescriptor.
    * 属性描述对象 -> 类型描述对象
    * */
   private final ConcurrentMap<PropertyDescriptor, TypeDescriptor> typeDescriptorCache;
}
```

</details>





成员变量分析完成之后接着来看`CachedIntrospectionResults`提供的方法



## 方法分析



### 构造函数

- 构造函数中处理了属性值和属性描述对象的绑定关系即`propertyDescriptorCache`变量操作
<details>
<summary>详细代码如下</summary>





```java
private CachedIntrospectionResults(Class<?> beanClass) throws BeansException {
      // 获取 beanInfo
      this.beanInfo = getBeanInfo(beanClass);

      // 对象初始化
      this.propertyDescriptorCache = new LinkedHashMap<>();

      // This call is slow so we do it once.
      // 获取 beanInfo的属性描述对象列表
      PropertyDescriptor[] pds = this.beanInfo.getPropertyDescriptors();
      for (PropertyDescriptor pd : pds) {
         // 数据验证
         if (Class.class == beanClass &&
               ("classLoader".equals(pd.getName()) || "protectionDomain".equals(pd.getName()))) {
            // Ignore Class.getClassLoader() and getProtectionDomain() methods - nobody needs to bind to those
            continue;
         }
         // pd 数据修正
         pd = buildGenericTypeAwarePropertyDescriptor(beanClass, pd);
         // 建立 属性名称和属性描述符的绑定关系
         this.propertyDescriptorCache.put(pd.getName(), pd);
      }

      // Explicitly check implemented interfaces for setter/getter methods as well,
      // in particular for Java 8 default methods...
      Class<?> currClass = beanClass;
      while (currClass != null && currClass != Object.class) {
         // 接口检查
         introspectInterfaces(beanClass, currClass);
         currClass = currClass.getSuperclass();
      }

      this.typeDescriptorCache = new ConcurrentReferenceHashMap<>();
   }
}
```

</details>





在构造函数中海涌到了其他的方法 我们继续分析.



### getBeanInfo

- 方法签名: `org.springframework.beans.CachedIntrospectionResults#getBeanInfo(java.lang.Class<?>)`
- 方法作用: 从`BeanInfoFactory`容器中获取`BeanInfo`对象或者依赖`Introspector`获取`BeanInfo`对象

<details>
<summary>详细代码如下</summary>

```java
private static BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
   for (BeanInfoFactory beanInfoFactory : beanInfoFactories) {
      BeanInfo beanInfo = beanInfoFactory.getBeanInfo(beanClass);
      if (beanInfo != null) {
         return beanInfo;
      }
   }
   return (shouldIntrospectorIgnoreBeaninfoClasses ?
         Introspector.getBeanInfo(beanClass, Introspector.IGNORE_ALL_BEANINFO) :
         Introspector.getBeanInfo(beanClass));
}
```

</details>

- 在这里牵扯到 BeanInfoFactory 相关分析, 详细可以查看[这篇文章](/doc/book/bean/BeanInfoFactory/Spring-BeanInfoFactory.md)

### introspectInterfaces

- 方法签名: `org.springframework.beans.CachedIntrospectionResults#introspectInterfaces`
- 方法作用: 提取类的接口列表 将接口的属性描述接口都获取到, 获取后设置到容器中

<details>
<summary>详细代码如下</summary>



```java
private void introspectInterfaces(Class<?> beanClass, Class<?> currClass) throws IntrospectionException {
   // 获取所有的实现接口
   for (Class<?> ifc : currClass.getInterfaces()) {
      // 判断是否是 java 的接口
      if (!ClassUtils.isJavaLanguageInterface(ifc)) {
         // 获取 PropertyDescriptor 列表循环处理
         for (PropertyDescriptor pd : getBeanInfo(ifc).getPropertyDescriptors()) {
            PropertyDescriptor existingPd = this.propertyDescriptorCache.get(pd.getName());
            if (existingPd == null ||
                  (existingPd.getReadMethod() == null && pd.getReadMethod() != null)) {
               // GenericTypeAwarePropertyDescriptor leniently resolves a set* write method
               // against a declared read method, so we prefer read method descriptors here.
               pd = buildGenericTypeAwarePropertyDescriptor(beanClass, pd);
               this.propertyDescriptorCache.put(pd.getName(), pd);
            }
         }
         introspectInterfaces(ifc, ifc);
      }
   }
}
```

</details>





### buildGenericTypeAwarePropertyDescriptor

- 方法签名: `org.springframework.beans.CachedIntrospectionResults#buildGenericTypeAwarePropertyDescriptor`
- 方法作用
  - 构建通用类型感知属性描述符
  - 创建: `org.springframework.beans.GenericTypeAwarePropertyDescriptor`



- 该方法内容很简单, 直接调用一个new方法就可以了. 

<details>
<summary>详细代码</summary>

```java
private PropertyDescriptor buildGenericTypeAwarePropertyDescriptor(Class<?> beanClass, PropertyDescriptor pd) {
   try {
      return new GenericTypeAwarePropertyDescriptor(beanClass, pd.getName(), pd.getReadMethod(),
            pd.getWriteMethod(), pd.getPropertyEditorClass());
   }
   catch (IntrospectionException ex) {
      throw new FatalBeanException("Failed to re-introspect class [" + beanClass.getName() + "]", ex);
   }
}
```



</details>





在上述方法外还有两个方法, 接下来我们对身下的两个方法进行分析

这里的 `GenericTypeAwarePropertyDescriptor`详细分析可以查看 [这篇文章](/doc/book/bean/GenericTypeAwarePropertyDescriptor/Spring-GenericTypeAwarePropertyDescriptor.md)



### getPropertyDescriptors

- 方法签名: `org.springframework.beans.CachedIntrospectionResults#getPropertyDescriptors`

- 方法作用: 通过 属性名称获取属性描述对象

<details>
<summary>详细代码</summary>

```java
@Nullable
PropertyDescriptor getPropertyDescriptor(String name) {
   PropertyDescriptor pd = this.propertyDescriptorCache.get(name);
   if (pd == null && StringUtils.hasLength(name)) {
      // Same lenient fallback checking as in Property...
      pd = this.propertyDescriptorCache.get(StringUtils.uncapitalize(name));
      if (pd == null) {
         pd = this.propertyDescriptorCache.get(StringUtils.capitalize(name));
      }
   }
   return (pd == null || pd instanceof GenericTypeAwarePropertyDescriptor ? pd :
         buildGenericTypeAwarePropertyDescriptor(getBeanClass(), pd));
}
```

</details>


### getPropertyDescriptor

- 方法签名: `org.springframework.beans.CachedIntrospectionResults#getPropertyDescriptor`

- 方法作用: 获取一个对象的所有 属性描述对象

<details>
<summary>详细代码</summary>

```java
PropertyDescriptor[] getPropertyDescriptors() {
   PropertyDescriptor[] pds = new PropertyDescriptor[this.propertyDescriptorCache.size()];
   int i = 0;
   for (PropertyDescriptor pd : this.propertyDescriptorCache.values()) {
      pds[i] = (pd instanceof GenericTypeAwarePropertyDescriptor ? pd :
            buildGenericTypeAwarePropertyDescriptor(getBeanClass(), pd));
      i++;
   }
   return pds;
}
```

</details>


