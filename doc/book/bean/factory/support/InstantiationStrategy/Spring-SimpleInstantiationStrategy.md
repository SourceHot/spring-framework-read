# Spring SimpleInstantiationStrategy
- 类全路径: `org.springframework.beans.factory.support.SimpleInstantiationStrategy`





## 成员变量

在 `SimpleInstantiationStrategy` 中仅有一个变量 用来存储 工厂方法(**`FactoryMethod`**)

```java
    private static final ThreadLocal<Method> currentlyInvokedFactoryMethod = new ThreadLocal<>();
```







## 方法分析

### 从 beanFactory 中返回 对应的 BeanName 实例对象

- 方法签名: `org.springframework.beans.factory.support.SimpleInstantiationStrategy#instantiate(org.springframework.beans.factory.support.RootBeanDefinition, java.lang.String, org.springframework.beans.factory.BeanFactory)`

第一部分代码分析

- 先来看一看第一部分的代码

<details>
<summary>第一部分代码</summary>

```java
     if (!bd.hasMethodOverrides()) {
         // 构造方法
Constructor<?> constructorToUse;
// 锁
         synchronized (bd.constructorArgumentLock) {
           // 提取构造函数
             constructorToUse = (Constructor<?>) bd.resolvedConstructorOrFactoryMethod;
   if (constructorToUse == null) {
      // 获取 bean Class
      final Class<?> clazz = bd.getBeanClass();
      // 确定 类型不是 接口
      if (clazz.isInterface()) {
         throw new BeanInstantiationException(clazz, "Specified class is an interface");
      }
      try {
         // 获取构造方法
         if (System.getSecurityManager() != null) {
            constructorToUse = AccessController.doPrivileged(
                  (PrivilegedExceptionAction<Constructor<?>>) clazz::getDeclaredConstructor);
         }
         else {
            // 获取构造函数
            constructorToUse = clazz.getDeclaredConstructor();
         }
         // 数据设置
         bd.resolvedConstructorOrFactoryMethod = constructorToUse;
      }
      catch (Throwable ex) {
         throw new BeanInstantiationException(clazz, "No default constructor found", ex);
      }
   }
         }
         // 调用构造方法进行构造
         return BeanUtils.instantiateClass(constructorToUse);
     }
```



</details>



- 第一部分代码的核心操作都是在找 **`Constructor`** 构造函数类. 寻找方式有
  1. 从**`BeanDefinition`**中的`resolvedConstructorOrFactoryMethod` 属性获取
  2. 从 `BeanDefinition` 中获取 `BeanClass` 后在通过 `Class`获取



在获取到`Constructor`后交给`BeanUtils.instantiateClass` 进行Bean构造



- 有关`BeanUtils.instantiateClass`的分析可以查看[这篇文章](/doc/book/utils/Spring-BeanUtils-未完成.md)



接下来看第二段代码





```java
else {
    // Must generate CGLIB subclass.
    // cglib 构造 . 本质还是 构造函数创建
    return instantiateWithMethodInjection(bd, beanName, owner);
}
```



第二段代码就涉及到 CGLIB 的对象构造了 ， 即`InstantiationStrategy`的另一个实现类`CglibSubclassingInstantiationStrategy`



关于 `CglibSubclassingInstantiationStrategy`  的分析查看[这篇文章](/doc/book/bean/factory/support/InstantiationStrategy/Spring-CglibSubclassingInstantiationStrategy.md)



<details>
<summary>详细代码如下</summary>

```java
  @Override
  public Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
      // Don't override the class with CGLIB if no overrides.
// 不是 cglib
      // 重写方法列表是否存在
      if (!bd.hasMethodOverrides()) {
          // 构造方法
   Constructor<?> constructorToUse;
   // 锁
          synchronized (bd.constructorArgumentLock) {
           // 提取构造函数
              constructorToUse = (Constructor<?>) bd.resolvedConstructorOrFactoryMethod;
      if (constructorToUse == null) {
         // 获取 bean Class
         final Class<?> clazz = bd.getBeanClass();
         // 确定 类型不是 接口
         if (clazz.isInterface()) {
            throw new BeanInstantiationException(clazz, "Specified class is an interface");
         }
         try {
            // 获取构造方法
            if (System.getSecurityManager() != null) {
               constructorToUse = AccessController.doPrivileged(
                     (PrivilegedExceptionAction<Constructor<?>>) clazz::getDeclaredConstructor);
            }
            else {
               // 获取构造函数
               constructorToUse = clazz.getDeclaredConstructor();
            }
            // 数据设置
            bd.resolvedConstructorOrFactoryMethod = constructorToUse;
         }
         catch (Throwable ex) {
            throw new BeanInstantiationException(clazz, "No default constructor found", ex);
         }
      }
          }
          // 调用构造方法进行构造
          return BeanUtils.instantiateClass(constructorToUse);
      }
      else {
          // Must generate CGLIB subclass.
          // cglib 构造 . 本质还是 构造函数创建
          return instantiateWithMethodInjection(bd, beanName, owner);
      }
  }
```



</details>





### 从 beanFactory 中返回 对应的 BeanName 实例对象, <b>指定构造函数</b>



在上面的分析中我们已经认识到了两种初始化方式.

1. 通过类自身创建
2. 通过 CGLIB 创建



在这个方法中也是这样两种

- 方法签名: `org.springframework.beans.factory.support.SimpleInstantiationStrategy#instantiate(org.springframework.beans.factory.support.RootBeanDefinition, java.lang.String, org.springframework.beans.factory.BeanFactory, java.lang.reflect.Constructor<?>, java.lang.Object...)`





<details>
<summary>第一部分代码</summary>

```java
if (!bd.hasMethodOverrides()) {
          if (System.getSecurityManager() != null) {
              // use own privileged to change accessibility (when security is on)
              AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                  ReflectionUtils.makeAccessible(ctor);
                  return null;
              });
          }
          return BeanUtils.instantiateClass(ctor, args);
      }
```



<details>



第一部分有一次指向了方法`BeanUtils.instantiateClass` 



有关`BeanUtils.instantiateClass`的分析可以查看[这篇文章](/doc/book/utils/Spring-BeanUtils-未完成.md)





第二部分代码



```java
// cglib 的初始化
      else {
          return instantiateWithMethodInjection(bd, beanName, owner, ctor, args);
      }
```







### 从 beanFactory 中返回 对应的 BeanName 实例对象, <b>通过指定的FactoryMethod</b>



- 方法签名: `org.springframework.beans.factory.support.SimpleInstantiationStrategy#instantiate(org.springframework.beans.factory.support.RootBeanDefinition, java.lang.String, org.springframework.beans.factory.BeanFactory, java.lang.Object, java.lang.reflect.Method, java.lang.Object...)`





执行事项

1. 执行参数的`Method` 将结果返回	
2. 设置`currentlyInvokedFactoryMethod`







<details>
<summary>详细代码如下</summary>



```java
        if (System.getSecurityManager() != null) {
// 设置 accessible
AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                ReflectionUtils.makeAccessible(factoryMethod);
                return null;
            });
        }
        else {
           // 设置 accessible
            ReflectionUtils.makeAccessible(factoryMethod);
        }

        // 获取 工厂函数
        Method priorInvokedFactoryMethod = currentlyInvokedFactoryMethod.get();
        try {
           // 找到 factory method 调用执行
            currentlyInvokedFactoryMethod.set(factoryMethod);
            // 反射执行工厂函数
            Object result = factoryMethod.invoke(factoryBean, args);
            if (result == null) {
                result = new NullBean();
            }
            return result;
        }
        finally {
            if (priorInvokedFactoryMethod != null) {
                currentlyInvokedFactoryMethod.set(priorInvokedFactoryMethod);
            }
            else {
                currentlyInvokedFactoryMethod.remove();
            }
        }
```

</details>