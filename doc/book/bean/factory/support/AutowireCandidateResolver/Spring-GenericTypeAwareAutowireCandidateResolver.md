# Spring GenericTypeAwareAutowireCandidateResolver
- 类全路径: `org.springframework.beans.factory.support.GenericTypeAwareAutowireCandidateResolver`


## 方法分析
### checkGenericTypeMatch
- 方法签名: `org.springframework.beans.factory.support.GenericTypeAwareAutowireCandidateResolver.checkGenericTypeMatch`
- 方法作用: 验证类型是否匹配


首先了解参数

1. BeanDefinitionHolder: Bean定义持有对象
2. DependencyDescriptor: 依赖描述对象


方法主要目的验证: Bean 定义持有对象的 bean 类型是否和依赖描述对象的类型相同


下面开始阅读第一部分代码
 

第一部分代码交代了一些基础变量的获取

<details>
<summary>第一部分代码详情</summary>


```java
    // 第一部分
    // 类型描述的解析对象
    // 依赖类型
    ResolvableType dependencyType = descriptor.getResolvableType();
    // 依赖类型 是否是 class
    if (dependencyType.getType() instanceof Class) {
        // No generic type -> we know it's a Class type-match, so no need to check again.
        return true;
    }

    ResolvableType targetType = null;
    boolean cacheType = false;
    RootBeanDefinition rbd = null;
    // 从 bean定义持有对象中获取 bean定义
    if (bdHolder.getBeanDefinition() instanceof RootBeanDefinition) {
        rbd = (RootBeanDefinition) bdHolder.getBeanDefinition();
    }
```

</details>


继续阅读第二部分代码
第二部分代码围绕 targetType 这个变量展开, 描述了其获取过程

1. 从 bean 定义中直接获取 即 RootBeanDefinition#targetType 属性
2. 从 工厂方法的返回值转换获取


<details>
<summary>第二部分代码详情</summary>


```java
		// 第二部分
		if (rbd != null) {
			targetType = rbd.targetType;
			if (targetType == null) {
				cacheType = true;
				// First, check factory method return type, if applicable
				// 工厂方法的返回值
				targetType = getReturnTypeForFactoryMethod(rbd, descriptor);
				if (targetType == null) {
					// 解析 RootBeanDefinition
					RootBeanDefinition dbd = getResolvedDecoratedDefinition(rbd);
					if (dbd != null) {
						targetType = dbd.targetType;
						if (targetType == null) {
							// 工厂方法的返回值
							targetType = getReturnTypeForFactoryMethod(dbd, descriptor);
						}
					}
				}
			}
		}

```

</details>



第二部分中出现了两个方法
    1. getReturnTypeForFactoryMethod
    2. getResolvedDecoratedDefinition
    这两个方法我们也需要对其进行了解.

### getReturnTypeForFactoryMethod
- 方法签名: `org.springframework.beans.factory.support.GenericTypeAwareAutowireCandidateResolver.getReturnTypeForFactoryMethod`
- 方法作用: 将工厂方法的返回值类型 转换成 ResolvableType 对象

在了解`RootBeanDefinition`对象的前提下, 我们知道其中存有`factoryMethodReturnType`和`factoryMethodToIntrospect` 这两个属性都是有可能成为这个方法的操作对象.
    1. `factoryMethodReturnType` 可以直接作为返回值
    2. `factoryMethodToIntrospect` 类型是 `Method` 需要获取 return class 在转换成返回对象

    围绕上述两点来看下面的代码比较容易理解



<details>
<summary>getReturnTypeForFactoryMethod 代码详情</summary>

```java
	@Nullable
	protected ResolvableType getReturnTypeForFactoryMethod(RootBeanDefinition rbd, DependencyDescriptor descriptor) {
		// Should typically be set for any kind of factory method, since the BeanFactory
		// pre-resolves them before reaching out to the AutowireCandidateResolver...
		ResolvableType returnType = rbd.factoryMethodReturnType;
		if (returnType == null) {
			Method factoryMethod = rbd.getResolvedFactoryMethod();
			if (factoryMethod != null) {
				returnType = ResolvableType.forMethodReturnType(factoryMethod);
			}
		}
		if (returnType != null) {
			Class<?> resolvedClass = returnType.resolve();
			if (resolvedClass != null && descriptor.getDependencyType().isAssignableFrom(resolvedClass)) {
				// Only use factory method metadata if the return type is actually expressive enough
				// for our dependency. Otherwise, the returned instance type may have matched instead
				// in case of a singleton instance having been registered with the container already.
				return returnType;
			}
		}
		return null;
	}

```
</details>





### getResolvedDecoratedDefinition
- 方法签名: `org.springframework.beans.factory.support.GenericTypeAwareAutowireCandidateResolver.getResolvedDecoratedDefinition`
- 方法作用: 解析依赖bean的定义

通过当前bean定义来获取与之对应的依赖对象, 然后返回


<details>
<summary>getResolvedDecoratedDefinition 代码详情</summary>

```java
	@Nullable
	protected RootBeanDefinition getResolvedDecoratedDefinition(RootBeanDefinition rbd) {
		// 获取 bean 定义持有对象(依赖的对象)
		BeanDefinitionHolder decDef = rbd.getDecoratedDefinition();
		if (decDef != null && this.beanFactory instanceof ConfigurableListableBeanFactory) {
			ConfigurableListableBeanFactory clbf = (ConfigurableListableBeanFactory) this.beanFactory;
			if (clbf.containsBeanDefinition(decDef.getBeanName())) {
				BeanDefinition dbd = clbf.getMergedBeanDefinition(decDef.getBeanName());
				if (dbd instanceof RootBeanDefinition) {
					return (RootBeanDefinition) dbd;
				}
			}
		}
		return null;
	}

```

</details>



回到主线继续阅读第三部分代码

<details>
<summary>checkGenericTypeMatch 第三部分 代码详情</summary>


```java
    // 第三部分
    if (targetType == null) {
        // Regular case: straight bean instance, with BeanFactory available.
        // 普通情况: bean 实例存在 且有 BeanFactory
        if (this.beanFactory != null) {
            // beanFactory 获取 beanName 对应的 类型
            Class<?> beanType = this.beanFactory.getType(bdHolder.getBeanName());
            if (beanType != null) {
                targetType = ResolvableType.forClass(ClassUtils.getUserClass(beanType));
            }
        }
        // Fallback: no BeanFactory set, or no type resolvable through it
        // -> best-effort match against the target class if applicable.
        if (targetType == null && rbd != null && rbd.hasBeanClass() && rbd.getFactoryMethodName() == null) {
            Class<?> beanClass = rbd.getBeanClass();
            // beanClass 是否来自 FactoryBean
            if (!FactoryBean.class.isAssignableFrom(beanClass)) {
                targetType = ResolvableType.forClass(ClassUtils.getUserClass(beanClass));
            }
        }
    }

```

</details>


第三部分的处理是针对 beanName 和 beanFactory.
    1. 从 BeanFactory 中获取 BeanName 对应的类型, 转换成 ResolvableType 返回
    2. 判断是否 FactoryBean 实现. 返回 ResolvableType 




继续阅读第四部分
第四部分是最终结果返回


<details>
<summary>checkGenericTypeMatch 第四部分 代码详情</summary>
```java
		// 第四部分
		if (targetType == null) {
			return true;
		}
		if (cacheType) {
			rbd.targetType = targetType;
		}
		if (descriptor.fallbackMatchAllowed() &&
				(targetType.hasUnresolvableGenerics() || targetType.resolve() == Properties.class)) {
			// Fallback matches allow unresolvable generics, e.g. plain HashMap to Map<String,String>;
			// and pragmatically also java.util.Properties to any Map (since despite formally being a
			// Map<Object,Object>, java.util.Properties is usually perceived as a Map<String,String>).
			return true;
		}
		// Full check for complex generic type match...
		// 是否来自依赖类型
		return dependencyType.isAssignableFrom(targetType);
```
</details>