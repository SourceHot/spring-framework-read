# Spring SimpleAutowireCandidateResolver
- 类全路径: `org.springframework.beans.factory.support.SimpleAutowireCandidateResolver`


## 方法分析


### isAutowireCandidate
- 方法签名: `org.springframework.beans.factory.support.SimpleAutowireCandidateResolver.isAutowireCandidate`
- 从 BeanDefinitionHolder 中的 BeanDefinition 中获取 `isAutowireCandidate`

<details>
<summary>isAutowireCandidate 详细代码</summary>

```java
	@Override
	public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
		return bdHolder.getBeanDefinition().isAutowireCandidate();
	}
```
</details>


### isRequired
- 方法签名: `org.springframework.beans.factory.support.SimpleAutowireCandidateResolver.isRequired`
- 从依赖描述对象中获取是否是必须的

<details>
<summary>isRequired 详细代码</summary>

```java
	@Override
	public boolean isRequired(DependencyDescriptor descriptor) {
		return descriptor.isRequired();
	}

```

</details>
