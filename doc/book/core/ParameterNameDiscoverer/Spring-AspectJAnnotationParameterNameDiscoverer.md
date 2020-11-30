# Spring AspectJAnnotationParameterNameDiscoverer
- 类全路径: `org.springframework.aop.aspectj.annotation.AbstractAspectJAdvisorFactory.AspectJAnnotationParameterNameDiscoverer`


- `AspectJAnnotationParameterNameDiscoverer` 主要用作处理函数(`method`)的注解(Aop相关注解)的参数名称

## 方法分析

### getParameterNames
- 方法签名: `org.springframework.aop.aspectj.annotation.AbstractAspectJAdvisorFactory.AspectJAnnotationParameterNameDiscoverer.getParameterNames(java.lang.reflect.Method)`
- 方法逻辑
    1. 获取 method 的 aop 相关注解
    2. 从类 `AspectJAnnotation` 中获取参数名称

```java
		@Override
		@Nullable
		public String[] getParameterNames(Method method) {
			if (method.getParameterCount() == 0) {
				return new String[0];
			}
			// 搜索 切面注解
			AspectJAnnotation<?> annotation = findAspectJAnnotationOnMethod(method);
			if (annotation == null) {
				return null;
			}
			// 注解参数切割
			StringTokenizer nameTokens = new StringTokenizer(annotation.getArgumentNames(), ",");
			if (nameTokens.countTokens() > 0) {
				String[] names = new String[nameTokens.countTokens()];
				for (int i = 0; i < names.length; i++) {
					names[i] = nameTokens.nextToken();
				}
				return names;
			}
			else {
				return null;
			}
		}
```