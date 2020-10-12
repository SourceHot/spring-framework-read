# Spring StaticStringValueResolver
- 类全路径: `org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder.StaticStringValueResolver`


- 完整代码

```java

	private static class StaticStringValueResolver implements StringValueResolver {

		private final PropertyPlaceholderHelper helper;

		private final PlaceholderResolver resolver;

		public StaticStringValueResolver(Map<String, String> values) {
			this.helper = new PropertyPlaceholderHelper("${", "}", ":", false);
			this.resolver = values::get;
		}

		@Override
		public String resolveStringValue(String strVal) throws BeansException {
			return this.helper.replacePlaceholders(strVal, this.resolver);
		}
	}

```

- 其中使用的两个类 PropertyPlaceholderHelper PlaceholderResolver 请查阅文档
    - [PropertyPlaceholderHelper](/doc/book/env/PropertyResolver/Spring-PropertyPlaceholderHelper.md)
    - [PlaceholderResolver](/doc/book/env/PropertyResolver/PlaceholderResolver)