# Spring NamedBeanHolder
- 类全路径: `org.springframework.beans.factory.config.NamedBeanHolder`



## 成员变量

1. beanName
2. bean 实例


```java

public class NamedBeanHolder<T> implements NamedBean {
    /**
     * bean name
     */
    private final String beanName;

    /**
     * bean 实例
     */
    private final T beanInstance;

}
```




## 完整代码


```java
public class NamedBeanHolder<T> implements NamedBean {
	/**
	 * bean name
	 */
	private final String beanName;

	/**
	 * bean 实例
	 */
	private final T beanInstance;


	/**
	 * Create a new holder for the given bean name plus instance.
	 * @param beanName the name of the bean
	 * @param beanInstance the corresponding bean instance
	 */
	public NamedBeanHolder(String beanName, T beanInstance) {
		Assert.notNull(beanName, "Bean name must not be null");
		this.beanName = beanName;
		this.beanInstance = beanInstance;
	}


	/**
	 * Return the name of the bean.
	 */
	@Override
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * Return the corresponding bean instance.
	 */
	public T getBeanInstance() {
		return this.beanInstance;
	}

}
```