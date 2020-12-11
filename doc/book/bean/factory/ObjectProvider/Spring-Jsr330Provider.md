# Spring Jsr330Provider
- 类全路径: `org.springframework.beans.factory.support.DefaultListableBeanFactory.Jsr330Factory.Jsr330Provider`
- 代码内容不是很多直接看代码


```java
private class Jsr330Provider extends DependencyObjectProvider implements Provider<Object> {

			public Jsr330Provider(DependencyDescriptor descriptor, @Nullable String beanName) {
				super(descriptor, beanName);
			}

			@Override
			@Nullable
			public Object get() throws BeansException {
				return getValue();
			}
		}
```

- 这段代码指向了 `getValue` 方法, `getValue` 在 `DependencyObjectProvider`, 有关 `DependencyObjectProvider` 分析请查看[这篇文章](Spring-DependencyObjectProvider.md)