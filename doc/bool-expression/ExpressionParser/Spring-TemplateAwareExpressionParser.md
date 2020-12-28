# Spring TemplateAwareExpressionParser
- 类全路径: `org.springframework.expression.common.TemplateAwareExpressionParser`



### parseExpression
- 方法签名: `org.springframework.expression.common.TemplateAwareExpressionParser.parseExpression(java.lang.String, org.springframework.expression.ParserContext)`


```java
	@Override
	public Expression parseExpression(String expressionString, @Nullable ParserContext context) throws ParseException {
		// 两种处理方式
		// 1. 解析上下文是模板的情况下
		if (context != null && context.isTemplate()) {
			// 使用 模板上下文进行解析
			return parseTemplate(expressionString, context);
		}
		// 2. 解析上下文不是模板的情况下
		else {
			// 自定义的解析规则
			return doParseExpression(expressionString, context);
		}
	}

```



### parseTemplate
- 方法签名: `org.springframework.expression.common.TemplateAwareExpressionParser.parseTemplate`
- 通过解析上下文模板进行解析


```java
	private Expression parseTemplate(String expressionString, ParserContext context) throws ParseException {
		// 表达式为空
		if (expressionString.isEmpty()) {
			// 创建空的 LiteralExpression
			return new LiteralExpression("");
		}

		// 表达式解析成接口
		Expression[] expressions = parseExpressions(expressionString, context);
		if (expressions.length == 1) {
			return expressions[0];
		}
		else {
			// 返回字符串的表达式
			return new CompositeStringExpression(expressionString, expressions);
		}
	}

```
- `parseTemplate`方法中的`parseExpressions`方法实关于 el 表达式 的一个解析过程, 本质上是对字符串的拆分和创建 Expression 对象 详细代码如下

### parseExpressions
- 方法签名: `org.springframework.expression.common.TemplateAwareExpressionParser.parseExpressions`

<details>
<summary>parseExpressions</summary>


```java
	private Expression[] parseExpressions(String expressionString, ParserContext context) throws ParseException {
		List<Expression> expressions = new ArrayList<>();
		// 获取前缀
		String prefix = context.getExpressionPrefix();
		// 获取后缀
		String suffix = context.getExpressionSuffix();
		int startIdx = 0;

		while (startIdx < expressionString.length()) {
			int prefixIndex = expressionString.indexOf(prefix, startIdx);
			if (prefixIndex >= startIdx) {
				// an inner expression was found - this is a composite
				if (prefixIndex > startIdx) {
					expressions.add(new LiteralExpression(expressionString.substring(startIdx, prefixIndex)));
				}
				int afterPrefixIndex = prefixIndex + prefix.length();
				int suffixIndex = skipToCorrectEndSuffix(suffix, expressionString, afterPrefixIndex);
				if (suffixIndex == -1) {
					throw new ParseException(expressionString, prefixIndex,
							"No ending suffix '" + suffix + "' for expression starting at character " +
							prefixIndex + ": " + expressionString.substring(prefixIndex));
				}
				if (suffixIndex == afterPrefixIndex) {
					throw new ParseException(expressionString, prefixIndex,
							"No expression defined within delimiter '" + prefix + suffix +
							"' at character " + prefixIndex);
				}
				String expr = expressionString.substring(prefixIndex + prefix.length(), suffixIndex);
				expr = expr.trim();
				if (expr.isEmpty()) {
					throw new ParseException(expressionString, prefixIndex,
							"No expression defined within delimiter '" + prefix + suffix +
							"' at character " + prefixIndex);
				}
				expressions.add(doParseExpression(expr, context));
				startIdx = suffixIndex + suffix.length();
			}
			else {
				// no more ${expressions} found in string, add rest as static text
				expressions.add(new LiteralExpression(expressionString.substring(startIdx)));
				startIdx = expressionString.length();
			}
		}

		return expressions.toArray(new Expression[0]);
	}

```
</details>





### doParseExpression
- 方法签名: `org.springframework.expression.common.TemplateAwareExpressionParser#doParseExpression`
- 抽象方法 


```java
	protected abstract Expression doParseExpression(String expressionString, @Nullable ParserContext context)
			throws ParseException;

```


关于 doParseExpression 分析请查看