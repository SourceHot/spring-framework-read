# Spring AnnotationMetadata
- 类全路径: `org.springframework.core.type.AnnotationMetadata`
- `AnnotationMetadata` 接口主要用来描述 注解的元信息. 接下来对 `AnnotationMetadata` 进行方法说明


## 方法列表
- 这里的英文注释不做删除, 笔者对这里的方法存在一定的疑惑, 保留以供后续查看. 

```java
public interface AnnotationMetadata extends ClassMetadata, AnnotatedTypeMetadata {

	/**
	 * Get the fully qualified class names of all annotation types that
	 * are <em>present</em> on the underlying class.
     * 获取注解名称,全类名
	 * @return the annotation type names
	 */
	default Set<String> getAnnotationTypes() {
		return getAnnotations().stream()
				// 判断是否直接使用注解
				.filter(MergedAnnotation::isDirectlyPresent)
				// 获取注解的名字
				.map(annotation -> annotation.getType().getName())
				// 转换成set
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Get the fully qualified class names of all meta-annotation types that
	 * are <em>present</em> on the given annotation type on the underlying class.
	 * @param annotationName the fully qualified class name of the meta-annotation
	 * type to look for
	 * @return the meta-annotation type names, or an empty set if none found
     *
     * 注解全类名
	 */
	default Set<String> getMetaAnnotationTypes(String annotationName) {
		// 获取注解合并后的结果
		MergedAnnotation<?> annotation = getAnnotations().get(annotationName, MergedAnnotation::isDirectlyPresent);
		// 判断注解是否使用
		if (!annotation.isPresent()) {
			// 不使用直接返回空
			return Collections.emptySet();
		}
		// 注解使用 继承查找获得所有注解名称(类名)
		return MergedAnnotations.from(annotation.getType(), SearchStrategy.INHERITED_ANNOTATIONS).stream()
				.map(mergedAnnotation -> mergedAnnotation.getType().getName())
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Determine whether an annotation of the given type is <em>present</em> on
	 * the underlying class.
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 * @return {@code true} if a matching annotation is present
     *
     * 是否包含某个注解
	 */
	default boolean hasAnnotation(String annotationName) {
		return getAnnotations().isDirectlyPresent(annotationName);
	}

	/**
	 * Determine whether the underlying class has an annotation that is itself
	 * annotated with the meta-annotation of the given type.
	 * @param metaAnnotationName the fully qualified class name of the
	 * meta-annotation type to look for
	 * @return {@code true} if a matching meta-annotation is present
     *
     * 是否被某个注解标记过
	 */
	default boolean hasMetaAnnotation(String metaAnnotationName) {
		return getAnnotations().get(metaAnnotationName,
				MergedAnnotation::isMetaPresent).isPresent();
	}

	/**
	 * Determine whether the underlying class has any methods that are
	 * annotated (or meta-annotated) with the given annotation type.
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
     *
     * 是否有注解,类里面有一个注解就返回true
	 */
	default boolean hasAnnotatedMethods(String annotationName) {
		return !getAnnotatedMethods(annotationName).isEmpty();
	}

	/**
	 * Retrieve the method metadata for all methods that are annotated
	 * (or meta-annotated) with the given annotation type.
	 * <p>For any returned method, {@link MethodMetadata#isAnnotated} will
	 * return {@code true} for the given annotation type.
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 * @return a set of {@link MethodMetadata} for methods that have a matching
	 * annotation. The return value will be an empty set if no methods match
	 * the annotation type.
     *
     * 获取包含注解的方法
	 */
	Set<MethodMetadata> getAnnotatedMethods(String annotationName);


	/**
	 * Factory method to create a new {@link AnnotationMetadata} instance
	 * for the given class using standard reflection.
     *
     * 通过反射创建一个注解的元信息
	 * @param type the class to introspect
	 * @return a new {@link AnnotationMetadata} instance
	 * @since 5.2
	 */
	static AnnotationMetadata introspect(Class<?> type) {
		return StandardAnnotationMetadata.from(type);
	}

}
```