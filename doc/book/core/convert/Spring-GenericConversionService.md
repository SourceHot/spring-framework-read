# Spring GenericConversionService



- 类全路径: `org.springframework.core.convert.support.GenericConversionService`





## 属性

- 先来看看这个类中的一些属性字段. 

```java
/**
 * General NO-OP converter used when conversion is not required.
 * 没有操作的 convert
 */
private static final GenericConverter NO_OP_CONVERTER = new NoOpConverter("NO_OP");

/**
 * Used as a cache entry when no converter is available.
 * This converter is never returned.
 * 没有操作的 convert
 */
private static final GenericConverter NO_MATCH = new NoOpConverter("NO_MATCH");


/**
 * 转换器集合
 */
private final Converters converters = new Converters();

/**
 * 转换器缓存
 * key: 转换器缓存key
 * value: 转换器
 */
private final Map<ConverterCacheKey, GenericConverter> converterCache = new ConcurrentReferenceHashMap<>(64);
```



## Converters

- 转换器集合
- 类路径: `org.springframework.core.convert.support.GenericConversionService.Converters`

- 字段

```java
/**
 * 转换器接口
 */
private final Set<GenericConverter> globalConverters = new LinkedHashSet<>();

/**
 * 转换器映射
 */
private final Map<ConvertiblePair, ConvertersForPair> converters = new LinkedHashMap<>(36);
```







### add

```java
public void add(GenericConverter converter) {
   // 获取转换对象 ConvertiblePair
   Set<ConvertiblePair> convertibleTypes = converter.getConvertibleTypes();
   // 判空
   if (convertibleTypes == null) {
      Assert.state(converter instanceof ConditionalConverter,
            "Only conditional converters may return null convertible types");
      this.globalConverters.add(converter);
   }
   else {
      for (ConvertiblePair convertiblePair : convertibleTypes) {
         // 获取 ConvertersForPair对象
         ConvertersForPair convertersForPair = getMatchableConverters(convertiblePair);
         convertersForPair.add(converter);
      }
   }
}
```





## getMatchableConverters



```java
private ConvertersForPair getMatchableConverters(ConvertiblePair convertiblePair) {
   // 缓存中获取
   ConvertersForPair convertersForPair = this.converters.get(convertiblePair);
   if (convertersForPair == null) {
      // 创建一个空对象
      convertersForPair = new ConvertersForPair();
      this.converters.put(convertiblePair, convertersForPair);
   }
   return convertersForPair;
}
```







## find



```java
@Nullable
public GenericConverter find(TypeDescriptor sourceType, TypeDescriptor targetType) {
   // Search the full type hierarchy
   // 找到 source 类型的类关系和接口关系
   List<Class<?>> sourceCandidates = getClassHierarchy(sourceType.getType());
   // 找到 target 类型的类关系和接口关系
   List<Class<?>> targetCandidates = getClassHierarchy(targetType.getType());
   // 循环 source 的类列表 和 target 的类列表
   for (Class<?> sourceCandidate : sourceCandidates) {
      for (Class<?> targetCandidate : targetCandidates) {
         // 创建 ConvertiblePair 对象
         ConvertiblePair convertiblePair = new ConvertiblePair(sourceCandidate, targetCandidate);
         // 获取 source + target 的转换接口
         GenericConverter converter = getRegisteredConverter(sourceType, targetType, convertiblePair);
         if (converter != null) {
            return converter;
         }
      }
   }
   return null;
}
```



### getClassHierarchy



```java
private List<Class<?>> getClassHierarchy(Class<?> type) {
   // 层级关系列表
   List<Class<?>> hierarchy = new ArrayList<>(20);
   // 访问列表
   Set<Class<?>> visited = new HashSet<>(20);
   // 在第0个添加 type
   addToClassHierarchy(0, ClassUtils.resolvePrimitiveIfNecessary(type), false, hierarchy, visited);
   // type 是否是 array
   boolean array = type.isArray();

   int i = 0;
   while (i < hierarchy.size()) {
      Class<?> candidate = hierarchy.get(i);
      candidate = (array ? candidate.getComponentType() : ClassUtils.resolvePrimitiveIfNecessary(candidate));
      // 父类
      Class<?> superclass = candidate.getSuperclass();
      if (superclass != null && superclass != Object.class && superclass != Enum.class) {
         // 当前索引位置+1 设置父类
         addToClassHierarchy(i + 1, candidate.getSuperclass(), array, hierarchy, visited);
      }
      // 添加接口类型
      addInterfacesToClassHierarchy(candidate, array, hierarchy, visited);
      i++;
   }

   // 判断是否 enum
   if (Enum.class.isAssignableFrom(type)) {
      // 添加
      addToClassHierarchy(hierarchy.size(), Enum.class, array, hierarchy, visited);
      addToClassHierarchy(hierarchy.size(), Enum.class, false, hierarchy, visited);
      addInterfacesToClassHierarchy(Enum.class, array, hierarchy, visited);
   }

   // 添加
   addToClassHierarchy(hierarchy.size(), Object.class, array, hierarchy, visited);
   addToClassHierarchy(hierarchy.size(), Object.class, false, hierarchy, visited);
   return hierarchy;
}
```





### addInterfacesToClassHierarchy



```java
private void addInterfacesToClassHierarchy(Class<?> type, boolean asArray,
      List<Class<?>> hierarchy, Set<Class<?>> visited) {

   // 获取类的接口列表
   for (Class<?> implementedInterface : type.getInterfaces()) {
      // 添加元素
      addToClassHierarchy(hierarchy.size(), implementedInterface, asArray, hierarchy, visited);
   }
}
```





### addToClassHierarchy

```java
/**
 * 在指定索引位置添加 class
 */
private void addToClassHierarchy(int index, Class<?> type, boolean asArray,
      List<Class<?>> hierarchy, Set<Class<?>> visited) {

   // 是否 array
   if (asArray) {
      // type 如果是array 直接创建空数组
      type = Array.newInstance(type, 0).getClass();
   }
   // 向访问者列表添加类型,判断是否成功
   if (visited.add(type)) {
      // 成功添加后放入具体位置
      hierarchy.add(index, type);
   }
}
```



- 这里会有一个新的对象 ConvertersForPair , 接下来看看这个对象





### getRegisteredConverter

```java
@Nullable
private GenericConverter getRegisteredConverter(TypeDescriptor sourceType,
      TypeDescriptor targetType, ConvertiblePair convertiblePair) {

   // Check specifically registered converters
   // 从map中获取
   ConvertersForPair convertersForPair = this.converters.get(convertiblePair);
   if (convertersForPair != null) {
      // 获取 GenericConverter
      GenericConverter converter = convertersForPair.getConverter(sourceType, targetType);
      if (converter != null) {
         return converter;
      }
   }
   // Check ConditionalConverters for a dynamic match
   for (GenericConverter globalConverter : this.globalConverters) {
      if (((ConditionalConverter) globalConverter).matches(sourceType, targetType)) {
         return globalConverter;
      }
   }
   return null;
}
```





## addConverter

- 添加 converter 接口

```java
@Override
public void addConverter(Converter<?, ?> converter) {
   // 获取解析类型
   ResolvableType[] typeInfo = getRequiredTypeInfo(converter.getClass(), Converter.class);
   if (typeInfo == null && converter instanceof DecoratingProxy) {
      typeInfo = getRequiredTypeInfo(((DecoratingProxy) converter).getDecoratedClass(), Converter.class);
   }
   if (typeInfo == null) {
      throw new IllegalArgumentException("Unable to determine source type <S> and target type <T> for your " +
            "Converter [" + converter.getClass().getName() + "]; does the class parameterize those types?");
   }
   // 添加 converter
   addConverter(new ConverterAdapter(converter, typeInfo[0], typeInfo[1]));
}
```



- 最后一行代码会操作内部属性, 向`Converters` 插入 convert 接口对象

  ```java
  @Override
  public <S, T> void addConverter(Class<S> sourceType, Class<T> targetType, Converter<? super S, ? extends T> converter) {
     addConverter(new ConverterAdapter(
           converter, ResolvableType.forClass(sourceType), ResolvableType.forClass(targetType)));
  }
  ```

  ```java
  @Override
  public void addConverter(GenericConverter converter) {
     this.converters.add(converter);
     invalidateCache();
  }
  ```

  ```java
  /**
   * 转换器集合
   */
  private final Converters converters = new Converters();
  ```





## addConverterFactory

- 添加 convert 工厂

```java
@Override
public void addConverterFactory(ConverterFactory<?, ?> factory) {
   // 获取类型信息
   ResolvableType[] typeInfo = getRequiredTypeInfo(factory.getClass(), ConverterFactory.class);
   // 判断 factory 是不是DecoratingProxy
   if (typeInfo == null && factory instanceof DecoratingProxy) {
      // 其中 DecoratingProxy 可以获取 class
      typeInfo = getRequiredTypeInfo(((DecoratingProxy) factory).getDecoratedClass(), ConverterFactory.class);
   }
   if (typeInfo == null) {
      throw new IllegalArgumentException("Unable to determine source type <S> and target type <T> for your " +
            "ConverterFactory [" + factory.getClass().getName() + "]; does the class parameterize those types?");
   }
   // 添加转换器
   addConverter(new ConverterFactoryAdapter(factory,
         new ConvertiblePair(typeInfo[0].toClass(), typeInfo[1].toClass())));
}
```



## canConvert

- 判断是否可以进行转换



```java
@Override
public boolean canConvert(@Nullable TypeDescriptor sourceType, TypeDescriptor targetType) {
   Assert.notNull(targetType, "Target type to convert to cannot be null");
   if (sourceType == null) {
      return true;
   }
   // 获取 convert , 如果获取到了就是可以转换
   GenericConverter converter = getConverter(sourceType, targetType);
   return (converter != null);
}
```





## getRequiredTypeInfo

- 获取类型信息

```java
@Nullable
private ResolvableType[] getRequiredTypeInfo(Class<?> converterClass, Class<?> genericIfc) {

   // 1. 通过class转换两次得到 ResolvableType
   ResolvableType resolvableType = ResolvableType.forClass(converterClass).as(genericIfc);
   // 2. 获取所有的 ResolvableType
   ResolvableType[] generics = resolvableType.getGenerics();
   if (generics.length < 2) {
      return null;
   }
   // 3. 数据校验准备
   Class<?> sourceType = generics[0].resolve();
   Class<?> targetType = generics[1].resolve();
   if (sourceType == null || targetType == null) {
      return null;
   }
   return generics;
}
```





## getConverter

- 获取 convert 对象



```java
@Nullable
protected GenericConverter getConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
   ConverterCacheKey key = new ConverterCacheKey(sourceType, targetType);
   GenericConverter converter = this.converterCache.get(key);
   if (converter != null) {
      return (converter != NO_MATCH ? converter : null);
   }

   // 找出 converter 对象
   converter = this.converters.find(sourceType, targetType);
   if (converter == null) {
      // 获取默认的 converter
      converter = getDefaultConverter(sourceType, targetType);
   }

   if (converter != null) {
      // 设置缓存
      this.converterCache.put(key, converter);
      return converter;
   }
   // 设置缓存
   this.converterCache.put(key, NO_MATCH);
   return null;
}
```





## convert

- 在 convert 方法中处理了下面几种操作
  1. sourceType 为空的情况下转换结果的处理
  2. 数据验证
  3. 获取转换器
     1. 转换器存在的处理
     2. 转换器不存在的处理

```java
@Override
@Nullable
public Object convert(@Nullable Object source, @Nullable TypeDescriptor sourceType, TypeDescriptor targetType) {
   Assert.notNull(targetType, "Target type to convert to cannot be null");
   if (sourceType == null) {
      Assert.isTrue(source == null, "Source must be [null] if source type == [null]");
      // 处理 sourceType 为空的转换
      return handleResult(null, targetType, convertNullSource(null, targetType));
   }
   // 数据验证
   if (source != null && !sourceType.getObjectType().isInstance(source)) {
      throw new IllegalArgumentException("Source to convert from must be an instance of [" +
            sourceType + "]; instead it was a [" + source.getClass().getName() + "]");
   }
   // 获取转换器接口
   GenericConverter converter = getConverter(sourceType, targetType);
   if (converter != null) {
      // 通过工具获得转换结果
      Object result = ConversionUtils.invokeConverter(converter, source, sourceType, targetType);
      return handleResult(sourceType, targetType, result);
   }
   // 处理找不到 convert 的转换结果
   return handleConverterNotFound(source, sourceType, targetType);
}
```



- 先来看一下 `handleResult` 方法是做了什么处理

## handleResult

- `handleResult` 方法 做 sourceType 和 targetType 的验证后返回 result 对象

```java
@Nullable
private Object handleResult(@Nullable TypeDescriptor sourceType, TypeDescriptor targetType, @Nullable Object result) {
   if (result == null) {
      // 判断 target type
      assertNotPrimitiveTargetType(sourceType, targetType);
   }
   return result;
}
```

- 通过这个方法我们得知 result 直接返回, 那么在调用的时候最后一个参数决定了返回值. 

回到下面代码

```java
if (sourceType == null) {
   Assert.isTrue(source == null, "Source must be [null] if source type == [null]");
   // 处理 sourceType 为空的转换
   return handleResult(null, targetType, convertNullSource(null, targetType));
}
```



可以得知 最后的`convertNullSource(null, targetType)`就是返回值, 一起看一下`convertNullSource` 方法





## convertNullSource

- 类型判断, 通过返回 Optional , 不通过直接返回null

```java
@Nullable
protected Object convertNullSource(@Nullable TypeDescriptor sourceType, TypeDescriptor targetType) {
   if (targetType.getObjectType() == Optional.class) {
      return Optional.empty();
   }
   return null;
}
```



- 还有一个方法关系到返回值

  ```java
  if (converter != null) {
     // 通过工具获得转换结果
     Object result = ConversionUtils.invokeConverter(converter, source, sourceType, targetType);
     return handleResult(sourceType, targetType, result);
  }
  ```
  - 在这个方法中通过一个工具类获取convert结果.详细分析可以查看

最后还有一个找不到converter的处理



## handleConverterNotFound

- 处理 converter 找不到的转换结果



```java
@Nullable
private Object handleConverterNotFound(
      @Nullable Object source, @Nullable TypeDescriptor sourceType, TypeDescriptor targetType) {

   if (source == null) {
      assertNotPrimitiveTargetType(sourceType, targetType);
      return null;
   }
   if ((sourceType == null || sourceType.isAssignableTo(targetType)) &&
         targetType.getObjectType().isInstance(source)) {
      return source;
   }
   throw new ConverterNotFoundException(sourceType, targetType);
}
```





从上述代码可以发现, 通过了各类验证后会直接将 souce 作为返回值返回. 



## ConvertersForPair

- 类全路径: `org.springframework.core.convert.support.GenericConversionService.ConvertersForPair`
- 完整代码如下

```java
private static class ConvertersForPair {

   /**
    * 转换器列表
    */
   private final LinkedList<GenericConverter> converters = new LinkedList<>();

   /**
    * 添加转换器
    * @param converter
    */
   public void add(GenericConverter converter) {
      this.converters.addFirst(converter);
   }

   /**
    * 获取转换器
    * @param sourceType 原类型描述
    * @param targetType 目标类型描述
    * @return
    */
   @Nullable
   public GenericConverter getConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
      for (GenericConverter converter : this.converters) {
         // 判断是否匹配
         if (!(converter instanceof ConditionalGenericConverter) ||
               ((ConditionalGenericConverter) converter).matches(sourceType, targetType)) {
            return converter;
         }
      }
      return null;
   }

   @Override
   public String toString() {
      return StringUtils.collectionToCommaDelimitedString(this.converters);
   }
}
```







## ConverterAdapter

- `org.springframework.core.convert.support.GenericConversionService.ConverterAdapter`



- 内部属性

```java
/**
 * 转换器对象
 */
private final Converter<Object, Object> converter;

/**
 * 元对象和目标对象.
 */
private final ConvertiblePair typeInfo;

/**
 * 解析类型
 */
private final ResolvableType targetType;
```

### matches

- 判断是否匹配

```java
@Override
public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
   // Check raw type first...
   // 判断类型是否一致
   if (this.typeInfo.getTargetType() != targetType.getObjectType()) {
      return false;
   }
   // Full check for complex generic type match required?
   ResolvableType rt = targetType.getResolvableType();
   if (!(rt.getType() instanceof Class) && !rt.isAssignableFrom(this.targetType) &&
         !this.targetType.hasUnresolvableGenerics()) {
      return false;
   }
   // 从 converter 判断是否可以转换
   return !(this.converter instanceof ConditionalConverter) ||
         ((ConditionalConverter) this.converter).matches(sourceType, targetType);
}
```







## ConverterFactoryAdapter

- 内部属性

```java
/**
 * convert 工厂
 */
private final ConverterFactory<Object, Object> converterFactory;

/**
 * 源对象和目标对象
 */
private final ConvertiblePair typeInfo;
```





### matches

```java
@Override
public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
   boolean matches = true;
   if (this.converterFactory instanceof ConditionalConverter) {
      // 是否 matches
      matches = ((ConditionalConverter) this.converterFactory).matches(sourceType, targetType);
   }
   if (matches) {
      // 获取 converter 接口
      Converter<?, ?> converter = this.converterFactory.getConverter(targetType.getType());
      // 类型判断
      if (converter instanceof ConditionalConverter) {
         // 判断是否匹配
         matches = ((ConditionalConverter) converter).matches(sourceType, targetType);
      }
   }
   return matches;
}
```



### convert



```java
@Override
@Nullable
public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
   if (source == null) {
      return convertNullSource(sourceType, targetType);
   }
   // 从工厂中获取一个 convert 接口进行转换
   return this.converterFactory.getConverter(targetType.getObjectType()).convert(source);
}
```





## ConverterCacheKey

- 内部属性

```java
/**
 * source 的类型描述
 */
private final TypeDescriptor sourceType;

/**
 * target 的类型描述
 */
private final TypeDescriptor targetType;
```