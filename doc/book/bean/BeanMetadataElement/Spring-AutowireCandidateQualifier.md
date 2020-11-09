# Spring AutowireCandidateQualifier
- 类全路径: `org.springframework.beans.factory.support.AutowireCandidateQualifier`

- 对应标签: 

```xml
 <qualifier value="main"/>
```

  - 解析xml的处理方法: `org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.parseQualifierElement` 这里不做具体展开



## 成员变量


```java


	/**
	 * The name of the key used to store the value.
	 */
	public static final String VALUE_KEY = "value";

	/**
	 * 注入的类型
	 */
	private final String typeName;

```