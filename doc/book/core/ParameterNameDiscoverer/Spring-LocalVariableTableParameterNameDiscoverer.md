# Spring LocalVariableTableParameterNameDiscoverer
- 类全路径: `org.springframework.core.LocalVariableTableParameterNameDiscoverer`



## 成员变量
- 对于`LocalVariableTableParameterNameDiscoverer`中的两个成员变量笔者对其的了解不是很深. 这里仅贴出代码. 


<details>
<summary>详细代码如下</summary>

```java
public class LocalVariableTableParameterNameDiscoverer implements ParameterNameDiscoverer {

	// marker object for classes that do not have any debug info
	private static final Map<Executable, String[]> NO_DEBUG_INFO_MAP = Collections.emptyMap();

	// the cache uses a nested index (value is a map) to keep the top level cache relatively small in size
	private final Map<Class<?>, Map<Executable, String[]>> parameterNamesCache = new ConcurrentHashMap<>(32);
}
```


</details>


- 在成员变量中出现了一个 `Executable` 在 JDK 中 `Executable` 下有两个实现类. 
    1. Method
    2. Constructor
    
    说到这里大概可以了解到 这里的 Executable -> String[] 的含义了. 
    即 可执行器 -> 参数列表
    
    
## 方法分析

- 在 `LocalVariableTableParameterNameDiscoverer` 中 `doGetParameterNames` 就是主要的处理方法, 接下来对这个方法进行分析
### doGetParameterNames
- 方法签名: `org.springframework.core.LocalVariableTableParameterNameDiscoverer.doGetParameterNames`


```java
	@Nullable
	private String[] doGetParameterNames(Executable executable) {
		Class<?> declaringClass = executable.getDeclaringClass();
		Map<Executable, String[]> map = this.parameterNamesCache.computeIfAbsent(declaringClass, this::inspectClass);
		return (map != NO_DEBUG_INFO_MAP ? map.get(executable) : null);
	}
```



在 `doGetParameterNames` 方法中还依赖`inspectClass`方法. 


### inspectClass
- 方法签名: `org.springframework.core.LocalVariableTableParameterNameDiscoverer.inspectClass`

- 方法流程
    1. 读取类文件
    2. 交给 ClassReader 进行处理结果
    
    
    
<details>
<summary>读取类文件</summary>

```java
    InputStream is = clazz.getResourceAsStream(ClassUtils.getClassFileName(clazz));
```

</details>





<details>
<summary>交给 ClassReader 进行处理结果</summary>

```java
    // 类读取器
    ClassReader classReader = new ClassReader(is);
    Map<Executable, String[]> map = new ConcurrentHashMap<>(32);
    classReader.accept(new ParameterNameDiscoveringVisitor(clazz, map), 0);
    return map;
```


</details>



- 这里涉及到 Spring 中对 asm 的分析. 这里不具体展开. 后续会有专门的分析