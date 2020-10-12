# Spring Converter
- 类全路径: `org.springframework.core.convert.converter.Converter`
- 类作用: 转换接口


```java
@FunctionalInterface
public interface Converter<S, T> {

	/**
	 * Convert the source object of type {@code S} to target type {@code T}.
	 * 从 source 转换成 t 
	 * @param source the source object to convert, which must be an instance of {@code S} (never {@code null})
	 * @return the converted object, which must be an instance of {@code T} (potentially {@code null})
	 * @throws IllegalArgumentException if the source cannot be converted to the desired target type
	 */
	@Nullable
	T convert(S source);

}

```

- 类图

![converter](./images/Converter.png)





接下来我们看几个类来了解一下具体的实现

## ObjectToStringConverter

```java
final class ObjectToStringConverter implements Converter<Object, String> {

   @Override
   public String convert(Object source) {
      return source.toString();
   }

}
```



## StringToCharsetConverter



```java
class StringToCharsetConverter implements Converter<String, Charset> {

   @Override
   public Charset convert(String source) {
      return Charset.forName(source);
   }

}
```





- 其他的子类各位读者请自行阅读. 

  包路径: `org.springframework.core.convert.support`