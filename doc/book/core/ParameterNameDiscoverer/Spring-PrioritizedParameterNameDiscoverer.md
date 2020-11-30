# Spring PrioritizedParameterNameDiscoverer
- 类全路径: `org.springframework.core.PrioritizedParameterNameDiscoverer`


## 成员变量

- 在 `PrioritizedParameterNameDiscoverer` 中有一个 List 对象存储了接口 `ParameterNameDiscoverer` , 这个对象在后续的方法调用中有一定的作用

```java
	private final List<ParameterNameDiscoverer> parameterNameDiscoverers = new LinkedList<>();
```


## 方法分析

- 在成员变量中有一个集合 , 这里的两个方法都会调用对应的方法, 直到获取到结果

### getParameterNames

```java
	@Override
	@Nullable
	public String[] getParameterNames(Method method) {
		for (ParameterNameDiscoverer pnd : this.parameterNameDiscoverers) {
			String[] result = pnd.getParameterNames(method);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

```

### getParameterNames

```java
	@Override
	@Nullable
	public String[] getParameterNames(Constructor<?> ctor) {
		for (ParameterNameDiscoverer pnd : this.parameterNameDiscoverers) {
			String[] result = pnd.getParameterNames(ctor);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

```