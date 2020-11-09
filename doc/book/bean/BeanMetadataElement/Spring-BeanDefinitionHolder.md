# Spring BeanDefinitionHolder
- 类全路径: `org.springframework.beans.factory.config.BeanDefinitionHolder`
- 对应标签 : `<bean/>`
- 处理方法: `org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.parseBeanDefinitionElement(org.w3c.dom.Element, org.springframework.beans.factory.config.BeanDefinition)`





## 成员变量

- `BeanDefinitionHolder` 的成员变量信息

```java
/**
 * bean 定义信息
 */
private final BeanDefinition beanDefinition;

/**
 * bean name
 */
private final String beanName;

/**
 * 别名列表
 */
@Nullable
private final String[] aliases;
```

