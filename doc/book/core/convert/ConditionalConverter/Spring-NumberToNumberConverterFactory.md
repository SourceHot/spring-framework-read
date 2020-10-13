# Spring NumberToNumberConverterFactory
- 类全路径: `org.springframework.core.convert.support.NumberToNumberConverterFactory`

- 代码如下

```java
final class NumberToNumberConverterFactory implements ConverterFactory<Number, Number>, ConditionalConverter {

	@Override
	public <T extends Number> Converter<Number, T> getConverter(Class<T> targetType) {
		return new NumberToNumber<>(targetType);
	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		// 是否相同判断
		return !sourceType.equals(targetType);
	}


	/**
	 * number 转换 number
	 * @param <T>
	 */
	private static final class NumberToNumber<T extends Number> implements Converter<Number, T> {

		private final Class<T> targetType;

		public NumberToNumber(Class<T> targetType) {
			this.targetType = targetType;
		}

		/**
		 * 转换方法
		 * @param source the source object to convert, which must be an instance of {@code S} (never {@code null})
		 * @return
		 */
		@Override
		public T convert(Number source) {
			return NumberUtils.convertNumberToTargetClass(source, this.targetType);
		}
	}

}
```
