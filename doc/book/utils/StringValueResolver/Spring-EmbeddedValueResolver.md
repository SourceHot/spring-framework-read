# Spring EmbeddedValueResolver

- 类全路径: `org.springframework.beans.factory.config.EmbeddedValueResolver`
- 完整代码
    - EmbeddedValueResolver 通过 el 表达式的解析来返回string类型的结果
```java
public class EmbeddedValueResolver implements StringValueResolver {

	/**
	 * bean el 表达式上下文
	 */
	private final BeanExpressionContext exprContext;

	@Nullable
	private final BeanExpressionResolver exprResolver;


	public EmbeddedValueResolver(ConfigurableBeanFactory beanFactory) {
		this.exprContext = new BeanExpressionContext(beanFactory, null);
		this.exprResolver = beanFactory.getBeanExpressionResolver();
	}


	@Override
	@Nullable
	public String resolveStringValue(String strVal) {
		String value = this.exprContext.getBeanFactory().resolveEmbeddedValue(strVal);
		if (this.exprResolver != null && value != null) {
			// 通过 el表达解析进行解析
			Object evaluated = this.exprResolver.evaluate(value, this.exprContext);
			value = (evaluated != null ? evaluated.toString() : null);
		}
		return value;
	}

}
```