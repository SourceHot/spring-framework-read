# Spring ParserConverter
- 类全路径: `org.springframework.format.support.FormattingConversionService.ParserConverter`

- 先看属性

```java


    /**
     * 类型
     */
    private final Class<?> fieldType;

    /**
     * 解析接口
     */
    private final Parser<?> parser;

    /**
     * 转换服务
     */
    private final ConversionService conversionService;

```







## getConvertibleTypes



- 创建了一个Set 集合内部对象为`ConvertiblePair`
  - sourceType = String
  - targetType = fieldType

```java
@Override
public Set<ConvertiblePair> getConvertibleTypes() {
   return Collections.singleton(new ConvertiblePair(String.class, this.fieldType));
}
```





## convert



```java
@Override
@Nullable
public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
   // 强制转换
   String text = (String) source;
   if (!StringUtils.hasText(text)) {
      return null;
   }
   Object result;
   try {
      // 解析器解析
      result = this.parser.parse(text, LocaleContextHolder.getLocale());
   }
   catch (IllegalArgumentException ex) {
      throw ex;
   }
   catch (Throwable ex) {
      throw new IllegalArgumentException("Parse attempt failed for value [" + text + "]", ex);
   }
   // 获取类型描述
   TypeDescriptor resultType = TypeDescriptor.valueOf(result.getClass());
   if (!resultType.isAssignableTo(targetType)) {
      // 转换
      result = this.conversionService.convert(result, resultType, targetType);
   }
   return result;
}
```