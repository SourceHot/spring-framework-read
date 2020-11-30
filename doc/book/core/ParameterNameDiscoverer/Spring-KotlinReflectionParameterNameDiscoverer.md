# Spring KotlinReflectionParameterNameDiscoverer

- 类全路径: `org.springframework.core.KotlinReflectionParameterNameDiscoverer`
- 从类名可以看出这是一个和 KOTLIN 相关的一个类. 这个类和 `StandardReflectionParameterNameDiscoverer` 相似, 都是使用的语言原生的方法来获取 参数名称. 









整体依赖 kotlin 相关实现. 这里直接贴出代码. 不做具体分析



```java
public class KotlinReflectionParameterNameDiscoverer implements ParameterNameDiscoverer {

	@Override
	@Nullable
	public String[] getParameterNames(Method method) {
		// 判断是不是 kotlin 类
		if (!KotlinDetector.isKotlinType(method.getDeclaringClass())) {
			return null;
		}

		try {
			// kotlin 函数获取
			KFunction<?> function = ReflectJvmMapping.getKotlinFunction(method);
			return (function != null ? getParameterNames(function.getParameters()) : null);
		}
		catch (UnsupportedOperationException ex) {
			return null;
		}
	}

	@Override
	@Nullable
	public String[] getParameterNames(Constructor<?> ctor) {
		if (ctor.getDeclaringClass().isEnum() || !KotlinDetector.isKotlinType(ctor.getDeclaringClass())) {
			return null;
		}

		try {
			KFunction<?> function = ReflectJvmMapping.getKotlinFunction(ctor);
			return (function != null ? getParameterNames(function.getParameters()) : null);
		}
		catch (UnsupportedOperationException ex) {
			return null;
		}
	}

	@Nullable
	private String[] getParameterNames(List<KParameter> parameters) {
		List<KParameter> filteredParameters = parameters
				.stream()
				// Extension receivers of extension methods must be included as they appear as normal method parameters in Java
				.filter(p -> KParameter.Kind.VALUE.equals(p.getKind()) || KParameter.Kind.EXTENSION_RECEIVER.equals(p.getKind()))
				.collect(Collectors.toList());
		String[] parameterNames = new String[filteredParameters.size()];
		for (int i = 0; i < filteredParameters.size(); i++) {
			KParameter parameter = filteredParameters.get(i);
			// extension receivers are not explicitly named, but require a name for Java interoperability
			// $receiver is not a valid Kotlin identifier, but valid in Java, so it can be used here
			String name = KParameter.Kind.EXTENSION_RECEIVER.equals(parameter.getKind())  ? "$receiver" : parameter.getName();
			if (name == null) {
				return null;
			}
			parameterNames[i] = name;
		}
		return parameterNames;
	}

}
```