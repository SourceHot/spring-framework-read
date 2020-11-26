# Spring AbstractPropertyAccessor
- 类全路径: `org.springframework.beans.AbstractPropertyAccessor`


## 成员变量

```java
	/**
	 * 是否需要提取历史数据
	 */
	private boolean extractOldValueForEditor = false;

	/**
	 * 嵌套注入的时候是否为null的情况下是否需要创建对象
	 */
	private boolean autoGrowNestedPaths = false;
```



## 方法分析

- `AbstractPropertyAccessor` 提供了多个 `setPropertyValues` 方法. 

这里的方法没啥可说的都在子类上面做实现. 

```java
	@Override
	public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid)
			throws BeansException {

		// 异常集合
		List<PropertyAccessException> propertyAccessExceptions = null;
		// 获取属性值列表
		List<PropertyValue> propertyValues = (pvs instanceof MutablePropertyValues ?
				((MutablePropertyValues) pvs).getPropertyValueList() : Arrays.asList(pvs.getPropertyValues()));
		for (PropertyValue pv : propertyValues) {
			try {
				// This method may throw any BeansException, which won't be caught
				// here, if there is a critical failure such as no matching field.
				// We can attempt to deal only with less serious exceptions.
				// 设置属性
				setPropertyValue(pv);
			}
			catch (NotWritablePropertyException ex) {
				if (!ignoreUnknown) {
					throw ex;
				}
				// Otherwise, just ignore it and continue...
			}
			catch (NullValueInNestedPathException ex) {
				if (!ignoreInvalid) {
					throw ex;
				}
				// Otherwise, just ignore it and continue...
			}
			catch (PropertyAccessException ex) {
				if (propertyAccessExceptions == null) {
					propertyAccessExceptions = new ArrayList<>();
				}
				propertyAccessExceptions.add(ex);
			}
		}

		// If we encountered individual exceptions, throw the composite exception.
		if (propertyAccessExceptions != null) {
			PropertyAccessException[] paeArray = propertyAccessExceptions.toArray(new PropertyAccessException[0]);
			throw new PropertyBatchUpdateException(paeArray);
		}
	}

```