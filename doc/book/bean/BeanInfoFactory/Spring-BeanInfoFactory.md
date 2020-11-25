# Spring BeanInfoFactory
- 类全路径: `org.springframework.beans.BeanInfoFactory`


BeanInfoFactory 主要作用是将`BeanInfo`接口返回

代码如下

```java
public interface BeanInfoFactory {

	/**
	 * Return the bean info for the given class, if supported.
	 * @param beanClass the bean class
	 * @return the BeanInfo, or {@code null} if the given class is not supported
	 * @throws IntrospectionException in case of exceptions
	 */
	@Nullable
	BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException;

}

```

这个接口只有一个实现 `ExtendedBeanInfoFactory` 相关分析可以看: [这篇文章](Spring-ExtendedBeanInfoFactory.md)