# Spring AnnotatedGenericBeanDefinition
- 类全路径: `org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition`
- 类图: 
    ![AnnotatedGenericBeanDefinition](./images/AnnotatedGenericBeanDefinition.png)
    
- 从类图上看, `AnnotatedGenericBeanDefinition` 继承 `GenericBeanDefinition` 有关 `GenericBeanDefinition`的分析请查看: [这篇文章](./Spring-GenericBeanDefinition.md)

    
- `AnnotatedGenericBeanDefinition` 在 `GenericBeanDefinition` 的基础上增加了下面两个属性. 

```java

	/**
	 * 注解元信息
	 */
	private final AnnotationMetadata metadata;

	/**
	 * 方法元信息
	 */
	@Nullable
	private MethodMetadata factoryMethodMetadata;
```