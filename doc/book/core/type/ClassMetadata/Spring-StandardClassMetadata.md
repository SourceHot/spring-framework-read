# Spring StandardClassMetadata
- 类全路径: `org.springframework.core.type.StandardClassMetadata`
- `StandardClassMetadata` 是使用JDK中`Class`对象来实现 `ClassMeatadata` 接口, 本文主要讲述其方法调用.


## 方法分析

### getClassName
- 获取 class 名称
    通过调用 class.getName 来获取
    
```java
    @Override
    public String getClassName() {
        return this.introspectedClass.getName();
    }

```

### isInterface
- 判断是否是接口
    通过 class.isInterface 来判断
    
```java
    @Override
    public boolean isInterface() {
        return this.introspectedClass.isInterface();
    }

```

### isAnnotation
- 判断是否是注解
```java
    @Override
    public boolean isAnnotation() {
        return this.introspectedClass.isAnnotation();
    }


```


### isAbstract
- 判断是否 abstract 修饰

```java
    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(this.introspectedClass.getModifiers());
    }


```


### isFinal
- 判断是否 final 修饰

```java
    @Override
    public boolean isFinal() {
        return Modifier.isFinal(this.introspectedClass.getModifiers());
    }


```


### isIndependent

```java
	@Override
	public boolean isIndependent() {
    	// 是否存在内部类
		// 是否有 class
		// 是否有 static 修饰
		return (!hasEnclosingClass() ||
				(this.introspectedClass.getDeclaringClass() != null &&
						Modifier.isStatic(this.introspectedClass.getModifiers())));
	}

```


### getEnclosingClassName
- 获取内部类名称

```java
    @Override
    @Nullable
    public String getEnclosingClassName() {
    	// 内部类名称
        Class<?> enclosingClass = this.introspectedClass.getEnclosingClass();
        return (enclosingClass != null ? enclosingClass.getName() : null);
    }
```


### getSuperClassName
- 获取父类名称

```java
    @Override
    @Nullable
    public String getSuperClassName() {
        Class<?> superClass = this.introspectedClass.getSuperclass();
        return (superClass != null ? superClass.getName() : null);
    }
```


### getInterfaceNames
- 获取实现的接口名称

```java
    @Override
    public String[] getInterfaceNames() {
        Class<?>[] ifcs = this.introspectedClass.getInterfaces();
        String[] ifcNames = new String[ifcs.length];
        for (int i = 0; i < ifcs.length; i++) {
            ifcNames[i] = ifcs[i].getName();
        }
        return ifcNames;
    }
```


### getMemberClassNames
- 获取成员的类型名称

```java
    @Override
    public String[] getMemberClassNames() {
        LinkedHashSet<String> memberClassNames = new LinkedHashSet<>(4);
        for (Class<?> nestedClass : this.introspectedClass.getDeclaredClasses()) {
            memberClassNames.add(nestedClass.getName());
        }
        return StringUtils.toStringArray(memberClassNames);
    }

```