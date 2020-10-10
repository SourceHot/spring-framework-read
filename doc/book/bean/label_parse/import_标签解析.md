# Spring 标签解析 - import 标签解析
- Author: [HuiFer](https://github.com/huifer)
- 源码阅读仓库: [SourceHot-spring](https://github.com/SourceHot/spring-framework-read)


- 给出一个 Spring 中 import 标签的使用例子进行逐步分析

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	   xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

	<import resource="../beans/spring-lookup-method.xml"/>
</beans>
```


- 在 Spring 中解析 import 标签的方法位于: **`org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.importBeanDefinitionResource`**

- 流程概述
    1. 读取 import 标签的 resource 属性
        1. 判断是否存在
    1. 解析 resource 属性
    1. 获取 import 数量 
    1. 获取 资源对象 Resource 放入内存
    1. 触发事件  fireImportProcessed 
        1. 将 import 信息(ImportDefinition) 放入集合中




```java
	/**
	 * Parse an "import" element and load the bean definitions
	 * from the given resource into the bean factory.
	 *
	 * 解析 import 标签
	 */
	protected void importBeanDefinitionResource(Element ele) {
		// 获取 resource 属性
		String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
		if (!StringUtils.hasText(location)) {
			getReaderContext().error("Resource location must not be empty", ele);
			return;
		}

		// Resolve system properties: e.g. "${user.dir}"
		// 处理配置文件占位符
		location = getReaderContext().getEnvironment().resolveRequiredPlaceholders(location);

		// 资源集合
		Set<Resource> actualResources = new LinkedHashSet<>(4);

		// Discover whether the location is an absolute or relative URI
		// 是不是绝对地址
		boolean absoluteLocation = false;
		try {
			// 1. 判断是否为 url
			// 2. 通过转换成URI判断是否是绝对地址
			absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
		}
		catch (URISyntaxException ex) {
			// cannot convert to an URI, considering the location relative
			// unless it is the well-known Spring prefix "classpath*:"
		}

		// Absolute or relative?
		if (absoluteLocation) {
			try {
				// 获取 import 的数量
				int importCount = getReaderContext().getReader().loadBeanDefinitions(location, actualResources);
				if (logger.isTraceEnabled()) {
					logger.trace("Imported " + importCount + " bean definitions from URL location [" + location + "]");
				}
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to import bean definitions from URL location [" + location + "]", ele, ex);
			}
		}
		else {
			// No URL -> considering resource location as relative to the current file.
			try {
				// import 的数量
				int importCount;
				// 资源信息
				Resource relativeResource = getReaderContext().getResource().createRelative(location);
				// 资源是否存在
				if (relativeResource.exists()) {
					// 确定加载的bean定义数量
					importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);
					// 加入资源集合
					actualResources.add(relativeResource);
				}
				// 资源不存在处理方案
				else {
					// 获取资源URL的数据
					String baseLocation = getReaderContext().getResource().getURL().toString();
					// 获取import数量
					importCount = getReaderContext().getReader().loadBeanDefinitions(
							StringUtils.applyRelativePath(baseLocation, location), actualResources);
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Imported " + importCount + " bean definitions from relative location [" + location + "]");
				}
			}
			catch (IOException ex) {
				getReaderContext().error("Failed to resolve current resource location", ele, ex);
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to import bean definitions from relative location [" + location + "]", ele, ex);
			}
		}
		Resource[] actResArray = actualResources.toArray(new Resource[0]);
		// 唤醒 import 处理事件
		getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
	}

```