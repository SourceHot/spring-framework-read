# Spring AspectJAdviceParameterNameDiscoverer
- 类全路径: `org.springframework.aop.aspectj.AspectJAdviceParameterNameDiscoverer`
- `AspectJAdviceParameterNameDiscoverer` 用来发现切面参数


## 成员变量
singleValuedAnnotationPcds: 存储`Pointcut`表达式

nonReferencePointcutTokens: 存储 逻辑运算符


<details>
<summary>详细代码如下</summary>



```java


	/**
	 * 存储`Pointcut`表达式
	 */
	private static final Set<String> singleValuedAnnotationPcds = new HashSet<>();

	/**
	 * 逻辑运算符
	 */
	private static final Set<String> nonReferencePointcutTokens = new HashSet<>();
	static {
		singleValuedAnnotationPcds.add("@this");
		singleValuedAnnotationPcds.add("@target");
		singleValuedAnnotationPcds.add("@within");
		singleValuedAnnotationPcds.add("@withincode");
		singleValuedAnnotationPcds.add("@annotation");

		Set<PointcutPrimitive> pointcutPrimitives = PointcutParser.getAllSupportedPointcutPrimitives();
		for (PointcutPrimitive primitive : pointcutPrimitives) {
			nonReferencePointcutTokens.add(primitive.getName());
		}
		nonReferencePointcutTokens.add("&&");
		nonReferencePointcutTokens.add("!");
		nonReferencePointcutTokens.add("||");
		nonReferencePointcutTokens.add("and");
		nonReferencePointcutTokens.add("or");
		nonReferencePointcutTokens.add("not");
	}
```


</details>


- 上面介绍了两个关于切面表达式的内容, 在`AspectJAdviceParameterNameDiscoverer` 还有其他和 切面相关的属性.

<details>
<summary>详细代码如下</summary>


```java
/**
	 * The pointcut expression associated with the advice, as a simple String.
	 *
	 * 切面表达式
	 * */
	@Nullable
	private String pointcutExpression;

	private boolean raiseExceptions;

	/**
	 * If the advice is afterReturning, and binds the return value, this is the parameter name used.
	 *
	 * 返回值名称
	 * */
	@Nullable
	private String returningName;

	/**
	 * If the advice is afterThrowing, and binds the thrown value, this is the parameter name used.
	 * 异常名称
	 * */
	@Nullable
	private String throwingName;

	/**
	 * 参数类型
	 */
	private Class<?>[] argumentTypes = new Class<?>[0];

	/**
	 * 参数名称
	 */
	private String[] parameterNameBindings = new String[0];

	private int numberOfRemainingUnboundArguments;

```


</details>




## 方法分析
- 成员变量(属性)已经分析完成了. 接下来就是对方法的分析了 


### getParameterNames
- 方法签名: `org.springframework.aop.aspectj.AspectJAdviceParameterNameDiscoverer.getParameterNames(java.lang.reflect.Method)`

在 `getParameterNames` 中的整体处理逻辑就是下面这段代码

<details>
<summary>详细代码如下</summary>

```java
    int algorithmicStep = STEP_JOIN_POINT_BINDING;
    while ((this.numberOfRemainingUnboundArguments > 0) && algorithmicStep < STEP_FINISHED) {
        switch (algorithmicStep++) {
            case STEP_JOIN_POINT_BINDING:
                if (!maybeBindThisJoinPoint()) {
                    maybeBindThisJoinPointStaticPart();
                }
                break;
            case STEP_THROWING_BINDING:
                maybeBindThrowingVariable();
                break;
            case STEP_ANNOTATION_BINDING:
                maybeBindAnnotationsFromPointcutExpression();
                break;
            case STEP_RETURNING_BINDING:
                maybeBindReturningVariable();
                break;
            case STEP_PRIMITIVE_ARGS_BINDING:
                maybeBindPrimitiveArgsFromPointcutExpression();
                break;
            case STEP_THIS_TARGET_ARGS_BINDING:
                maybeBindThisOrTargetOrArgsFromPointcutExpression();
                break;
            case STEP_REFERENCE_PCUT_BINDING:
                maybeBindReferencePointcutParameter();
                break;
            default:
                throw new IllegalStateException("Unknown algorithmic step: " + (algorithmicStep - 1));
        }
    }

```

</details>




### maybeBindThisJoinPoint
- 方法签名: `org.springframework.aop.aspectj.AspectJAdviceParameterNameDiscoverer.maybeBindThisJoinPoint`
- 方法作用: 判断参数类型是否是 `JoinPoint` 和 `ProceedingJoinPoint` , 进行参数绑定

<details>
<summary>详细代码如下</summary>

```java

	private boolean maybeBindThisJoinPoint() {
		if ((this.argumentTypes[0] == JoinPoint.class) || (this.argumentTypes[0] == ProceedingJoinPoint.class)) {
			bindParameterName(0, THIS_JOIN_POINT);
			return true;
		}
		else {
			return false;
		}
	}

```

</details>



### maybeBindThisJoinPointStaticPart
- 方法签名: `org.springframework.aop.aspectj.AspectJAdviceParameterNameDiscoverer.maybeBindThisJoinPointStaticPart`
- 方法作用: 判断参数类型是否是`JoinPoint.StaticPart` , 进行参数绑定


<details>
<summary>详细代码如下</summary>


```java
private void maybeBindThisJoinPointStaticPart() {
		if (this.argumentTypes[0] == JoinPoint.StaticPart.class) {
			bindParameterName(0, THIS_JOIN_POINT_STATIC_PART);
		}
	}
```

</details>


### maybeBindThrowingVariable
- 方法签名: `org.springframework.aop.aspectj.AspectJAdviceParameterNameDiscoverer.maybeBindThrowingVariable`
- 方法作用: 判断参数类型是否是`Throwable` , 进行参数绑定


<details>
<summary>详细代码如下</summary>

```java
private void maybeBindThrowingVariable() {
		if (this.throwingName == null) {
			return;
		}

		// So there is binding work to do...
		int throwableIndex = -1;
		for (int i = 0; i < this.argumentTypes.length; i++) {
			if (isUnbound(i) && isSubtypeOf(Throwable.class, i)) {
				if (throwableIndex == -1) {
					throwableIndex = i;
				}
				else {
					// Second candidate we've found - ambiguous binding
					throw new AmbiguousBindingException("Binding of throwing parameter '" +
							this.throwingName + "' is ambiguous: could be bound to argument " +
							throwableIndex + " or argument " + i);
				}
			}
		}

		if (throwableIndex == -1) {
			throw new IllegalStateException("Binding of throwing parameter '" + this.throwingName
					+ "' could not be completed as no available arguments are a subtype of Throwable");
		}
		else {
			bindParameterName(throwableIndex, this.throwingName);
		}
	}
```

</details>




### maybeBindAnnotationsFromPointcutExpression
- 方法签名: `org.springframework.aop.aspectj.AspectJAdviceParameterNameDiscoverer.maybeBindAnnotationsFromPointcutExpression`
- 方法作用: 处理 切面表达式的相关参数绑定

<details>
<summary>详细代码如下</summary>

```java
	private void maybeBindAnnotationsFromPointcutExpression() {
		List<String> varNames = new ArrayList<>();
		// 切面表达式切分
		String[] tokens = StringUtils.tokenizeToStringArray(this.pointcutExpression, " ");
		for (int i = 0; i < tokens.length; i++) {
			// 待匹配的表达式
			String toMatch = tokens[i];
			int firstParenIndex = toMatch.indexOf('(');
			if (firstParenIndex != -1) {
				toMatch = toMatch.substring(0, firstParenIndex);
			}
			// 是否是 @this @target @within @withincode @annotation 的内容
			if (singleValuedAnnotationPcds.contains(toMatch)) {
				// 获取表达式内容
				PointcutBody body = getPointcutBody(tokens, i);
				i += body.numTokensConsumed;
                // 提取变量名
				String varName = maybeExtractVariableName(body.text);
				if (varName != null) {
					varNames.add(varName);
				}
			}
			// args 的参数处理
			else if (tokens[i].startsWith("@args(") || tokens[i].equals("@args")) {
				PointcutBody body = getPointcutBody(tokens, i);
				i += body.numTokensConsumed;
				// 从 arg 中提取变量名
				maybeExtractVariableNamesFromArgs(body.text, varNames);
			}
		}

		// 注解处理
		bindAnnotationsFromVarNames(varNames);
	}

```

</details>



### maybeBindReturningVariable
- 方法签名: `org.springframework.aop.aspectj.AspectJAdviceParameterNameDiscoverer.maybeBindReturningVariable`
- 方法作用: 返回值数据的绑定



<details>
<summary>详细代码如下</summary>


```java
	private void maybeBindReturningVariable() {
		if (this.numberOfRemainingUnboundArguments == 0) {
			throw new IllegalStateException(
					"Algorithm assumes that there must be at least one unbound parameter on entry to this method");
		}

		if (this.returningName != null) {
			if (this.numberOfRemainingUnboundArguments > 1) {
				throw new AmbiguousBindingException("Binding of returning parameter '" + this.returningName +
						"' is ambiguous, there are " + this.numberOfRemainingUnboundArguments + " candidates.");
			}

			// We're all set... find the unbound parameter, and bind it.
			for (int i = 0; i < this.parameterNameBindings.length; i++) {
				if (this.parameterNameBindings[i] == null) {
					bindParameterName(i, this.returningName);
					break;
				}
			}
		}
	}

```

</details>



### maybeBindPrimitiveArgsFromPointcutExpression
- 方法签名: `org.springframework.aop.aspectj.AspectJAdviceParameterNameDiscoverer.maybeBindPrimitiveArgsFromPointcutExpression`
- 方法作用: 处理表达式中的 arg 相关属性名称

<details>
<summary>详细代码如下</summary>

```java
	private void maybeBindPrimitiveArgsFromPointcutExpression() {
		int numUnboundPrimitives = countNumberOfUnboundPrimitiveArguments();
		if (numUnboundPrimitives > 1) {
			throw new AmbiguousBindingException("Found '" + numUnboundPrimitives +
					"' unbound primitive arguments with no way to distinguish between them.");
		}
		if (numUnboundPrimitives == 1) {
			// Look for arg variable and bind it if we find exactly one...
			List<String> varNames = new ArrayList<>();
			String[] tokens = StringUtils.tokenizeToStringArray(this.pointcutExpression, " ");
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].equals("args") || tokens[i].startsWith("args(")) {
					PointcutBody body = getPointcutBody(tokens, i);
					i += body.numTokensConsumed;
					maybeExtractVariableNamesFromArgs(body.text, varNames);
				}
			}
			if (varNames.size() > 1) {
				throw new AmbiguousBindingException("Found " + varNames.size() +
						" candidate variable names but only one candidate binding slot when matching primitive args");
			}
			else if (varNames.size() == 1) {
				// 1 primitive arg, and one candidate...
				for (int i = 0; i < this.argumentTypes.length; i++) {
					if (isUnbound(i) && this.argumentTypes[i].isPrimitive()) {
						bindParameterName(i, varNames.get(0));
						break;
					}
				}
			}
		}
	}

```
</details>




### maybeBindThisOrTargetOrArgsFromPointcutExpression
- 方法签名: `org.springframework.aop.aspectj.AspectJAdviceParameterNameDiscoverer.maybeBindThisOrTargetOrArgsFromPointcutExpression`
- 方法作用: 处理 `this` 和 `target` 和 `args` 参数绑定


<details>
<summary>详细代码如下</summary>

```java
	private void maybeBindThisOrTargetOrArgsFromPointcutExpression() {
		if (this.numberOfRemainingUnboundArguments > 1) {
			throw new AmbiguousBindingException("Still " + this.numberOfRemainingUnboundArguments
					+ " unbound args at this(),target(),args() binding stage, with no way to determine between them");
		}

		List<String> varNames = new ArrayList<>();
		String[] tokens = StringUtils.tokenizeToStringArray(this.pointcutExpression, " ");
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("this") ||
					tokens[i].startsWith("this(") ||
					tokens[i].equals("target") ||
					tokens[i].startsWith("target(")) {
				PointcutBody body = getPointcutBody(tokens, i);
				i += body.numTokensConsumed;
				String varName = maybeExtractVariableName(body.text);
				if (varName != null) {
					varNames.add(varName);
				}
			}
			else if (tokens[i].equals("args") || tokens[i].startsWith("args(")) {
				PointcutBody body = getPointcutBody(tokens, i);
				i += body.numTokensConsumed;
				List<String> candidateVarNames = new ArrayList<>();
				maybeExtractVariableNamesFromArgs(body.text, candidateVarNames);
				// we may have found some var names that were bound in previous primitive args binding step,
				// filter them out...
				for (String varName : candidateVarNames) {
					if (!alreadyBound(varName)) {
						varNames.add(varName);
					}
				}
			}
		}


		if (varNames.size() > 1) {
			throw new AmbiguousBindingException("Found " + varNames.size() +
					" candidate this(), target() or args() variables but only one unbound argument slot");
		}
		else if (varNames.size() == 1) {
			for (int j = 0; j < this.parameterNameBindings.length; j++) {
				if (isUnbound(j)) {
					bindParameterName(j, varNames.get(0));
					break;
				}
			}
		}
		// else varNames.size must be 0 and we have nothing to bind.
	}

```

</details>


### maybeBindReferencePointcutParameter
- 方法签名: `org.springframework.aop.aspectj.AspectJAdviceParameterNameDiscoverer.maybeBindReferencePointcutParameter`
- 方法作用: 


### bindParameterName