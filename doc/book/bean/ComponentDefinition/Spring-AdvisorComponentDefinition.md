# Spring AdvisorComponentDefinition
- 类全路径: `org.springframework.aop.config.AdvisorComponentDefinition`



## 成员变量

```java
	/**
	 * 顾问 bean 名称
	 */
	private final String advisorBeanName;

	/**
	 * 顾问bean定义
	 */
	private final BeanDefinition advisorDefinition;

	/**
	 * 顾问描述
	 */
	private final String description;

	/**
	 * 外联的bean定义
	 */
	private final BeanReference[] beanReferences;

	/**
	 * bean定义列表
	 */
	private final BeanDefinition[] beanDefinitions;
```