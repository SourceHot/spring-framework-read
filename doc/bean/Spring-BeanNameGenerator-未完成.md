# Spring BeanNameGenerator
- Author: [HuiFer](https://github.com/huifer)
- 源码阅读仓库: [SourceHot-spring](https://github.com/SourceHot/spring-framework-read)


- `org.springframework.beans.factory.support.BeanNameGenerator`


```java
public interface BeanNameGenerator {

	/**
	 * Generate a bean name for the given bean definition.
	 * 生成 beanName
	 * @param definition the bean definition to generate a name for
	 * @param registry the bean definition registry that the given definition
	 * is supposed to be registered with
	 * @return the generated bean name
	 */
	String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry);

}
```