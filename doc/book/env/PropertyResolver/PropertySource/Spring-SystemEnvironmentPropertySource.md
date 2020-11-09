# Spring SystemEnvironmentPropertySource



- `SystemEnvironmentPropertySource` 继承`MapPropertySource` 其 source 还是一个 map 结构. 在了解这点后我们进行方法分析



## 方法列表



### getProperty

- 获取属性

```java
@Override
@Nullable
public Object getProperty(String name) {
   // 解析属性名称
   String actualName = resolvePropertyName(name);
   if (logger.isDebugEnabled() && !name.equals(actualName)) {
      logger.debug("PropertySource '" + getName() + "' does not contain property '" + name +
            "', but found equivalent '" + actualName + "'");
   }
   // 从 map 中获取
   return super.getProperty(actualName);
}
```



- 这段代码主要解决 name 转换到真实属性名称的过程, 其方法为`resolvePropertyName`





### resolvePropertyName

- 解析属性名称

  ```java
  protected final String resolvePropertyName(String name) {
     Assert.notNull(name, "Property name must not be null");
     // 验证属性名称
     String resolvedName = checkPropertyName(name);
     // 不为空返回
     if (resolvedName != null) {
        return resolvedName;
     }
     // 大写
     String uppercasedName = name.toUpperCase();
     // name 是否等价于大写
     if (!name.equals(uppercasedName)) {
        // 验证属性名称
        resolvedName = checkPropertyName(uppercasedName);
        // 返回
        if (resolvedName != null) {
           return resolvedName;
        }
     }
     return name;
  }
  ```



- 这段代码中出现了 属性验证的函数`checkPropertyName` 对此进行查阅





### checkPropertyName

```java
@Nullable
private String checkPropertyName(String name) {
   // Check name as-is
   // 是否存在
   if (containsKey(name)) {
      return name;
   }
   // Check name with just dots replaced
   // 将名称中 . 替换成 _ 是否存在
   String noDotName = name.replace('.', '_');
   if (!name.equals(noDotName) && containsKey(noDotName)) {
      return noDotName;
   }
   // Check name with just hyphens replaced
   // 将名称中 - 替换成 _ 是否存在
   String noHyphenName = name.replace('-', '_');
   if (!name.equals(noHyphenName) && containsKey(noHyphenName)) {
      return noHyphenName;
   }
   // Check name with dots and hyphens replaced
   // 替换 . 和 - 都转换为 _ 判断是否存在
   String noDotNoHyphenName = noDotName.replace('-', '_');
   if (!noDotName.equals(noDotNoHyphenName) && containsKey(noDotNoHyphenName)) {
      return noDotNoHyphenName;
   }
   // Give up
   return null;
}
```





### 判断是否存在

```java
private boolean containsKey(String name) {
   return (isSecurityManagerPresent() ? this.source.containsKey(name) : this.source.containsKey(name));
}

protected boolean isSecurityManagerPresent() {
   return (System.getSecurityManager() != null);
}
```