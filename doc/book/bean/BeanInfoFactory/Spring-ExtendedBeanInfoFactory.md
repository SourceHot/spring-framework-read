# Spring ExtendedBeanInfoFactory
- 类全路径: `org.springframework.beans.ExtendedBeanInfoFactory`

`ExtendedBeanInfo` 作为`BeanInfoFactory`的实现, 主要目的和接口`BeanInfoFactory`一样. 返回一个 BeanInfo 接口对象. 
    接下来我们对这个方法进行分析
    
    
    
## 方法分析

### getBeanInfo



<details>
<summary>getBeanInfo 详情</summary>

    
```java
	@Override
	@Nullable
	public BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
		return (supports(beanClass) ? new ExtendedBeanInfo(Introspector.getBeanInfo(beanClass)) : null);
	}
```

</details>

在 `getBeanInfo` 中出现了 `supports`来判断是否支持创建`BeanInfo`接口. 以及一个 `BeanInfo`接口的实现类`ExtendedBeanInfo`
    有关 `ExtendedBeanInfo` 可以查看: [这篇文章](Spring-ExtendedBeanInfo.md)
    
    


### supports

<details>
<summary>supports 详情</summary>

    

```java
	private boolean supports(Class<?> beanClass) {
		for (Method method : beanClass.getMethods()) {
			if (ExtendedBeanInfo.isCandidateWriteMethod(method)) {
				return true;
			}
		}
		return false;
	}

```

</details>


在 `supports` 出现的判断依据是 `org.springframework.beans.ExtendedBeanInfo.isCandidateWriteMethod`
    起作用是判断是否可写. 即 set 方法
    

<details>
<summary>supports 详情</summary>

```java
public static boolean isCandidateWriteMethod(Method method) {
		String methodName = method.getName();
		int nParams = method.getParameterCount();
		return (methodName.length() > 3 && methodName.startsWith("set") && Modifier.isPublic(method.getModifiers()) &&
				(!void.class.isAssignableFrom(method.getReturnType()) || Modifier.isStatic(method.getModifiers())) &&
				(nParams == 1 || (nParams == 2 && int.class == method.getParameterTypes()[0])));
	}
```


</details>
