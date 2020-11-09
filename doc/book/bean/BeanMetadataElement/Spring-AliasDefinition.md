# Spring AliasDefinition
- 类全路径: `org.springframework.beans.factory.parsing.AliasDefinition`

- 对应标签 `<alias>`

  ```xml
<alias name="fromName" alias="toName"/>
  ```

  - 解析xml的处理方法: `org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.processAliasRegistration` . 
  相关事件 `org.springframework.beans.factory.parsing.ReaderEventListener.aliasRegistered`
  这里不做具体展开. 






## 内部变量



```java
/**
 * bean name
 */
private final String beanName;

/**
 * bean 别称
 */
private final String alias;

/**
 * 对象
 */
@Nullable
private final Object source;
```



其他方法均为 `get & set` 不做展开描述