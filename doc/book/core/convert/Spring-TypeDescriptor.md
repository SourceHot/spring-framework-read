# Spring TypeDescriptor
- 类全路径: `org.springframework.core.convert.TypeDescriptor`
- 类型描述接口


## 成员变量

```java
public class TypeDescriptor implements Serializable {

	/**
	 * 空注解列表
	 */
	private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

	/**
	 * 公共类型和 TypeDescriptor 的关系缓存
	 */
	private static final Map<Class<?>, TypeDescriptor> commonTypesCache = new HashMap<>(32);

	/**
	 * 公共类型
	 */
	private static final Class<?>[] CACHED_COMMON_TYPES = {
			boolean.class, Boolean.class, byte.class, Byte.class, char.class, Character.class,
			double.class, Double.class, float.class, Float.class, int.class, Integer.class,
			long.class, Long.class, short.class, Short.class, String.class, Object.class};

	private final Class<?> type;

	/**
	 * 解析类型
	 */
	private final ResolvableType resolvableType;

	/**
	 * 注解集合
	 */
	private final AnnotatedElementAdapter annotatedElement;
}

```


## 静态方法
- 该静态方法会设置 通用类型的 类型描述对象 TypeDescriptor

```java
	static {
		for (Class<?> preCachedClass : CACHED_COMMON_TYPES) {
			commonTypesCache.put(preCachedClass, valueOf(preCachedClass));
		}
	}

```

- 接下来阅读 valueOf 方法


## valueOf
- 方法作用: 获取一个类型的TypeDescriptor对象
```java
	public static TypeDescriptor valueOf(@Nullable Class<?> type) {
		// 类型判断是否为空
		if (type == null) {
			type = Object.class;
		}
		// 缓存中获取 TypeDescriptor
		TypeDescriptor desc = commonTypesCache.get(type);
		
		// 判空后进行手动创建
		return (desc != null ? desc : new TypeDescriptor(ResolvableType.forClass(type), null, null));
	}

```

- 在这个类中还有其他的将 class 转换成 TypeDescriptor 的方法在这里不具体一个个分析了. 