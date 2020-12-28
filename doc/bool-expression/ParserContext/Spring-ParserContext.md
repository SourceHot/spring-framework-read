# Spring ParserContext
- 类全路径: `org.springframework.expression.ParserContext`
- 解析上下文
- 接口主要呈get方法, 实现类中也是如此, 关于 ParserContext 的实现类不做具体的分析
  

- 在 `ParserContext` 中定义了下面3个方法
    1. 是否是模板 
    2. 获取表达式前缀
    3. 获取表达式后缀
    

- 在 `ParserContext` 中也同时提供了一个默认的解析上下文实现. 它的前缀: `#{` 后缀: `}`


<details>
<summary>ParserContext 详细代码</summary>

```java
public interface ParserContext {

	/**
	 *
	 * 是否是模板
	 */
	boolean isTemplate();

	/**
	 *
	 * 获取表达式前缀
	 */
	String getExpressionPrefix();

	/**
	 * 获取表达式后缀
	 */
	String getExpressionSuffix();


	/**
	 * 解析上下文模板类
	 */
	ParserContext TEMPLATE_EXPRESSION = new ParserContext() {

		@Override
		public boolean isTemplate() {
			return true;
		}

		@Override
		public String getExpressionPrefix() {
			return "#{";
		}

		@Override
		public String getExpressionSuffix() {
			return "}";
		}
	};

}
```

</details>
