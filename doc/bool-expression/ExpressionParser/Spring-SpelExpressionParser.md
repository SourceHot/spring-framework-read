# Spring SpelExpressionParser
- 类全路径: `org.springframework.expression.spel.standard.SpelExpressionParser`

- spring 中用来解析 el 表达式的核心对象, 其成员变量存储了关于 el 表达式解析的配置信息


## 成员变量

```java

public class SpelExpressionParser extends TemplateAwareExpressionParser {
    /**
     * el 表达式解析的配置
     */
    private final SpelParserConfiguration configuration;

}
```


## 方法分析

### parseRaw 
- 方法签名: `org.springframework.expression.spel.standard.SpelExpressionParser.parseRaw`    


```java
	public SpelExpression parseRaw(String expressionString) throws ParseException {
		return doParseExpression(expressionString, null);
	}

```

- 关于解析正真的处理代码是交给 [`InternalSpelExpressionParser`](Spring-InternalSpelExpressionParser.md) 执行的并不是自己完成. 

```java

	@Override
	protected SpelExpression doParseExpression(String expressionString, @Nullable ParserContext context) throws ParseException {
		// 交给 InternalSpelExpressionParser 进行解析
		return new InternalSpelExpressionParser(this.configuration).doParseExpression(expressionString, context);
	}
```