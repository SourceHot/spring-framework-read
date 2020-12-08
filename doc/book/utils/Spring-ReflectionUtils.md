# Spring ReflectionUtils
- 类全路径: `org.springframework.util.ReflectionUtils`
- 反射工具类





## 成员变量

`ReflectionUtils`中的几个成员变量都是比较容易理解的详细请查看下面代码

<details>
<summary>成员变量 详细代码如下</summary>



```java
public abstract class ReflectionUtils {

/**
 * Naming prefix for CGLIB-renamed methods.
 *
 * CGLIB 函数前缀
 * @see #isCglibRenamedMethod
 */
private static final String CGLIB_RENAMED_METHOD_PREFIX = "CGLIB$";

private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

private static final Method[] EMPTY_METHOD_ARRAY = new Method[0];

private static final Field[] EMPTY_FIELD_ARRAY = new Field[0];

private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];


/**
 * Cache for {@link Class#getDeclaredMethods()} plus equivalent default methods
 * from Java 8 based interfaces, allowing for fast iteration.
 *
 * 类的函数缓存
 *
 * class => method 列表
 */
private static final Map<Class<?>, Method[]> declaredMethodsCache = new ConcurrentReferenceHashMap<>(256);

/**
 * Cache for {@link Class#getDeclaredFields()}, allowing for fast iteration.
 * 类的字段缓存
 * class => field 列表
 */
private static final Map<Class<?>, Field[]> declaredFieldsCache = new ConcurrentReferenceHashMap<>(256);
    
}
```



</details>





除了一些常规的变量外还有两个变量, 这两个变量的类型是

1. `MethodFilter`
2. `FieldFilter`

<details>
<summary>特殊变量</summary>

```java
/**
 * Pre-built MethodFilter that matches all non-bridge non-synthetic methods
 * which are not declared on {@code java.lang.Object}.
 *
 * method 过滤器
 * @since 3.0.5
 */
public static final MethodFilter USER_DECLARED_METHODS =
      (method -> !method.isBridge() && !method.isSynthetic());

/**
 * Pre-built FieldFilter that matches all non-static, non-final fields.
 *
 * 字段过滤器
 */
public static final FieldFilter COPYABLE_FIELDS =
      (field -> !(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())));
```





</details>





从名称上可以看出这是两个过滤的接口. 从定义上可以看到更多的过滤条件

```java
!method.isBridge() && !method.isSynthetic()
```



```java
!(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))
```







下面就开始方法分析



## 方法分析



### handleReflectionException

- 方法签名:`org.springframework.util.ReflectionUtils#handleReflectionException`
- 方法作用: 处理反射的异常



`handleReflectionException` 是对反射异常的处理, 内部代码如下

<details>
<summary>handleReflectionException 详细代码如下</summary>



```JAVA
public static void handleReflectionException(Exception ex) {
   if (ex instanceof NoSuchMethodException) {
      throw new IllegalStateException("Method not found: " + ex.getMessage());
   }
   if (ex instanceof IllegalAccessException) {
      throw new IllegalStateException("Could not access method or field: " + ex.getMessage());
   }
   if (ex instanceof InvocationTargetException) {
      handleInvocationTargetException((InvocationTargetException) ex);
   }
   if (ex instanceof RuntimeException) {
      throw (RuntimeException) ex;
   }
   throw new UndeclaredThrowableException(ex);
}
```

</details>





在这里处理异常相关的方法有

1. handleInvocationTargetException
1. rethrowRuntimeException
1. rethrowException

对于异常处理这里就不仔细分析 了, 各位可以去看看源码. 







### accessibleConstructor

- 方法签名: `org.springframework.util.ReflectionUtils#accessibleConstructor`
- 方法作用: 根据给定的类 + 构造函数参数类型 找到 构造函数





```JAVA
public static <T> Constructor<T> accessibleConstructor(Class<T> clazz, Class<?>... parameterTypes)
      throws NoSuchMethodException {

   Constructor<T> ctor = clazz.getDeclaredConstructor(parameterTypes);
   // 设置可访问 setAccessible
   makeAccessible(ctor);
   return ctor;
}
```







在 `accessibleConstructor`中涉及到了`makeAccessible`方法 , 下面直接看这个方法的作用





### makeAccessible

- 方法签名: `org.springframework.util.ReflectionUtils#makeAccessible(java.lang.reflect.Constructor<?>)`

- 方法作用: 设置`Constructor` 的`accessible`属性



<details>
<summary>makeAccessible 详细代码如下</summary>

```java
@SuppressWarnings("deprecation")  // on JDK 9
public static void makeAccessible(Constructor<?> ctor) {
   if ((!Modifier.isPublic(ctor.getModifiers()) ||
         !Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) && !ctor.isAccessible()) {
      ctor.setAccessible(true);
   }
}
```





</details>









### findMethod

- 方法签名: `org.springframework.util.ReflectionUtils#findMethod(java.lang.Class<?>, java.lang.String)`
- 方法作用: 根据类+方法名查询对用的`method`



```java
@Nullable
public static Method findMethod(Class<?> clazz, String name) {
   return findMethod(clazz, name, EMPTY_CLASS_ARRAY);
}
```





不难发现这里还有一个调用方法`findMethod`,内部的`findMethod`才是最终的核心调用，下面对核心的`findMethod`进行分析



### findMethod

- 方法签名: `org.springframework.util.ReflectionUtils#findMethod(java.lang.Class<?>, java.lang.String, java.lang.Class<?>...)`
- 方法作用: 根据类+方法名+参数类型查询对用的`method`



查询逻辑

1. 从当前类出发, 找到所有Method

   ```java
   Method[] methods = (searchType.isInterface() ? searchType.getMethods() :
         getDeclaredMethods(searchType, false));
   ```

2. 循环 Method

   进行匹配条件过滤

   1. 方法名称相同

      ```java
      name.equals(method.getName())
      ```

   2. 参数类型比较

      ```java
      (paramTypes == null || hasSameParams(method, paramTypes))
      ```

   当前类出发如果找到了就返回, 没有找到的话会搜索当前类的父类在进行上述操作. 





<details>
<summary>findMethod 详细代码如下</summary>

```java
@Nullable
public static Method findMethod(Class<?> clazz, String name, @Nullable Class<?>... paramTypes) {
   Assert.notNull(clazz, "Class must not be null");
   Assert.notNull(name, "Method name must not be null");
   Class<?> searchType = clazz;
   while (searchType != null) {
      // 找出 searchType 的所有 method
      Method[] methods = (searchType.isInterface() ? searchType.getMethods() :
            getDeclaredMethods(searchType, false));
      for (Method method : methods) {
         // 1. method 名称相同
         // 2. 参数类型相同
         if (name.equals(method.getName()) && (paramTypes == null || hasSameParams(method, paramTypes))) {
            return method;
         }
      }
      // 查询父类 递归
      searchType = searchType.getSuperclass();
   }
   return null;
}
```

</details>







在这我们又看到了一个新的方法`hasSameParams` 

### hasSameParams

- 方法签名:`org.springframework.util.ReflectionUtils#hasSameParams`
- 方法作用: 判断 method 的参数类型和 该方法的参数是否相同





<details>
<summary>hasSameParams 详细代码如下</summary>

```java
/**
 * 判断 method 的参数类型和 该方法的参数是否相同
 */
private static boolean hasSameParams(Method method, Class<?>[] paramTypes) {
   return (paramTypes.length == method.getParameterCount() &&
         Arrays.equals(paramTypes, method.getParameterTypes()));
}
```

</details>







### invokeMethod

- 方法签名: `org.springframework.util.ReflectionUtils#invokeMethod(java.lang.reflect.Method, java.lang.Object)`

- 方法作用: 执行函数



方法本质即: `Method.invoke`方法调用



<details>
<summary>invokeMethod 详细代码如下</summary>

```java
@Nullable
public static Object invokeMethod(Method method, @Nullable Object target) {
   return invokeMethod(method, target, EMPTY_OBJECT_ARRAY);
}

/**
 * Invoke the specified {@link Method} against the supplied target object with the
 * supplied arguments. The target object can be {@code null} when invoking a
 * static {@link Method}.
 * <p>Thrown exceptions are handled via a call to {@link #handleReflectionException}.
 * @param method the method to invoke
 * @param target the target object to invoke the method on
 * @param args the invocation arguments (may be {@code null})
 * @return the invocation result, if any
 */
@Nullable
public static Object invokeMethod(Method method, @Nullable Object target, @Nullable Object... args) {
   try {
      return method.invoke(target, args);
   }
   catch (Exception ex) {
      handleReflectionException(ex);
   }
   throw new IllegalStateException("Should never get here");
}
```



</details>







### declaresException

- 方法签名: `org.springframework.util.ReflectionUtils#declaresException`
- 方法作用: 方法是否有声明异常(异常是参数传递的)



核心根据 `method.getExceptionTypes()`方法来判断



<details>
<summary>declaresException 详细代码如下</summary>

```JAVA
public static boolean declaresException(Method method, Class<?> exceptionType) {
   Assert.notNull(method, "Method must not be null");
   // 获取异常说明列表
   Class<?>[] declaredExceptions = method.getExceptionTypes();
   for (Class<?> declaredException : declaredExceptions) {
      if (declaredException.isAssignableFrom(exceptionType)) {
         return true;
      }
   }
   return false;
}
```

</details>







### doWithLocalMethods

- 方法签名: `org.springframework.util.ReflectionUtils#doWithLocalMethods`

- 方法作用: 执行`doWith`方法



```java
public static void doWithLocalMethods(Class<?> clazz, MethodCallback mc) {
   // 获取方法列表
   Method[] methods = getDeclaredMethods(clazz, false);
   for (Method method : methods) {
      try {
         mc.doWith(method);
      }
      catch (IllegalAccessException ex) {
         throw new IllegalStateException("Not allowed to access method '" + method.getName() + "': " + ex);
      }
   }
}
```



`doWithLocalMethods`中第一段是一个寻找函数列表的动作. 这个动作处理了那些事项, 下面就对其进行一个分析



### getDeclaredMethods

- 方法签名: `org.springframework.util.ReflectionUtils#getDeclaredMethods(java.lang.Class<?>, boolean)`
- 方法作用: 查询函数列表

在看`getDeclaredMethods`的代码之前我们先认识方法会在那些地方存有

1. 类
2. 接口



`getDeclaredMethods`方法的逻辑就由上述两项组成



<details>
<summary>getDeclaredMethods 详细代码如下</summary>

```java
private static Method[] getDeclaredMethods(Class<?> clazz, boolean defensive) {
   Assert.notNull(clazz, "Class must not be null");
   // 从缓存中获取
   Method[] result = declaredMethodsCache.get(clazz);
   if (result == null) {
      try {
         Method[] declaredMethods = clazz.getDeclaredMethods();
         // 查询接口的方法.
         List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
         if (defaultMethods != null) {
            result = new Method[declaredMethods.length + defaultMethods.size()];
            System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
            int index = declaredMethods.length;
            for (Method defaultMethod : defaultMethods) {
               result[index] = defaultMethod;
               index++;
            }
         }
         else {
            result = declaredMethods;
         }
         declaredMethodsCache.put(clazz, (result.length == 0 ? EMPTY_METHOD_ARRAY : result));
      }
      catch (Throwable ex) {
         throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() +
               "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
      }
   }
   return (result.length == 0 || !defensive) ? result : result.clone();
}
```



</details>





在上面方法中又多了一个方法`findConcreteMethodsOnInterfaces` 这个方法主要就是查询接口中的方法



### findConcreteMethodsOnInterfaces

- 方法签名: `org.springframework.util.ReflectionUtils#findConcreteMethodsOnInterfaces`
- 方法作用: 查询类的实现接口中的方法



<details>
<summary>findConcreteMethodsOnInterfaces 详细代码如下</summary>

```java
@Nullable
private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
   List<Method> result = null;
   // 接口类
   for (Class<?> ifc : clazz.getInterfaces()) {
      // 接口的函数列表
      for (Method ifcMethod : ifc.getMethods()) {
         // 非 abstract 方法
         if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
            if (result == null) {
               result = new ArrayList<>();
            }
            result.add(ifcMethod);
         }
      }
   }
   return result;
}
```



</details>









### getUniqueDeclaredMethods

- 方法签名: `org.springframework.util.ReflectionUtils#getUniqueDeclaredMethods(java.lang.Class<?>, org.springframework.util.ReflectionUtils.MethodFilter)`

- 方法作用: 寻找通过 `MethodFilter` 的 method


<details>
<summary>getUniqueDeclaredMethods</summary>

```java
	public static Method[] getUniqueDeclaredMethods(Class<?> leafClass, @Nullable MethodFilter mf) {
		final List<Method> methods = new ArrayList<>(32);
		doWithMethods(leafClass, method -> {
			boolean knownSignature = false;
			Method methodBeingOverriddenWithCovariantReturnType = null;
			for (Method existingMethod : methods) {
				if (method.getName().equals(existingMethod.getName()) &&
						method.getParameterCount() == existingMethod.getParameterCount() &&
						Arrays.equals(method.getParameterTypes(), existingMethod.getParameterTypes())) {
					// Is this a covariant return type situation?
					if (existingMethod.getReturnType() != method.getReturnType() &&
							existingMethod.getReturnType().isAssignableFrom(method.getReturnType())) {
						methodBeingOverriddenWithCovariantReturnType = existingMethod;
					}
					else {
						knownSignature = true;
					}
					break;
				}
			}
			if (methodBeingOverriddenWithCovariantReturnType != null) {
				methods.remove(methodBeingOverriddenWithCovariantReturnType);
			}
			if (!knownSignature && !isCglibRenamedMethod(method)) {
				methods.add(method);
			}
		}, mf);
		return methods.toArray(EMPTY_METHOD_ARRAY);
	}

```

</details>




### findField

- 方法签名: `org.springframework.util.ReflectionUtils#findField(java.lang.Class<?>, java.lang.String, java.lang.Class<?>)`

- 方法作用: 在类中寻找 类型和字段名称相同的字段(`Field`)


<details>
<summary>findField 详细代码</summary>


```java
	@Nullable
	public static Field findField(Class<?> clazz, @Nullable String name, @Nullable Class<?> type) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.isTrue(name != null || type != null, "Either name or type of the field must be specified");
		Class<?> searchType = clazz;
		while (Object.class != searchType && searchType != null) {
			Field[] fields = getDeclaredFields(searchType);
			for (Field field : fields) {
				// 属性名称是否相同
				// 属性类型是否相同
				if ((name == null || name.equals(field.getName())) &&
						(type == null || type.equals(field.getType()))) {
					return field;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}

```

</details>



### doWithFields
- 方法签名: `org.springframework.util.ReflectionUtils.doWithFields(java.lang.Class<?>, org.springframework.util.ReflectionUtils.FieldCallback, org.springframework.util.ReflectionUtils.FieldFilter)`
- 方法作用: 执行 FieldCallback 的 doWith 方法

<details>
<summary>doWithFields 详细代码</summary>
```java
	public static void doWithFields(Class<?> clazz, FieldCallback fc, @Nullable FieldFilter ff) {
		// Keep backing up the inheritance hierarchy.
		Class<?> targetClass = clazz;
		do {
			Field[] fields = getDeclaredFields(targetClass);
			for (Field field : fields) {
				if (ff != null && !ff.matches(field)) {
					continue;
				}
				try {
					fc.doWith(field);
				}
				catch (IllegalAccessException ex) {
					throw new IllegalStateException("Not allowed to access field '" + field.getName() + "': " + ex);
				}
			}
			targetClass = targetClass.getSuperclass();
		}
		while (targetClass != null && targetClass != Object.class);
	}

```
</details>