# Spring ConstructorArgumentValues.ValueHolder
- 类全路径: `org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder`
- 对应标签: ` <constructor-arg />`

- 相关处理方式
    - `org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder`
    - `org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray`
    - 处理内容不在此展开


## 成员变量

```java

    
    /**
     * 值
     */
    @Nullable
    private Object value;
    
    /**
     * 类型
     */
    @Nullable
    private String type;
    
    /**
     * 名称
     */
    @Nullable
    private String name;
    
    /**
     * 源
     */
    @Nullable
    private Object source;
    
    /**
     * 是否需要转换
     */
    private boolean converted = false;
    
    /**
     * 转换的值
     */
    @Nullable
    private Object convertedValue;

```