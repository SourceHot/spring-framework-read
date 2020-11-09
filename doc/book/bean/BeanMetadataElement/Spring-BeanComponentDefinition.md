# Spring BeanComponentDefinition
- 类全路径: `org.springframework.beans.factory.parsing.BeanComponentDefinition`
- `BeanComponentDefinition` 作为`BeanDefinitionHolder`的补充, 补充 BeanDefinition 中的 连接bean和内部bean

## 成员变量

```java
	/**
	 * bean 定义列表
	 */
	private final BeanDefinition[] innerBeanDefinitions;

	/**
	 *  bean 连接列表
	 */
	private final BeanReference[] beanReferences;
```

- 父类的成员变量

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



## 方法分析

### 构造函数
- 这里着重关注构造函数`org.springframework.beans.factory.parsing.BeanComponentDefinition.BeanComponentDefinition(org.springframework.beans.factory.config.BeanDefinitionHolder)`
    - 构造函数主要将 `BeanDefinitionHolder` 的信息进行解析得到 `BeanDefinition`和`BeanReference`
      详细代码逻辑如下
```java
	public BeanComponentDefinition(BeanDefinitionHolder beanDefinitionHolder) {
		super(beanDefinitionHolder);

		List<BeanDefinition> innerBeans = new ArrayList<>();
		List<BeanReference> references = new ArrayList<>();
		// 从 beanDefinition 中获取 PropertyValues 
		PropertyValues propertyValues = beanDefinitionHolder.getBeanDefinition().getPropertyValues();
		// 循环 PropertyValues 对象中的元素
		for (PropertyValue propertyValue : propertyValues.getPropertyValues()) {
			Object value = propertyValue.getValue();
			// 类型判断 加入各自对应的集合中进行存储
			if (value instanceof BeanDefinitionHolder) {
				innerBeans.add(((BeanDefinitionHolder) value).getBeanDefinition());
			}
			else if (value instanceof BeanDefinition) {
				innerBeans.add((BeanDefinition) value);
			}
			else if (value instanceof BeanReference) {
				references.add((BeanReference) value);
			}
		}
		// 转换成array
		this.innerBeanDefinitions = innerBeans.toArray(new BeanDefinition[0]);
		this.beanReferences = references.toArray(new BeanReference[0]);
	}

```