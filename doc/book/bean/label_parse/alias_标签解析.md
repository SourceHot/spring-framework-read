# Spring 标签解析 - alias 标签解析
- Author: [HuiFer](https://github.com/huifer)
- 源码阅读仓库: [SourceHot-spring](https://github.com/SourceHot/spring-framework-read)



- 给出一个 Spring 中 alias 标签的使用例子进行逐步分析

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	   xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

	<alias name="apple" alias="apple2"/>
</beans>
```
- 在 Spring 中解析 alias 标签的方法位于: **`org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.processAliasRegistration`**

- 流程概述
    1. 读取 alias 标签的 name 属性和 alias 属性
    1. name 属性验证 alias 属性验证
    1. 别名注册
    1. 别名事件触发

```java
	protected void processAliasRegistration(Element ele) {
		// 获取 name 属性
		String name = ele.getAttribute(NAME_ATTRIBUTE);
		// 获取 alias 属性
		String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
		boolean valid = true;
		// name 属性验证
		if (!StringUtils.hasText(name)) {
			getReaderContext().error("Name must not be empty", ele);
			valid = false;
		}
		// alias 属性验证
		if (!StringUtils.hasText(alias)) {
			getReaderContext().error("Alias must not be empty", ele);
			valid = false;
		}
		if (valid) {
			try {
				getReaderContext().getRegistry().registerAlias(name, alias);
			}
			catch (Exception ex) {
				getReaderContext().error("Failed to register alias '" + alias +
						"' for bean with name '" + name + "'", ele, ex);
			}
			// alias注册事件触发
			getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
		}
	}

```