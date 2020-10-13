# Spring AbstractConditionalEnumConverter

- 类全路径: `org.springframework.core.convert.support.AbstractConditionalEnumConverter`

- 完整代码

```java
abstract class AbstractConditionalEnumConverter implements ConditionalConverter {

	private final ConversionService conversionService;


	protected AbstractConditionalEnumConverter(ConversionService conversionService) {
		this.conversionService = conversionService;
	}


	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		// 获取接口列表类型. class
		for (Class<?> interfaceType : ClassUtils.getAllInterfacesForClassAsSet(sourceType.getType())) {
			// 判断是否可以转换
			if (this.conversionService.canConvert(TypeDescriptor.valueOf(interfaceType), targetType)) {
				return false;
			}
		}
		return true;
	}

}
```