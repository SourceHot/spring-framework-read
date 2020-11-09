# Spring RuntimeBeanReference
- 类全路径: `org.springframework.beans.factory.config.RuntimeBeanReference`



## 成员变量

```java

	/**
	 * beanName
	 */
	private final String beanName;

	/**
	 * bean 类型
	 */
	@Nullable
	private final Class<?> beanType;

	/**
	 * 是否走向父类
	 */
	private final boolean toParent;

	/**
	 * 源
	 */
	@Nullable
	private Object source;

```