# Spring InternalSpelExpressionParser
- 类全路径: `org.springframework.expression.spel.standard.InternalSpelExpressionParser`

## 成员变量

```java

	private static final Pattern VALID_QUALIFIED_ID_PATTERN = Pattern.compile("[\\p{L}\\p{N}_$]+");


	/**
	 * el解析配置
	 */
	private final SpelParserConfiguration configuration;


	// For rules that build nodes, they are stacked here for return

	/**
	 * el 表达式节点
	 */
	private final Deque<SpelNodeImpl> constructedNodes = new ArrayDeque<>();

	// The expression being parsed

	/**
	 * 需要解析的字符串
	 */
	private String expressionString = "";

	// The token stream constructed from that expression string

	/**
	 * 符号列表
	 *  
	 */
	private List<Token> tokenStream = Collections.emptyList();

	// length of a populated token stream

	/**
	 * 符号流长度
	 */
	private int tokenStreamLength;

	// Current location in the token stream when processing tokens

	/**
	 * 当前符号所在位置
	 */
	private int tokenStreamPointer;

```


- 成员变量类型解释
    - SpelNodeImpl
        el表达式的节点对象
    - Token
        存储各种符号
      


- spring 中支持的符号表达详细内容在 `org.springframework.expression.spel.standard.TokenKind` 类中



## 构造函数


```java
	@Override
	protected SpelExpression doParseExpression(String expressionString, @Nullable ParserContext context)
			throws ParseException {

		try {
			this.expressionString = expressionString;
			// 分词器, 根据预设的表达式进行分词
			Tokenizer tokenizer = new Tokenizer(expressionString);
			this.tokenStream = tokenizer.process();
			this.tokenStreamLength = this.tokenStream.size();
			this.tokenStreamPointer = 0;
			this.constructedNodes.clear();
			// 解析el表达式
			SpelNodeImpl ast = eatExpression();
			Assert.state(ast != null, "No node");
			Token t = peekToken();
			if (t != null) {
				throw new SpelParseException(t.startPos, SpelMessage.MORE_INPUT, toString(nextToken()));
			}
			Assert.isTrue(this.constructedNodes.isEmpty(), "At least one node expected");
			return new SpelExpression(expressionString, ast, this.configuration);
		}
		catch (InternalParseException ex) {
			throw ex.getCause();
		}
	}

```

- 在构造函数中有关于el表达式解析得方法`eatExpression`下面概述其方法流程
    1. 获取表达式解析字符串 token
    2. 不同的 TokenKind 处理成 不同的 `Operator`(`SpelNodeImpl`) 对象