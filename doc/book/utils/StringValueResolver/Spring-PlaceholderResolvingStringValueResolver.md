# Spring PlaceholderResolvingStringValueResolver


```java
	private class PlaceholderResolvingStringValueResolver implements StringValueResolver {

		/**
		 * 属性解析帮助工具
		 */
		private final PropertyPlaceholderHelper helper;

		/**
		 * 占位符解析
		 */
		private final PlaceholderResolver resolver;

		public PlaceholderResolvingStringValueResolver(Properties props) {
			this.helper = new PropertyPlaceholderHelper(
					placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
			this.resolver = new PropertyPlaceholderConfigurerResolver(props);
		}

		@Override
		@Nullable
		public String resolveStringValue(String strVal) throws BeansException {
			// 占位符解析
			String resolved = this.helper.replacePlaceholders(strVal, this.resolver);
			if (trimValues) {
				resolved = resolved.trim();
			}
			return (resolved.equals(nullValue) ? null : resolved);
		}
	}

```

- 其中使用的两个类 PropertyPlaceholderHelper PlaceholderResolver 请查阅文档
    - [PropertyPlaceholderHelper](/doc/book/env/PropertyResolver/Spring-PropertyPlaceholderHelper.md)
    - [PlaceholderResolver](/doc/book/env/PropertyResolver/PlaceholderResolver/Spring-PlaceholderResolver.md)