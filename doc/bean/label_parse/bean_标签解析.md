# Spring bean 标签解析
- Author: [HuiFer](https://github.com/huifer)
- 源码阅读仓库: [SourceHot-spring](https://github.com/SourceHot/spring-framework-read)


- 在 Spring 中解析 bean 标签的方法位于: **`org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.processBeanDefinition`**


- 流程概述
    1. 通过 BeanDefinitionParserDelegate 进行标签解析
    2. bean definition 注册
    3. 发布 component 注册事件



```java

	protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
		// 创建 bean definition
		BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
		if (bdHolder != null) {
			// bean definition 装饰
			bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
			try {
				// Register the final decorated instance.
				// 注册beanDefinition
				BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to register bean definition with name '" +
						bdHolder.getBeanName() + "'", ele, ex);
			}
			// Send registration event.
			// component注册事件触发
			getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
		}
	}

```
