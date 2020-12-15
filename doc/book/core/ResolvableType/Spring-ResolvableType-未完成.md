# Spring ResolvableType
- 类全路径: `org.springframework.core.ResolvableType`

- 类说明: `ResolvableType` 时对 `java.lang.reflect.Type`封装



- 在`ResolvableType`中有下面几个内部类(接口、类), 我们需要对其进行了解, 因为在方法中还有调用

    - VariableResolver
    - DefaultVariableResolver
    - TypeVariablesVariableResolver
    - SyntheticParameterizedType
    - WildcardBounds
    - EmptyType





## VariableResolver

- 类全路径: `org.springframework.core.ResolvableType.VariableResolver`



<details>
<summary>VariableResolver 详细代码</summary>

```java
/**
 * Strategy interface used to resolve {@link TypeVariable TypeVariables}.
 */
interface VariableResolver extends Serializable {

   /**
    * Return the source of the resolver (used for hashCode and equals).
    * 源对象
    */
   Object getSource();

   /**
    * Resolve the specified variable.
    *
    * 解析 TypeVariable 接口
    * @param variable the variable to resolve
    * @return the resolved variable, or {@code null} if not found
    */
   @Nullable
   ResolvableType resolveVariable(TypeVariable<?> variable);
}
```





</details>



这里需要注意的一点: getSource 对象是 ResolvableType 单个或多个





## DefaultVariableResolver

- 类全路径: `org.springframework.core.ResolvableType.DefaultVariableResolver`



<details>
<summary>DefaultVariableResolver 详细代码</summary>

```java
@SuppressWarnings("serial")
private static class DefaultVariableResolver implements VariableResolver {

   private final ResolvableType source;

   DefaultVariableResolver(ResolvableType resolvableType) {
      this.source = resolvableType;
   }

   @Override
   @Nullable
   public ResolvableType resolveVariable(TypeVariable<?> variable) {
      return this.source.resolveVariable(variable);
   }

   @Override
   public Object getSource() {
      return this.source;
   }
}
```

</details>





## TypeVariablesVariableResolver

- 类全路径: `org.springframework.core.ResolvableType.TypeVariablesVariableResolver`



<details>
<summary>TypeVariablesVariableResolver 详细代码</summary>

```java
@SuppressWarnings("serial")
private static class TypeVariablesVariableResolver implements VariableResolver {

   private final TypeVariable<?>[] variables;

   private final ResolvableType[] generics;

   public TypeVariablesVariableResolver(TypeVariable<?>[] variables, ResolvableType[] generics) {
      this.variables = variables;
      this.generics = generics;
   }

   @Override
   @Nullable
   public ResolvableType resolveVariable(TypeVariable<?> variable) {
      TypeVariable<?> variableToCompare = SerializableTypeWrapper.unwrap(variable);
      for (int i = 0; i < this.variables.length; i++) {
         TypeVariable<?> resolvedVariable = SerializableTypeWrapper.unwrap(this.variables[i]);
         // 相同返回
         if (ObjectUtils.nullSafeEquals(resolvedVariable, variableToCompare)) {
            return this.generics[i];
         }
      }
      return null;
   }

   @Override
   public Object getSource() {
      return this.generics;
   }
}
```

</details>







## SyntheticParameterizedType

- 类全路径: `org.springframework.core.ResolvableType.SyntheticParameterizedType`





<details>
<summary>SyntheticParameterizedType 详细代码</summary>

```java
private static final class SyntheticParameterizedType implements ParameterizedType, Serializable {

   private final Type rawType;

   private final Type[] typeArguments;

   public SyntheticParameterizedType(Type rawType, Type[] typeArguments) {
      this.rawType = rawType;
      this.typeArguments = typeArguments;
   }

   @Override
   public String getTypeName() {
      String typeName = this.rawType.getTypeName();
      if (this.typeArguments.length > 0) {
         StringJoiner stringJoiner = new StringJoiner(", ", "<", ">");
         for (Type argument : this.typeArguments) {
            stringJoiner.add(argument.getTypeName());
         }
         return typeName + stringJoiner;
      }
      return typeName;
   }
}
```

</details>





## 方法分析

### forType

- 方法签名: `org.springframework.core.ResolvableType#forType(java.lang.reflect.Type, org.springframework.core.SerializableTypeWrapper.TypeProvider, org.springframework.core.ResolvableType.VariableResolver)`





<details>
<summary>forType 第一部分代码</summary>

```java
// 第一部分
if (type == null && typeProvider != null) {
   type = SerializableTypeWrapper.forTypeProvider(typeProvider);
}
if (type == null) {
   return NONE;
}

// For simple Class references, build the wrapper right away -
// no expensive resolution necessary, so not worth caching...
// 如果是 class 类型 直接 new 创建
if (type instanceof Class) {
   return new ResolvableType(type, typeProvider, variableResolver, (ResolvableType) null);
}

// Purge empty entries on access since we don't have a clean-up thread or the like.
cache.purgeUnreferencedEntries();
```

</details>





第一部分中有两种构造`ResolvableType`方式

1. `SerializableTypeWrapper.forTypeProvider(typeProvider)`
2. `new ResolvableType(type, typeProvider, variableResolver, (ResolvableType) null)`



<details>
<summary>forType 第二部分代码</summary>

```java
// 第二部分
// Check the cache - we may have a ResolvableType which has been resolved before...
ResolvableType resultType = new ResolvableType(type, typeProvider, variableResolver);
ResolvableType cachedType = cache.get(resultType);
if (cachedType == null) {
   cachedType = new ResolvableType(type, typeProvider, variableResolver, resultType.hash);
   cache.put(cachedType, cachedType);
}
resultType.resolved = cachedType.resolved;
return resultType;
```

</details>




### hasGenerics
- 方法签名: `org.springframework.core.ResolvableType.hasGenerics`