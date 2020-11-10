# Spring AttributeAccessorSupport
- 类全路径: `org.springframework.core.AttributeAccessorSupport`
- `AttributeAccessorSupport` 实现`AttributeAccessor` 其内部维护一个Map结构用来存储上属性信息. 
    方法也是围绕这一个Map结构进行编写
    
    
    
## 方法分析
### copyAttributesFrom
- 复制属性列表


```java

	protected void copyAttributesFrom(AttributeAccessor source) {
		Assert.notNull(source, "Source must not be null");
		// 获取属性名称列表
		String[] attributeNames = source.attributeNames();
		// 循环属性名称列表
		for (String attributeName : attributeNames) {
			// 设置属性
			// name: 属性名称,value: 从入参中获取属性名称对应的属性值
			setAttribute(attributeName, source.getAttribute(attributeName));
		}
	}


```