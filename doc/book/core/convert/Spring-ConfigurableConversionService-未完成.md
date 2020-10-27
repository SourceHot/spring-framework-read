# Spring ConfigurableConversionService
- 类路径: `org.springframework.core.convert.support.ConfigurableConversionService`

- 类图

![ConfigurableConversionService.png](./images/ConfigurableConversionService.png)



代码如下

```java
public interface ConfigurableConversionService extends ConversionService, ConverterRegistry {

}
```



- 从代码上可以看出这是一个复合接口. 
  - `ConversionService`方法
    1. 是否能够转换
    2. 转换
  - `ConverterRegistry`方法
    1. 添加转换器



实现类分析

[Spring-GenericConversionService](./Spring-GenericConversionService.md)