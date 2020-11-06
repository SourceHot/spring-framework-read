# Spring  PropertyEditorRegistrar

- 类全路径: `org.springframework.beans.PropertyEditorRegistrar`
- `PropertyEditorRegistrar` 接口 通过`PropertyEditorRegistry` 将`PropertyEditor`注册到容器





```java
public interface PropertyEditorRegistrar {

   /**
    * Register custom {@link java.beans.PropertyEditor PropertyEditors} with the given {@code
    * PropertyEditorRegistry}.
    * <p>The passed-in registry will usually be a {@link BeanWrapper} or a
    * {@link org.springframework.validation.DataBinder DataBinder}.
    * <p>It is expected that implementations will create brand new
    * {@code PropertyEditors} instances for each invocation of this method (since {@code
    * PropertyEditors} are not threadsafe).
    * 属性编辑器注册方法
    *
    * @param registry the {@code PropertyEditorRegistry} to register the custom {@code
    *                 PropertyEditors} with
    */
   void registerCustomEditors(PropertyEditorRegistry registry);

}
```





详细实现方法请查看子类： [ResourceEditorRegistrar](./Spring-ResourceEditorRegistrar.md)