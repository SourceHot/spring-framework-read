# Spring SimpleMapScope

- 类全路径: `org.springframework.context.testfixture.SimpleMapScope`



## 内部变量

```java
/**
 * 存储对象容器
 * key: name
 * value: object instance
 */
private final Map<String, Object> map = new HashMap<>();

/**
 * 存储回调方法
 */
private final List<Runnable> callbacks = new LinkedList<>();
```



在`SimpleMapScope`主要围绕上述两个内部变量进行操作. 



## 方法分析

### get

- 获取实例

逻辑

1. 从容器中获取
   1. 不存在
      1. 从 ObjectFactory 中获取
         1. 放入容器

```java
	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		synchronized (this.map) {
			// 从 容器中获取 .
			Object scopedObject = this.map.get(name);
			// 不存在从 ObjectFactory 中获取
			if (scopedObject == null) {
				scopedObject = objectFactory.getObject();
				// 添加到容器
				this.map.put(name, scopedObject);
			}
			return scopedObject;
		}
	}

```







### remove

- 删除 name 对应的Object 实例



```java
@Override
public Object remove(String name) {
   synchronized (this.map) {
      return this.map.remove(name);
   }
}
```







### registerDestructionCallback

- 添加摧毁方法的回调



```java
@Override
public void registerDestructionCallback(String name, Runnable callback) {
   this.callbacks.add(callback);
}
```







### close

- 在 close 的时候调用所有注册的回调方法

```java
public void close() {
   for (Iterator<Runnable> it = this.callbacks.iterator(); it.hasNext();) {
      Runnable runnable = it.next();
      runnable.run();
   }
}
```