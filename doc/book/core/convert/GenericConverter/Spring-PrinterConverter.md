# Spring PrinterConverter
- 类全路径: `org.springframework.format.support.FormattingConversionService.PrinterConverter`
- 类属性

```java
/**
 * 类型 
 */
private final Class<?> fieldType;

/**
 * 类型描述
 */
private final TypeDescriptor printerObjectType;

/**
 * 打印接口
 */
@SuppressWarnings("rawtypes")
private final Printer printer;

/**
 * 转换服务
 */
private final ConversionService conversionService;
```


## convert



```java
		@Override
		@SuppressWarnings("unchecked")
		public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			if (!sourceType.isAssignableTo(this.printerObjectType)) {
				// 值转换
				source = this.conversionService.convert(source, sourceType, this.printerObjectType);
			}
			if (source == null) {
				return "";
			}
			// 获取 print 值
			return this.printer.print(source, LocaleContextHolder.getLocale());
		}

```