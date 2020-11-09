# Spring ImportDefinition
- 类全路径: `org.springframework.beans.factory.parsing.ImportDefinition`

- 对应标签 `<alias>`

  ```xml
   <import resource="com/bank/service/${customer}-config.xml"/>
  ```

  - 解析xml的处理方法: `org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.importBeanDefinitionResource` . 
  相关事件 `org.springframework.beans.factory.parsing.ReaderEventListener.importProcessed`
  这里不做具体展开. 






## 内部变量



```java
/**
 * import 标签的resource 值
 */
private final String importedResource;

/**
 * 资源数组
 */
@Nullable
private final Resource[] actualResources;


@Nullable
private final Object source;
```



其他方法均为 `get & set` 不做展开描述