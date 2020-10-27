# Spring ConversionUtils
- 类全路径: `org.springframework.core.convert.support.ConversionUtils`
- ConversionUtils 拥有三个方法
    1. invokeConverter: 做类型转换
    2. canConvertElements: 判断是否可以进行转换
    3. getEnumType: 获取枚举类型
    
    

接下来我们详细查看一下方法

## invokeConverter

- 通过 GenericConverter 进行方法调用获取返回值

```java
@Nullable
public static Object invokeConverter(GenericConverter converter, @Nullable Object source,
      TypeDescriptor sourceType, TypeDescriptor targetType) {

   try {
      // converter 方法调用
      return converter.convert(source, sourceType, targetType);
   }
   catch (ConversionFailedException ex) {
      throw ex;
   }
   catch (Throwable ex) {
      throw new ConversionFailedException(sourceType, targetType, source, ex);
   }
}
```





## canConvertElements

- 判断是否可以进行转换



```java
public static boolean canConvertElements(@Nullable TypeDescriptor sourceElementType,
      @Nullable TypeDescriptor targetElementType, ConversionService conversionService) {

   if (targetElementType == null) {
      // yes
      return true;
   }
   if (sourceElementType == null) {
      // maybe
      return true;
   }
   if (conversionService.canConvert(sourceElementType, targetElementType)) {
      // yes
      return true;
   }
   // 右侧类型是否可以赋值给左侧类型
   // 是否是父子类
   if (ClassUtils.isAssignable(sourceElementType.getType(), targetElementType.getType())) {
      // maybe
      return true;
   }
   // no
   return false;
}
```





## getEnumType

- 获取枚举类型

```java
public static Class<?> getEnumType(Class<?> targetType) {
   Class<?> enumType = targetType;
   while (enumType != null && !enumType.isEnum()) {
      enumType = enumType.getSuperclass();
   }
   Assert.notNull(enumType, () -> "The target type " + targetType.getName() + " does not refer to an enum");
   return enumType;
}
```