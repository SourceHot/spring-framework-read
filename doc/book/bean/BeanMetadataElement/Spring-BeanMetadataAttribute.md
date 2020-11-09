# Spring BeanMetadataAttribute


- 对应标签 `<meta>`

  ```xml
<meta key="" value=""/>
  ```

  - 解析xml的处理方法: `org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.parseMetaElements` 这里不做具体展开



## 成员变量


```java
	/***
	 * 属性值名称
	 */
	private final String name;

	/**
	 * 属性值
	 */
	@Nullable
	private final Object value;

	@Nullable
	private Object source;

```