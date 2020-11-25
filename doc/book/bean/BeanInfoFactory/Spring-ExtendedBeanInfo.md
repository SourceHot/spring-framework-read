# Spring ExtendedBeanInfo
- 类全路径: `org.springframework.beans.ExtendedBeanInfo`





## 成员变量

在`ExtendedBeanInfo`存在两个成员变量,让我们来看一下成员变量的信息. 

1. BeanInfo 接口
2. 属性描述集合

<details>
<summary>详细代码</summary>

```java
/**
 * beanInfo 接口
 */
private final BeanInfo delegate;

/**
 * 属性描述集合
 */
private final Set<PropertyDescriptor> propertyDescriptors = new TreeSet<>(new PropertyDescriptorComparator());
```

</details>



## 方法分析



成员变量分析完成了，接下来让我们来看看`ExtendedBeanInfo` 的方法



### 构造函数

- 方法签名: `org.springframework.beans.ExtendedBeanInfo#ExtendedBeanInfo`

在构造函数中主要处理了 `BeanInfo` 对象和 `propertyDescriptors`. 

首先进入第一部分代码的分析





<details>
<summary>第一部分代码</summary>

```java
// 变量赋值
this.delegate = delegate;
// 获取属性列表
for (PropertyDescriptor pd : delegate.getPropertyDescriptors()) {
   try {
      // 放入属性描述符容器中
      this.propertyDescriptors.add(pd instanceof IndexedPropertyDescriptor ?
            new SimpleIndexedPropertyDescriptor((IndexedPropertyDescriptor) pd) :
            new SimplePropertyDescriptor(pd));
   }
   catch (IntrospectionException ex) {
      // Probably simply a method that wasn't meant to follow the JavaBeans pattern...
      if (logger.isDebugEnabled()) {
         logger.debug("Ignoring invalid bean property '" + pd.getName() + "': " + ex.getMessage());
      }
   }
}
```



</details>



第一部分代码主要做了 `beanInfo`的赋值 以及 `BeanInfo` 接口中得到的属性描述符(`PropertyDescriptor`)处理

<details>
<summary>属性描述符处理</summary>

```java
this.propertyDescriptors.add(pd instanceof IndexedPropertyDescriptor ?
      new SimpleIndexedPropertyDescriptor((IndexedPropertyDescriptor) pd) :
      new SimplePropertyDescriptor(pd));
```

</details>



在属性描述符处理的过程中涉及到两个类

1. `SimpleIndexedPropertyDescriptor`
2. ·`SimplePropertyDescriptor`







## SimpleIndexedPropertyDescriptor

- 类全路径: `org.springframework.beans.ExtendedBeanInfo.SimpleIndexedPropertyDescriptor`

### 成员变量

- `SimpleIndexedPropertyDescriptor` 继承自`IndexedPropertyDescriptor` 其成员变量是对`IndexedPropertyDescriptor`的属性拓展. 





<details>
<summary>成员变量详情</summary>

```java
static class SimpleIndexedPropertyDescriptor extends IndexedPropertyDescriptor {
   /**
    * 可读函数
    */
   @Nullable
   private Method readMethod;

   /**
    * 可写函数
    */
   @Nullable
   private Method writeMethod;

   /**
    * 属性类型
    */
   @Nullable
   private Class<?> propertyType;

   @Nullable
   private Method indexedReadMethod;

   @Nullable
   private Method indexedWriteMethod;

   @Nullable
   private Class<?> indexedPropertyType;

   @Nullable
   private Class<?> propertyEditorClass;
}
```

</details>







## SimplePropertyDescriptor

- 类全路径: `org.springframework.beans.ExtendedBeanInfo.SimplePropertyDescriptor`



### 成员变量

- `SimplePropertyDescriptor` 继承自`PropertyDescriptor` 其成员变量是对`PropertyDescriptor`的属性拓展. 





<details>
<summary>成员变量详情</summary>

```java
static class SimplePropertyDescriptor extends PropertyDescriptor {
   /**
    * 可读函数
    */
   @Nullable
   private Method readMethod;

   /**
    * 可写函数
    */
   @Nullable
   private Method writeMethod;

   /**
    * 属性类型
    */
   @Nullable
   private Class<?> propertyType;

   /**
    * 属性编辑器类型
    */
   @Nullable
   private Class<?> propertyEditorClass;
}
```

</details>





认识了`SimpleIndexedPropertyDescriptor` 和 `SimplePropertyDescriptor`后对第一段代码的内容也了解完成了. 接下来就进入第二段代码的分析了



<details>
<summary>第二段代码</summary>



```java
// 函数描述符
MethodDescriptor[] methodDescriptors = delegate.getMethodDescriptors();
if (methodDescriptors != null) {
   // 寻找函数进行循环
   for (Method method : findCandidateWriteMethods(methodDescriptors)) {
      try {
         // 处理可写函数
         handleCandidateWriteMethod(method);
      }
      catch (IntrospectionException ex) {
         // We're only trying to find candidates, can easily ignore extra ones here...
         if (logger.isDebugEnabled()) {
            logger.debug("Ignoring candidate write method [" + method + "]: " + ex.getMessage());
         }
      }
   }
}
```

</details>



第二段代码依靠 `BeanInfo` 中的 `getMethodDescriptors` 方法来获得 函数描述符`MethodDescriptor`



接着就是两个函数的处理



### findCandidateWriteMethods

- 方法签名: `org.springframework.beans.ExtendedBeanInfo#findCandidateWriteMethods`
- 方法作用: 查询可写方法

<details>
<summary>详细代码</summary>

```java
private List<Method> findCandidateWriteMethods(MethodDescriptor[] methodDescriptors) {
   List<Method> matches = new ArrayList<>();
   for (MethodDescriptor methodDescriptor : methodDescriptors) {
      Method method = methodDescriptor.getMethod();
      if (isCandidateWriteMethod(method)) {
         matches.add(method);
      }
   }
   // Sort non-void returning write methods to guard against the ill effects of
   // non-deterministic sorting of methods returned from Class#getDeclaredMethods
   // under JDK 7. See https://bugs.java.com/view_bug.do?bug_id=7023180
   matches.sort((m1, m2) -> m2.toString().compareTo(m1.toString()));
   return matches;
}
```

</details>



这里涉及到一个函数`isCandidateWriteMethod` 这个函数才是真正判断是否是可写的方法





### isCandidateWriteMethod

- 方法签名: `org.springframework.beans.ExtendedBeanInfo#isCandidateWriteMethod`
- 方法作用: 判断函数是否可写



<details>
<summary>详细代码如下</summary>

```java
/**
 * 判断函数是否可写
 */
public static boolean isCandidateWriteMethod(Method method) {
   String methodName = method.getName();
   int nParams = method.getParameterCount();
   return (methodName.length() > 3 && methodName.startsWith("set") && Modifier.isPublic(method.getModifiers()) &&
         (!void.class.isAssignableFrom(method.getReturnType()) || Modifier.isStatic(method.getModifiers())) &&
         (nParams == 1 || (nParams == 2 && int.class == method.getParameterTypes()[0])));
}
```



</details>



这段代码可以简单理解成是否包含 `set` 字样.  然后返回值等的验证. 



### handleCandidateWriteMethod

- 方法签名: `org.springframework.beans.ExtendedBeanInfo#handleCandidateWriteMethod`
- 方法作用: 处理可写函数

方法有点长 我们就一部分一部分查看



第一部分代码是数据准备阶段

<details>
<summary>详细代码如下</summary>

```java
// 参数数量
int nParams = method.getParameterCount();
// 属性名称
String propertyName = propertyNameFor(method);
// 参数类型
Class<?> propertyType = method.getParameterTypes()[nParams - 1];
// 寻找属性描述符
PropertyDescriptor existingPd = findExistingPropertyDescriptor(propertyName, propertyType);
```

</details>



关注两个方法`propertyNameFor` 和`findExistingPropertyDescriptor`

### propertyNameFor

- 方法签名: `org.springframework.beans.ExtendedBeanInfo#propertyNameFor`

- 方法作用: 获取属性名称. 

例如 现在有下面对象 

```java
class Persion{

    private String name;
    public void setName(String name){
        this.name = name;
    }

}
```

通过`propertyNameFor` 将 `set`函数放入后可以得到 `name` 这个属性



<details>
<summary>详细代码如下</summary>

```JAVA
/**
 * 提取函数的后半段字符 .
 * getName => Name
 * setName => Name
 * @param method
 * @return
 */
private String propertyNameFor(Method method) {
   return Introspector.decapitalize(method.getName().substring(3));
}
```

</details>









### findExistingPropertyDescriptor

- 方法签名: `org.springframework.beans.ExtendedBeanInfo#findExistingPropertyDescriptor`
- 方法作用: 查询属性描述符对象`PropertyDescriptor`



<details>
<summary>详细代码如下</summary>



```java
@Nullable
private PropertyDescriptor findExistingPropertyDescriptor(String propertyName, Class<?> propertyType) {
   // 循环现有的 属性描述符列表
   for (PropertyDescriptor pd : this.propertyDescriptors) {
      // 待测类型
      final Class<?> candidateType;
      // 属性名称
      final String candidateName = pd.getName();
      // 类型判断
      if (pd instanceof IndexedPropertyDescriptor) {
         IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
         // 从IndexedPropertyDescriptor获取属性类型
         candidateType = ipd.getIndexedPropertyType();
         if (candidateName.equals(propertyName) &&
               (candidateType.equals(propertyType) || candidateType.equals(propertyType.getComponentType()))) {
            return pd;
         }
      }
      else {
         // 设置 待测类型. 从属性描述符中虎丘属性类型
         candidateType = pd.getPropertyType();
         if (candidateName.equals(propertyName) &&
               (candidateType.equals(propertyType) || propertyType.equals(candidateType.getComponentType()))) {
            return pd;
         }
      }
   }
   return null;
}
```



</details>





到这里`handleCandidateWriteMethod`的第一部分代码分析完成, 接下来进行第二部分. 第二部分主要处理值设置的问题





<details>
<summary>第二部分详细代码如下</summary>

```java
// 参数数量等于1的情况处理
if (nParams == 1) {
   if (existingPd == null) {
      this.propertyDescriptors.add(new SimplePropertyDescriptor(propertyName, null, method));
   }
   else {
      existingPd.setWriteMethod(method);
   }
}
// 参数数量等于2的情况处理
else if (nParams == 2) {
   if (existingPd == null) {
      this.propertyDescriptors.add(
            new SimpleIndexedPropertyDescriptor(propertyName, null, null, null, method));
   }
   else if (existingPd instanceof IndexedPropertyDescriptor) {
      ((IndexedPropertyDescriptor) existingPd).setIndexedWriteMethod(method);
   }
   else {
      this.propertyDescriptors.remove(existingPd);
      this.propertyDescriptors.add(new SimpleIndexedPropertyDescriptor(
            propertyName, existingPd.getReadMethod(), existingPd.getWriteMethod(), null, method));
   }
}
else {
   throw new IllegalArgumentException("Write method must have exactly 1 or 2 parameters: " + method);
}
```



</details>