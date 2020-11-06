# Spring ServletContextScope
- 类全路径: `org.springframework.web.context.support.ServletContextScope`





## 成员变量

```java
/**
 * servlet 上下文
 */
private final ServletContext servletContext;

/**
 * 摧毁方法容器
 * key: name
 * value: 摧毁方法
 */
private final Map<String, Runnable> destructionCallbacks = new LinkedHashMap<>();
```



- ServletContext 承担 Object Instance 存储的作用



## 方法分析



### get

- 逻辑
  1. 从 servlet-context 中获取
     1. 不存在
        1. 从 objectFactory 中获取
           1. 放入 servlet-context

```java
@Override
public Object get(String name, ObjectFactory<?> objectFactory) {
   // 从 servlet-context 根据name 获取实例
   Object scopedObject = this.servletContext.getAttribute(name);
   // 实例为空
   if (scopedObject == null) {
      // objectFactory 获取
      scopedObject = objectFactory.getObject();
      // 放入缓存
      this.servletContext.setAttribute(name, scopedObject);
   }
   return scopedObject;
}
```







### remove

- 逻辑
  1. 从 servlet-context 获取对象实例
     1. 对象实例存在
        1. 删除对象名称对应的回调方法
        2. 删除对象实例





```java
@Override
@Nullable
public Object remove(String name) {
   // 从 servlet-context 根据name 获取实例
   Object scopedObject = this.servletContext.getAttribute(name);
   if (scopedObject != null) {
      synchronized (this.destructionCallbacks) {
         // 删除 name 对应的回调方法
         this.destructionCallbacks.remove(name);
      }
      // 删除 servlet-context 中 name 对应的值
      this.servletContext.removeAttribute(name);
      // 返回删除对象
      return scopedObject;
   }
   else {
      return null;
   }
}
```







### registerDestructionCallback

- 注册摧毁方法



```java
@Override
public void registerDestructionCallback(String name, Runnable callback) {
   synchronized (this.destructionCallbacks) {
      this.destructionCallbacks.put(name, callback);
   }
}
```





### destroy

- 摧毁方法

- 执行所有的摧毁回调

```java
@Override
public void destroy() {
   synchronized (this.destructionCallbacks) {
      for (Runnable runnable : this.destructionCallbacks.values()) {
         runnable.run();
      }
      this.destructionCallbacks.clear();
   }
}
```