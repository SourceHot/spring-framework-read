# Spring - PropertyEditorRegistrySupport
- 类全路径: `org.springframework.beans.PropertyEditorRegistrySupport`

## 成员变量分析

```java

	/**
	 * 转换服务
	 */
	@Nullable
	private ConversionService conversionService;

	/**
	 * 默认属性编辑器是否激活
	 */
	private boolean defaultEditorsActive = false;

	/**
	 * 配置编辑器是否激活
	 */
	private boolean configValueEditorsActive = false;

	/**
	 * 默认的属性编辑器
	 * key:class
	 * value: PropertyEditor
	 */
	@Nullable
	private Map<Class<?>, PropertyEditor> defaultEditors;

	/**
	 * 覆盖默认的属性编辑器容器
	 */
	@Nullable
	private Map<Class<?>, PropertyEditor> overriddenDefaultEditors;

	/**
	 * key:数据类型
	 * value: 属性编辑器
	 */
	@Nullable
	private Map<Class<?>, PropertyEditor> customEditors;

	/**
	 * 自定义编辑器
	 *
	 * key: 属性地址
	 * value: 自定义属性编辑器
	 */
	@Nullable
	private Map<String, CustomEditorHolder> customEditorsForPath;

	/**
	 * 属性编辑器列表
	 * key: class
	 * value: 属性编辑器
	 */
	@Nullable
	private Map<Class<?>, PropertyEditor> customEditorCache;
```


## 方法分析

### getDefaultEditor

- 获取默认的属性编辑器

逻辑

1. 默认属性编辑器是否激活
   1. 激活
      1. 从覆盖默认属性编辑器中获取属性编辑器
2. 默认属性编辑器为空的
   1. 创建默认属性编辑器
      1. 从默认属性编辑器中获取





```java
@Nullable
public PropertyEditor getDefaultEditor(Class<?> requiredType) {

   // 默认属性编辑器是否激活
   if (!this.defaultEditorsActive) {
      return null;
   }
   // 覆盖默认编辑器的容器是否存在
   if (this.overriddenDefaultEditors != null) {
      // 从覆盖容器中获取
      PropertyEditor editor = this.overriddenDefaultEditors.get(requiredType);
      if (editor != null) {
         return editor;
      }
   }
   // 默认编辑器是否存在
   if (this.defaultEditors == null) {
      // 创建默认编辑器
      createDefaultEditors();
   }
   // 从默认编辑器中获取
   return this.defaultEditors.get(requiredType);
}
```







### createDefaultEditors

- 创建默认编辑器





```java
private void createDefaultEditors() {
   this.defaultEditors = new HashMap<>(64);

   // Simple editors, without parameterization capabilities.
   // The JDK does not contain a default editor for any of these target types.
   // 设置 JDK 相关类型所对应的 编辑器
   this.defaultEditors.put(Charset.class, new CharsetEditor());
   this.defaultEditors.put(Class.class, new ClassEditor());
   this.defaultEditors.put(Class[].class, new ClassArrayEditor());
   this.defaultEditors.put(Currency.class, new CurrencyEditor());
   this.defaultEditors.put(File.class, new FileEditor());
   this.defaultEditors.put(InputStream.class, new InputStreamEditor());
   this.defaultEditors.put(InputSource.class, new InputSourceEditor());
   this.defaultEditors.put(Locale.class, new LocaleEditor());
   this.defaultEditors.put(Path.class, new PathEditor());
   this.defaultEditors.put(Pattern.class, new PatternEditor());
   this.defaultEditors.put(Properties.class, new PropertiesEditor());
   this.defaultEditors.put(Reader.class, new ReaderEditor());
   this.defaultEditors.put(Resource[].class, new ResourceArrayPropertyEditor());
   this.defaultEditors.put(TimeZone.class, new TimeZoneEditor());
   this.defaultEditors.put(URI.class, new URIEditor());
   this.defaultEditors.put(URL.class, new URLEditor());
   this.defaultEditors.put(UUID.class, new UUIDEditor());
   this.defaultEditors.put(ZoneId.class, new ZoneIdEditor());

   // Default instances of collection editors.
   // Can be overridden by registering custom instances of those as custom editors.
   this.defaultEditors.put(Collection.class, new CustomCollectionEditor(Collection.class));
   this.defaultEditors.put(Set.class, new CustomCollectionEditor(Set.class));
   this.defaultEditors.put(SortedSet.class, new CustomCollectionEditor(SortedSet.class));
   this.defaultEditors.put(List.class, new CustomCollectionEditor(List.class));
   this.defaultEditors.put(SortedMap.class, new CustomMapEditor(SortedMap.class));

   // Default editors for primitive arrays.
   this.defaultEditors.put(byte[].class, new ByteArrayPropertyEditor());
   this.defaultEditors.put(char[].class, new CharArrayPropertyEditor());

   // The JDK does not contain a default editor for char!
   this.defaultEditors.put(char.class, new CharacterEditor(false));
   this.defaultEditors.put(Character.class, new CharacterEditor(true));

   // Spring's CustomBooleanEditor accepts more flag values than the JDK's default editor.
   this.defaultEditors.put(boolean.class, new CustomBooleanEditor(false));
   this.defaultEditors.put(Boolean.class, new CustomBooleanEditor(true));

   // The JDK does not contain default editors for number wrapper types!
   // Override JDK primitive number editors with our own CustomNumberEditor.
   this.defaultEditors.put(byte.class, new CustomNumberEditor(Byte.class, false));
   this.defaultEditors.put(Byte.class, new CustomNumberEditor(Byte.class, true));
   this.defaultEditors.put(short.class, new CustomNumberEditor(Short.class, false));
   this.defaultEditors.put(Short.class, new CustomNumberEditor(Short.class, true));
   this.defaultEditors.put(int.class, new CustomNumberEditor(Integer.class, false));
   this.defaultEditors.put(Integer.class, new CustomNumberEditor(Integer.class, true));
   this.defaultEditors.put(long.class, new CustomNumberEditor(Long.class, false));
   this.defaultEditors.put(Long.class, new CustomNumberEditor(Long.class, true));
   this.defaultEditors.put(float.class, new CustomNumberEditor(Float.class, false));
   this.defaultEditors.put(Float.class, new CustomNumberEditor(Float.class, true));
   this.defaultEditors.put(double.class, new CustomNumberEditor(Double.class, false));
   this.defaultEditors.put(Double.class, new CustomNumberEditor(Double.class, true));
   this.defaultEditors.put(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
   this.defaultEditors.put(BigInteger.class, new CustomNumberEditor(BigInteger.class, true));

   // Only register config value editors if explicitly requested.
   if (this.configValueEditorsActive) {
      StringArrayPropertyEditor sae = new StringArrayPropertyEditor();
      this.defaultEditors.put(String[].class, sae);
      this.defaultEditors.put(short[].class, sae);
      this.defaultEditors.put(int[].class, sae);
      this.defaultEditors.put(long[].class, sae);
   }
}
```





这里做个抛砖引玉 ， 更多细节读者可以自行查看源码.





#### CustomNumberEditor

- 在 `createDefaultEditors` 操作的是 : `PropertyEditor` 实现类. 这里对其中一个类进行说明





主要关注下面三个方法

1. `setValue`
2. `getAsText`
3. `setAsText`

##### setValue

- 将参数 value 放入成员变量 value



```java
@Override
public void setValue(@Nullable Object value) {
   if (value instanceof Number) {
      super.setValue(NumberUtils.convertNumberToTargetClass((Number) value, this.numberClass));
   }
   else {
      super.setValue(value);
   }
}
```



- 说明

  判断是否为 number 类型

  通过 NumberUtils 将 value 转换成 具体的 Number 类型

  - Number 类型
    - Integer
    - Long
    - ...



##### getAsText

- 获取值转换成字符串



```java
@Override
public String getAsText() {
   Object value = getValue();
   if (value == null) {
      return "";
   }
   if (this.numberFormat != null) {
      // Use NumberFormat for rendering value.
      return this.numberFormat.format(value);
   }
   else {
      // Use toString method for rendering value.
      return value.toString();
   }
}
```







##### setAsText

- 将字符串设置到 value





```java
@Override
public void setAsText(String text) throws IllegalArgumentException {
   if (this.allowEmpty && !StringUtils.hasText(text)) {
      // Treat empty String as null value.
      setValue(null);
   }
   else if (this.numberFormat != null) {
      // Use given NumberFormat for parsing text.
      setValue(NumberUtils.parseNumber(text, this.numberClass, this.numberFormat));
   }
   else {
      // Use default valueOf methods for parsing text.
      setValue(NumberUtils.parseNumber(text, this.numberClass));
   }
}
```

说明

- 字符串判空
- 字符串转换成 number 对象, 放入









### registerCustomEditor



- 注册 PropertyEditor

```java
@Override
public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
   registerCustomEditor(requiredType, null, propertyEditor);
}


	@Override
	public void registerCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath, PropertyEditor propertyEditor) {
		// 判断类是否为空, 判断实行地址是否为空
		if (requiredType == null && propertyPath == null) {
			throw new IllegalArgumentException("Either requiredType or propertyPath is required");
		}
		// 属性地址不为空
		if (propertyPath != null) {
			// 创建自定义属性编辑器
			if (this.customEditorsForPath == null) {
				this.customEditorsForPath = new LinkedHashMap<>(16);
			}
			// 放入缓存
			this.customEditorsForPath.put(propertyPath, new CustomEditorHolder(propertyEditor, requiredType));
		}
		else {
			// 自定义属性编辑器是否为空
			if (this.customEditors == null) {
				this.customEditors = new LinkedHashMap<>(16);
			}
			// 放入 customEditors map对象中
			this.customEditors.put(requiredType, propertyEditor);
			this.customEditorCache = null;
		}
	}

```







### findCustomEditor

- 查询 属性编辑器

- 逻辑

1. 属性地址是否存在
   1. 存在
      1. 通过 class + 属性地址获取判断是否存在
         1. 存在，返回
         2. 不存在
            1. 切分属性地址
               1. 属性地址循环获取
   2. 不存在
      1. 直接获取



```java
@Override
@Nullable
public PropertyEditor findCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath) {
   // 正在搜索的class
   Class<?> requiredTypeToUse = requiredType;
   // 属性地址不为空
   if (propertyPath != null) {
      // 属性地址->编辑器 容器不为空
      if (this.customEditorsForPath != null) {
         // Check property-specific editor first.
         // 获取自定义属性编辑器
         PropertyEditor editor = getCustomEditor(propertyPath, requiredType);
         // 属性编辑器为空
         if (editor == null) {
            List<String> strippedPaths = new ArrayList<>();
            // 将 propertyPath 做切分
            addStrippedPropertyPaths(strippedPaths, "", propertyPath);
            // 对切分后的 propertyPath 进行属性编辑器获取
            for (Iterator<String> it = strippedPaths.iterator(); it.hasNext() && editor == null; ) {
               String strippedPath = it.next();
               editor = getCustomEditor(strippedPath, requiredType);
            }
         }
         if (editor != null) {
            return editor;
         }
      }
      // 正在搜索的class 为空的情况
      if (requiredType == null) {
         // 确认具体的属性类型
         requiredTypeToUse = getPropertyType(propertyPath);
      }
   }
   // No property-specific editor -> check type-specific editor.
   // 获取自定义属性编辑器
   return getCustomEditor(requiredTypeToUse);
}
```







### getCustomEditor

- 获取自定义属性编辑器



```java
@Nullable
private PropertyEditor getCustomEditor(String propertyName, @Nullable Class<?> requiredType) {
   CustomEditorHolder holder =
         (this.customEditorsForPath != null ? this.customEditorsForPath.get(propertyName) : null);
   return (holder != null ? holder.getPropertyEditor(requiredType) : null);
}
```



```java
@Nullable
private Map<String, CustomEditorHolder> customEditorsForPath;
```



从`customEditorsForPath` 获取 属性名称对应的对象`CustomEditorHolder`

从`CustomEditorHolder` 获取属性编辑器



- 这里新多一个对象 `CustomEditorHolder` 分析如下

#### CustomEditorHolder



##### 成员变量

```java
/**
 * 属性编辑器
 */
private final PropertyEditor propertyEditor;

/**
 * class
 */
@Nullable
private final Class<?> registeredType;
```

- 成员变量存储了属性编辑器和属性类型





##### 方法

- `getPropertyEditor` 获取属性编辑器

逻辑

- 对比参数的class是否和自己的class相同. 相同返回

```java
@Nullable
private PropertyEditor getPropertyEditor(@Nullable Class<?> requiredType) {
   // Special case: If no required type specified, which usually only happens for
   // Collection elements, or required type is not assignable to registered type,
   // which usually only happens for generic properties of type Object -
   // then return PropertyEditor if not registered for Collection or array type.
   // (If not registered for Collection or array, it is assumed to be intended
   // for elements.)
   if (this.registeredType == null ||
         (requiredType != null &&
               (ClassUtils.isAssignable(this.registeredType, requiredType) ||
                     ClassUtils.isAssignable(requiredType, this.registeredType))) ||
         (requiredType == null &&
               (!Collection.class.isAssignableFrom(this.registeredType) && !this.registeredType.isArray()))) {
      return this.propertyEditor;
   }
   else {
      return null;
   }
}
```









### getPropertyType

- 子类实现

- 实现: `org.springframework.beans.AbstractNestablePropertyAccessor#getPropertyType`

  这里不做具体展开

  ```java
  @Nullable
  protected Class<?> getPropertyType(String propertyPath) {
     return null;
  }
  ```







### getCustomEditor

- 获取自定义编辑器

逻辑

1. 判空

2. 从自定义属性编辑器中获取

   1. 不存在

      1. 自定义属性编辑器缓存中获取

         1. 不存在

            1. 循环 自定义属性编辑器的key 判断是否从 参数的class中继承

               1. 继承

                  从自定义属性编辑中获取

                  放入自定义编辑器缓存



```java
@Nullable
private PropertyEditor getCustomEditor(@Nullable Class<?> requiredType) {
   // 获取的类型是否存在, 自定义编辑器容器是否存在
   if (requiredType == null || this.customEditors == null) {
      return null;
   }
   // Check directly registered editor for type.
   // 从容器中获取
   PropertyEditor editor = this.customEditors.get(requiredType);
   if (editor == null) {
      // Check cached editor for type, registered for superclass or interface.
      if (this.customEditorCache != null) {
         // 从 自定义属性编辑器缓存中获取
         editor = this.customEditorCache.get(requiredType);
      }
      if (editor == null) {
         // Find editor for superclass or interface.
         // 自定义属性编辑器循环尝试互殴去
         for (Iterator<Class<?>> it = this.customEditors.keySet().iterator(); it.hasNext() && editor == null; ) {
            Class<?> key = it.next();
            // 判断是否继承
            if (key.isAssignableFrom(requiredType)) {
               // 从自定义属性编辑器容器中获取
               editor = this.customEditors.get(key);
               // Cache editor for search type, to avoid the overhead
               // of repeated assignable-from checks.
               if (this.customEditorCache == null) {
                  this.customEditorCache = new HashMap<>();
               }
               // 放入缓存
               this.customEditorCache.put(requiredType, editor);
            }
         }
      }
   }
   return editor;
}
```








### addStrippedPropertyPaths



- 做属性值切分

```java
private void addStrippedPropertyPaths(List<String> strippedPaths, String nestedPath, String propertyPath) {
   // 从 属性地址中 获取 [ 所在位置
   int startIndex = propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);
   // 判断是否存在 [
   if (startIndex != -1) {
      // 从 属性地址中 获取 ] 所在位置
      int endIndex = propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR);
      // 判断是否存在 ]
      if (endIndex != -1) {
         // 切割字符串
         String prefix = propertyPath.substring(0, startIndex);
         String key = propertyPath.substring(startIndex, endIndex + 1);
         String suffix = propertyPath.substring(endIndex + 1);
         // Strip the first key.
         strippedPaths.add(nestedPath + prefix + suffix);
         // Search for further keys to strip, with the first key stripped.
         // 递归处理
         addStrippedPropertyPaths(strippedPaths, nestedPath + prefix, suffix);
         // Search for further keys to strip, with the first key not stripped.
         addStrippedPropertyPaths(strippedPaths, nestedPath + prefix + key, suffix);
      }
   }
}
```







### overrideDefaultEditor

- 方法作用: 设置覆盖的 PropertyEditor 
- 代码是对map的一个操作

```java
public void overrideDefaultEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
   if (this.overriddenDefaultEditors == null) {
      this.overriddenDefaultEditors = new HashMap<>();
   }
   this.overriddenDefaultEditors.put(requiredType, propertyEditor);
}
```