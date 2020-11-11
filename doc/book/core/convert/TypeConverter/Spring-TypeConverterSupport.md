# Spring TypeConverterSupport
- 类全路径: `org.springframework.beans.TypeConverterSupport`
- `TypeConverterSupport` 是一个抽象类, 实现了 `TypeConverter` , 
    在 `TypeConverterSupport`内部真正依靠的类是`TypeConverterDelegate`.
    关于 `TypeConverterDelegate` 分析可以查看: [这篇文章](Spring-TypeConverterDelegate.md)
    
    
- 这里直接贴出完整代码, 不做具体的分析.
    通过阅读可以发现最终都指向方法: `org.springframework.beans.TypeConverterDelegate.convertIfNecessary(java.lang.String, java.lang.Object, java.lang.Object, java.lang.Class<T>, org.springframework.core.convert.TypeDescriptor)`
    
```java
public abstract class TypeConverterSupport extends PropertyEditorRegistrySupport implements TypeConverter {

	@Nullable
	TypeConverterDelegate typeConverterDelegate;


	@Override
	@Nullable
	public <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType) throws TypeMismatchException {
		return convertIfNecessary(value, requiredType, TypeDescriptor.valueOf(requiredType));
	}

	@Override
	@Nullable
	public <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType,
			@Nullable MethodParameter methodParam) throws TypeMismatchException {

		return convertIfNecessary(value, requiredType,
				(methodParam != null ? new TypeDescriptor(methodParam) : TypeDescriptor.valueOf(requiredType)));
	}

	@Override
	@Nullable
	public <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType, @Nullable Field field)
			throws TypeMismatchException {

		return convertIfNecessary(value, requiredType,
				(field != null ? new TypeDescriptor(field) : TypeDescriptor.valueOf(requiredType)));
	}

	@Nullable
	@Override
	public <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType,
			@Nullable TypeDescriptor typeDescriptor) throws TypeMismatchException {

		Assert.state(this.typeConverterDelegate != null, "No TypeConverterDelegate");
		try {
			return this.typeConverterDelegate.convertIfNecessary(null, null, value, requiredType, typeDescriptor);
		}
		catch (ConverterNotFoundException | IllegalStateException ex) {
			throw new ConversionNotSupportedException(value, requiredType, ex);
		}
		catch (ConversionException | IllegalArgumentException ex) {
			throw new TypeMismatchException(value, requiredType, ex);
		}
	}

}
```