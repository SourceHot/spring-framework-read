# Spring ParameterNameDiscoverer
- 类全路径: `org.springframework.core.ParameterNameDiscoverer`
- 类作用: 参数名称发现. 

## 方法列表

- 在 `ParameterNameDiscoverer` 中定义了两个方法, 方法信息可以看下面代码. 
    1. 获取参数名称, 针对 Method 对象
    2. 获取参数名称, 针对 Constructor 对象

```java

public interface ParameterNameDiscoverer {

	 // 获取参数名称, 针对 Method 对象
    String[] getParameterNames(Method method);

	 // 获取参数名称, 针对 Constructor 对象
    String[] getParameterNames(Constructor<?> ctor);
}

```


接口上可以说的内容不多. 接下来就直接找实现方法去了解细节. 

- 首先看一个标准的反射方式的获取
    [StandardReflectionParameterNameDiscoverer]()