# BeanExpressionResolver
- 类全路径: `org.springframework.beans.factory.config.BeanExpressionResolver`



```java
public interface BeanExpressionResolver {

	/**
	 * Evaluate the given value as an expression, if applicable; return the value as-is otherwise.
	 * 与SPEL表达式相关
	 *
	 * @param value       the value to check
	 * @param evalContext the evaluation context
	 *
	 * @return the resolved value (potentially the given value as-is)
	 *
	 * @throws BeansException if evaluation failed
	 */
	@Nullable
	Object evaluate(@Nullable String value, BeanExpressionContext evalContext) throws BeansException;

}

```