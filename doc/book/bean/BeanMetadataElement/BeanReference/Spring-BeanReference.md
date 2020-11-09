# Spring BeanReference
- 类全路径: `org.springframework.beans.factory.config.BeanReference`
- bean 连接接口. 返回 beanName 



```java
public interface BeanReference extends BeanMetadataElement {

	/**
	 * Return the target bean name that this reference points to (never {@code null}).
	 * 返回引用的beanName
	 */
	String getBeanName();

}

```



对应标签的属性值: `ref=""`