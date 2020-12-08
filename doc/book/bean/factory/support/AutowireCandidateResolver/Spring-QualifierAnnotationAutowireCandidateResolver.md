# Spring QualifierAnnotationAutowireCandidateResolver
- 类全路径: `org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver`





关于`QualifierAnnotationAutowireCandidateResolver` 我们首先对其中的两个成员变量进行分析




<details>
<summary>QualifierAnnotationAutowireCandidateResolver 成员变量</summary>

```java
public class QualifierAnnotationAutowireCandidateResolver extends GenericTypeAwareAutowireCandidateResolver {

	/**
	 * Qualifier 相关的注解类型
	 */
	private final Set<Class<? extends Annotation>> qualifierTypes = new LinkedHashSet<>(2);

	/**
	 * value 注解.class
	 */
	private Class<? extends Annotation> valueAnnotationType = Value.class;
}
```

</details>



- 在两个成员变量中有一个很好理解, 这个变量是`valueAnnotationType` 就是注解 Value 的class
    还有一个是存储`Qualifier`相关的set集合. 存储 Class
    从整个类的阅读上目前可以知道存储了
        1. org.springframework.beans.factory.annotation.Qualifier
        2. javax.inject.Qualifier
    
    


在了解成员变量后接下来进行方法分析

## 方法分析

### isAutowireCandidate
- 方法签名: `org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver.isAutowireCandidate`
- 方法作用: 是否是自动注入相关类型,是否需要自动注入

<details>
<summary>isAutowireCandidate 详细代码</summary>

```java
	@Override
	public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
		// 父类验证
		boolean match = super.isAutowireCandidate(bdHolder, descriptor);
		// 如果匹配
		if (match) {
			// 注解验证
			match = checkQualifiers(bdHolder, descriptor.getAnnotations());
			// 如果匹配
			if (match) {
				// 方法参数验证
				MethodParameter methodParam = descriptor.getMethodParameter();
				if (methodParam != null) {
					Method method = methodParam.getMethod();
					if (method == null || void.class == method.getReturnType()) {
						match = checkQualifiers(bdHolder, methodParam.getMethodAnnotations());
					}
				}
			}
		}
		return match;
	}

```
</details>


在这个方法上主要围绕注解进行展开. 注解的获取方式成为了一个重要的方法, 下面我们看一下在这段方法(`isAutowireCandidate`)中有那些获取方式
    1. `DependencyDescriptor` 中的属性. 依赖描述的注解属性
    2. 方法参数的注解


- 其实最终大家都依靠`checkQualifiers`方法. 我们将目光着重放在`checkQualifiers`身上


### checkQualifiers
- 方法签名: `org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver.checkQualifiers`
- 方法作用: 验证是否匹配



<details>
<summary>checkQualifiers 详细代码如下</summary>

```java
/**
 * Match the given qualifier annotations against the candidate bean definition.
 */
protected boolean checkQualifiers(BeanDefinitionHolder bdHolder, Annotation[] annotationsToSearch) {
   // 判空
   if (ObjectUtils.isEmpty(annotationsToSearch)) {
      return true;
   }
   // 类型转换器
   SimpleTypeConverter typeConverter = new SimpleTypeConverter();

   for (Annotation annotation : annotationsToSearch) {
      Class<? extends Annotation> type = annotation.annotationType();
      boolean checkMeta = true;
      boolean fallbackToMeta = false;
      // 注解类型是否在 qualifierTypes 中
      if (isQualifier(type)) {
         // 验证是否匹配
         if (!checkQualifier(bdHolder, annotation, typeConverter)) {
            fallbackToMeta = true;
         }
         else {
            checkMeta = false;
         }
      }
      //
      if (checkMeta) {
         boolean foundMeta = false;
         for (Annotation metaAnn : type.getAnnotations()) {
            Class<? extends Annotation> metaType = metaAnn.annotationType();
            if (isQualifier(metaType)) {
               foundMeta = true;
               // Only accept fallback match if @Qualifier annotation has a value...
               // Otherwise it is just a marker for a custom qualifier annotation.
               
               // StringUtils.isEmpty(AnnotationUtils.getValue(metaAnn)) 存在数据: Qualifier value 存在数据
               if ((fallbackToMeta && StringUtils.isEmpty(AnnotationUtils.getValue(metaAnn))) ||
                     !checkQualifier(bdHolder, metaAnn, typeConverter)) {
                  return false;
               }
            }
         }
         if (fallbackToMeta && !foundMeta) {
            return false;
         }
      }
   }
   return true;
}
```

</details>







两层循环. 

第一层注解是当前类上的. 

第二层是注解上的注解





在这一方法中更多的信息在`checkQualifier`方法中







### checkQualifier

- 方法签名: `org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver#checkQualifier`
- 方法作用: 验证是否和 Qualifier 注解的信息是否匹配



先阅读第一部分代码

<details>
<summary>checkQualifier 第一段详细代码如下</summary>



```java
// 第一部分
// 获取注解类型
Class<? extends Annotation> type = annotation.annotationType();
// 提取 bean 定义
RootBeanDefinition bd = (RootBeanDefinition) bdHolder.getBeanDefinition();

// 获取 AutowireCandidateQualifier . 通过类名的方式获取
// 从容器(qualifiers)中获取
AutowireCandidateQualifier qualifier = bd.getQualifier(type.getName());
if (qualifier == null) {
   // 没有获取到的话 通过类的短名字获取
   qualifier = bd.getQualifier(ClassUtils.getShortName(type));
}
```





在第一部分代码中的所有操作都是为了获取`AutowireCandidateQualifier`

在获取的时候采用两种方式

1. 根据长类名
2. 根据短类名

这里衍生出一个问题: **在已知长短类名的情况下从哪里获取？**

这里涉及到下面这个对象(`AbstractBeanDefinition`属性)



```java
private final Map<String, AutowireCandidateQualifier> qualifiers = new LinkedHashMap<>();
```





第一部分结束进入第二部分代码分析

第二部分代码相关内容是围绕 qualifier 在上述搜索模式下搜索不到的情况

第二部分代码分上下两段阅读. 



<details>
<summary>checkQualifier 第二段 上 详细代码如下</summary>

```java
// First, check annotation on qualified element, if any
// 提取注解 qualified (这里不一定是 qualified 注解, 也可能是 type 类型, type 类型即 入参注解)
Annotation targetAnnotation = getQualifiedElementAnnotation(bd, type);
// Then, check annotation on factory method, if applicable
if (targetAnnotation == null) {
   // 工厂方法的注解获取
   targetAnnotation = getFactoryMethodAnnotation(bd, type);
}
if (targetAnnotation == null) {
   // 依赖描述的解析
   RootBeanDefinition dbd = getResolvedDecoratedDefinition(bd);
   if (dbd != null) {
      // 工厂方法的注解获取
      targetAnnotation = getFactoryMethodAnnotation(dbd, type);
   }
}
```



</details>





上半部分代码围绕`targetAnnotation`的获取来编写. 

在这里有**2种**方式来进行获取

1. `getQualifiedElementAnnotation`
2. `getFactoryMethodAnnotation`



下面先来看 `getQualifiedElementAnnotation` 方法





### getQualifiedElementAnnotation

- 方法签名: `org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver#getQualifiedElementAnnotation`



```java
@Nullable
protected Annotation getQualifiedElementAnnotation(RootBeanDefinition bd, Class<? extends Annotation> type) {
   AnnotatedElement qualifiedElement = bd.getQualifiedElement();
   return (qualifiedElement != null ? AnnotationUtils.getAnnotation(qualifiedElement, type) : null);
}
```



### getFactoryMethodAnnotation

- 方法签名: `org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver#getFactoryMethodAnnotation`



```java
@Nullable
protected Annotation getFactoryMethodAnnotation(RootBeanDefinition bd, Class<? extends Annotation> type) {
   Method resolvedFactoryMethod = bd.getResolvedFactoryMethod();
   return (resolvedFactoryMethod != null ? AnnotationUtils.getAnnotation(resolvedFactoryMethod, type) : null);
}
```





在这里我们就两个方法一起看了.  两者的目的都是获取 type 对应的注解

`getQualifiedElementAnnotation` 从 BeanDefinition 的 `qualifiedElement` 属性中获取

`getFactoryMethodAnnotation` 从 方法的注解中获取





第二部分的上篇还有`getResolvedDecoratedDefinition`方法. 主要作用是将两个bean合并

两个bean分别是

1. 自身
2. bean的依赖对象







接下来对第二部分的下篇代码进行分析





<details>
<summary>checkQualifier 第二部分 下 详细代码如下</summary>

```java
if (targetAnnotation == null) {
   // Look for matching annotation on the target class
   if (getBeanFactory() != null) {
      try {
         // 查询依赖的bean类型
         Class<?> beanType = getBeanFactory().getType(bdHolder.getBeanName());
         if (beanType != null) {
            targetAnnotation = AnnotationUtils.getAnnotation(ClassUtils.getUserClass(beanType), type);
         }
      }
      catch (NoSuchBeanDefinitionException ex) {
         // Not the usual case - simply forget about the type check...
      }
   }
   if (targetAnnotation == null && bd.hasBeanClass()) {
      targetAnnotation = AnnotationUtils.getAnnotation(ClassUtils.getUserClass(bd.getBeanClass()), type);
   }
}
if (targetAnnotation != null && targetAnnotation.equals(annotation)) {
   return true;
}
```



</details>

第二部分下篇通过 `BeanFactory`  找到 `beanName` 对应的类型,通过类型去搜索注解(`type`)

目标还是`targetAnnotation` , 求其真正的值.

最后判断是否类型相同

```java
if (targetAnnotation != null && targetAnnotation.equals(annotation)) {
   return true;
}
```







第二部分上下两段阅读分析完成. 下面进行`checkQualifier` 第三部分代码分析

<details>
<summary>checkQualifier 第三部分详细代码如下</summary>

```java
// 第三部分
// 获取注解的属性列表
Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
if (attributes.isEmpty() && qualifier == null) {
   // If no attributes, the qualifier must be present
   return false;
}
for (Map.Entry<String, Object> entry : attributes.entrySet()) {
   String attributeName = entry.getKey();
   Object expectedValue = entry.getValue();
   Object actualValue = null;
   // Check qualifier first
   if (qualifier != null) {
      // 获取属性
      actualValue = qualifier.getAttribute(attributeName);
   }
   if (actualValue == null) {
      // Fall back on bean definition attribute
      // bean定义中获取属性
      actualValue = bd.getAttribute(attributeName);
   }
   if (actualValue == null && attributeName.equals(AutowireCandidateQualifier.VALUE_KEY) &&
         expectedValue instanceof String && bdHolder.matchesName((String) expectedValue)) {
      // Fall back on bean name (or alias) match
      continue;
   }
   if (actualValue == null && qualifier != null) {
      // Fall back on default, but only if the qualifier is present
      // 默认值获取
      actualValue = AnnotationUtils.getDefaultValue(annotation, attributeName);
   }
   if (actualValue != null) {
      actualValue = typeConverter.convertIfNecessary(actualValue, expectedValue.getClass());
   }
   if (!expectedValue.equals(actualValue)) {
      return false;
   }
}
return true;
```





</details>



第三部分只做一件事 `expectedValue` 和 `actualValue` 是否相同

在代码中有`actualValue` 的不同的确认方式(取值方式)，下面来说一说获取方式

1. `qualifier` 中提取属性

   ```
   qualifier.getAttribute(attributeName)
   ```

2. `beanDefinition` 中提取属性

   ```
   bd.getAttribute(attributeName)
   ```

3. 从注解中获取

   ```
   AnnotationUtils.getDefaultValue(annotation, attributeName)
   ```

4. 从类型转换器中转换后获取

   ```java
   typeConverter.convertIfNecessary(actualValue, expectedValue.getClass())
   ```