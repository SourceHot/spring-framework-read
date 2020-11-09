# Spring TypedStringValue
- 类全路径: `org.springframework.beans.factory.config.TypedStringValue`
- 对应标签 `<value/>`


## 成员变量
```java
	/**
	 * 值
	 */
	@Nullable
	private String value;

	/**
	 * 目标对象类型
	 */
	@Nullable
	private volatile Object targetType;

	/**
	 * 源对象
	 */
	@Nullable
	private Object source;

	/**
	 * 类型名称
	 */
	@Nullable
	private String specifiedTypeName;

	/**
	 * 值是否是动态的
	 */
	private volatile boolean dynamic;
```