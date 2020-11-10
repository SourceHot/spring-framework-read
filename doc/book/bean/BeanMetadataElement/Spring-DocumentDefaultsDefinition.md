# Spring DocumentDefaultsDefinition
- 类全路径: `org.springframework.beans.factory.xml.DocumentDefaultsDefinition`

- `DocumentDefaultsDefinition` 作用主要是和 `<bean/>` 标签做一个绑定关系或者说是对象映射, 接下来我们了解一下属性

## 内部变量


```java

	/**
	 * true or false
	 * 是否懒加载
	 */
	@Nullable
	private String lazyInit;

	/**
	 * true or false
	 */
	@Nullable
	private String merge;

	/**
	 * no or byName or byType
	 * 自动注入方式
	 */
	@Nullable
	private String autowire;

	/**
	 * default-autowire-candidates
	 *
	 */
	@Nullable
	private String autowireCandidates;

	/**
	 * 实例化方法
	 */
	@Nullable
	private String initMethod;

	/**
	 * 摧毁方法
	 */
	@Nullable
	private String destroyMethod;

	/**
	 * 源对象
	 */
	@Nullable
	private Object source;
```