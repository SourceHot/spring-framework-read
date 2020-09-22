# Spring BeanDefinitionDocumentReader
- Author: [HuiFer](https://github.com/huifer)
- 源码阅读仓库: [SourceHot-spring](https://github.com/SourceHot/spring-framework-read)

- 全路径: `org.springframework.beans.factory.xml.BeanDefinitionDocumentReader`

```java
/**
 * SPI for parsing an XML document that contains Spring bean definitions.
 * Used by {@link XmlBeanDefinitionReader} for actually parsing a DOM document.
 *
 * <p>Instantiated per document to parse: implementations can hold
 * state in instance variables during the execution of the
 * {@code registerBeanDefinitions} method &mdash; for example, global
 * settings that are defined for all bean definitions in the document.
 *
 *
 * <p>bean定义文档读取</p>
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 18.12.2003
 * @see XmlBeanDefinitionReader#setDocumentReaderClass
 */
public interface BeanDefinitionDocumentReader {

   /**
    * Read bean definitions from the given DOM document and
    * register them with the registry in the given reader context.
    * 注册 bean 定义
    * @param doc the DOM document
    * @param readerContext the current context of the reader
    * (includes the target registry and the resource being parsed)
    * @throws BeanDefinitionStoreException in case of parsing errors
    */
   void registerBeanDefinitions(Document doc, XmlReaderContext readerContext)
         throws BeanDefinitionStoreException;

}
```

- 通过注释我们可以知道这个接口定义的行为如下
    1. 读取xml配置文件, 转换成 Spring bean definition
    2. 将 Spring bean definition 放入内存中
    - 方法作用
        1. 读取给定文档, 转换成 Spring bean definition 放入内存





## registerBeanDefinitions

- 接口定义的方法

```java
@Override
public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
   this.readerContext = readerContext;
   doRegisterBeanDefinitions(doc.getDocumentElement());
}
```

- 最终会交给`doRegisterBeanDefinitions`执行





## createDelegate



```java
this.delegate = createDelegate(getReaderContext(), root, parent)
```

- 参数说明
  1. xml 解析器
  2. element xml节点
  3. 父`BeanDefinitionParserDelegate`

```java
protected final XmlReaderContext getReaderContext() {
   Assert.state(this.readerContext != null, "No XmlReaderContext available");
   return this.readerContext;
}
```

