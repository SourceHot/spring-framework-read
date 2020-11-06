# Spring ResourceEditorRegistrar

- 类全路径: `org.springframework.beans.support.ResourceEditorRegistrar`





- 该类实现`PropertyEditorRegistrar`  关注方法

  ```java
  	@Override
  	public void registerCustomEditors(PropertyEditorRegistry registry) {
  		ResourceEditor baseEditor = new ResourceEditor(this.resourceLoader, this.propertyResolver);
  		doRegisterEditor(registry, Resource.class, baseEditor);
  		doRegisterEditor(registry, ContextResource.class, baseEditor);
  		doRegisterEditor(registry, InputStream.class, new InputStreamEditor(baseEditor));
  		doRegisterEditor(registry, InputSource.class, new InputSourceEditor(baseEditor));
  		doRegisterEditor(registry, File.class, new FileEditor(baseEditor));
  		doRegisterEditor(registry, Path.class, new PathEditor(baseEditor));
  		doRegisterEditor(registry, Reader.class, new ReaderEditor(baseEditor));
  		doRegisterEditor(registry, URL.class, new URLEditor(baseEditor));
  
  		ClassLoader classLoader = this.resourceLoader.getClassLoader();
  		doRegisterEditor(registry, URI.class, new URIEditor(classLoader));
  		doRegisterEditor(registry, Class.class, new ClassEditor(classLoader));
  		doRegisterEditor(registry, Class[].class, new ClassArrayEditor(classLoader));
  
  		if (this.resourceLoader instanceof ResourcePatternResolver) {
  			doRegisterEditor(registry, Resource[].class,
  					new ResourceArrayPropertyEditor((ResourcePatternResolver) this.resourceLoader, this.propertyResolver));
  		}
  	}
  ```



- doRegisterEditor 方法

```java
private void doRegisterEditor(PropertyEditorRegistry registry, Class<?> requiredType, PropertyEditor editor) {
   if (registry instanceof PropertyEditorRegistrySupport) {
      // 属性编辑器覆盖默认的编辑器
      ((PropertyEditorRegistrySupport) registry).overrideDefaultEditor(requiredType, editor);
   }
   else {
      // 注册自定义的属性编辑器
      registry.registerCustomEditor(requiredType, editor);
   }
}
```



- 这里主要涉及到`org.springframework.beans.PropertyEditorRegistrySupport`的两个方法 在 [Spring-PropertyEditorRegistrySupport](./Spring-PropertyEditorRegistrySupport.md)













