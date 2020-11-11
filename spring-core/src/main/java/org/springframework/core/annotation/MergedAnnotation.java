/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Proxy;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.lang.Nullable;

/**
 * A single merged annotation returned from a {@link MergedAnnotations}
 * collection. Presents a view onto an annotation where attribute values may
 * have been "merged" from different source values.
 *
 * <p>Attribute values may be accessed using the various {@code get} methods.
 * For example, to access an {@code int} attribute the {@link #getInt(String)}
 * method would be used.
 *
 * <p>Note that attribute values are <b>not</b> converted when accessed.
 * For example, it is not possible to call {@link #getString(String)} if the
 * underlying attribute is an {@code int}. The only exception to this rule is
 * {@code Class} and {@code Class[]} values which may be accessed as
 * {@code String} and {@code String[]} respectively to prevent potential early
 * class initialization.
 *
 * <p>If necessary, a {@code MergedAnnotation} can be {@linkplain #synthesize()
 * synthesized} back into an actual {@link java.lang.annotation.Annotation}.
 *
 * @author Phillip Webb
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 5.2
 * @param <A> the annotation type
 * @see MergedAnnotations
 * @see MergedAnnotationPredicates
 */
public interface MergedAnnotation<A extends Annotation> {

	/**
	 * The attribute name for annotations with a single element.
	 * 属性字段
	 */
	String VALUE = "value";


	/**
	 * Get the {@code Class} reference for the actual annotation type.
	 *
	 * 获取注解类型
	 * @return the annotation type
	 */
	Class<A> getType();

	/**
	 * Determine if the annotation is present on the source. Considers
	 * {@linkplain #isDirectlyPresent() directly present} and
	 * {@linkplain #isMetaPresent() meta-present} annotations within the context
	 * of the {@link SearchStrategy} used.
	 *
	 * 是否使用
	 * @return {@code true} if the annotation is present
	 */
	boolean isPresent();

	/**
	 * Determine if the annotation is directly present on the source.
	 * <p>A directly present annotation is one that the user has explicitly
	 * declared and not one that is {@linkplain #isMetaPresent() meta-present}
	 * or {@link Inherited @Inherited}.
	 * 注解是否直接使用. 非注解嵌套
	 * @return {@code true} if the annotation is directly present
	 */
	boolean isDirectlyPresent();

	/**
	 * Determine if the annotation is meta-present on the source.
	 * <p>A meta-present annotation is an annotation that the user hasn't
	 * explicitly declared, but has been used as a meta-annotation somewhere in
	 * the annotation hierarchy.
	 * 是否元注解使用
	 * @return {@code true} if the annotation is meta-present
	 */
	boolean isMetaPresent();

	/**
	 * Get the distance of this annotation related to its use as a
	 * meta-annotation.
	 * <p>A directly declared annotation has a distance of {@code 0}, a
	 * meta-annotation has a distance of {@code 1}, a meta-annotation on a
	 * meta-annotation has a distance of {@code 2}, etc. A {@linkplain #missing()
	 * missing} annotation will always return a distance of {@code -1}.
	 *
	 * 注解距离
	 * 0  => 直接使用注解
	 * 1  => 元注解
	 * 2  => 元注解的元注解
	 * -1 => 没有注解
	 * @return the annotation distance or {@code -1} if the annotation is missing
	 */
	int getDistance();

	/**
	 * Get the index of the aggregate collection containing this annotation.
	 * <p>Can be used to reorder a stream of annotations, for example, to give a
	 * higher priority to annotations declared on a superclass or interface. A
	 * {@linkplain #missing() missing} annotation will always return an aggregate
	 * index of {@code -1}.
	 * @return the aggregate index (starting at {@code 0}) or {@code -1} if the
	 * annotation is missing
	 */
	int getAggregateIndex();

	/**
	 * Get the source that ultimately declared the root annotation, or
	 * {@code null} if the source is not known.
	 * <p>If this merged annotation was created
	 * {@link MergedAnnotations#from(AnnotatedElement) from} an
	 * {@link AnnotatedElement} then this source will be an element of the same
	 * type. If the annotation was loaded without using reflection, the source
	 * can be of any type, but should have a sensible {@code toString()}.
	 * Meta-annotations will always return the same source as the
	 * {@link #getRoot() root}.
	 * 元对象
	 * @return the source, or {@code null}
	 */
	@Nullable
	Object getSource();

	/**
	 * Get the source of the meta-annotation, or {@code null} if the
	 * annotation is not {@linkplain #isMetaPresent() meta-present}.
	 * <p>The meta-source is the annotation that was meta-annotated with this
	 * annotation.
	 *
	 * 注解元对象
	 * @return the meta-annotation source or {@code null}
	 * @see #getRoot()
	 */
	@Nullable
	MergedAnnotation<?> getMetaSource();

	/**
	 * Get the root annotation, i.e. the {@link #getDistance() distance} {@code 0}
	 * annotation as directly declared on the source.
	 * 获取根注解
	 * @return the root annotation
	 * @see #getMetaSource()
	 */
	MergedAnnotation<?> getRoot();

	/**
	 * Get the complete list of annotation types within the annotation hierarchy
	 * from this annotation to the {@link #getRoot() root}.
	 * <p>Provides a useful way to uniquely identify a merged annotation instance.
	 * 获取注解列表
	 * @return the meta types for the annotation
	 * @see MergedAnnotationPredicates#unique(Function)
	 * @see #getRoot()
	 * @see #getMetaSource()
	 */
	List<Class<? extends Annotation>> getMetaTypes();


	/**
	 * Determine if the specified attribute name has a non-default value when
	 * compared to the annotation declaration.
	 * 是否不存在默认属性值
	 * @param attributeName the attribute name
	 * @return {@code true} if the attribute value is different from the default
	 * value
	 */
	boolean hasNonDefaultValue(String attributeName);

	/**
	 * Determine if the specified attribute name has a default value when compared
	 * to the annotation declaration.
	 * 是否存在默认属性值
	 * @param attributeName the attribute name
	 * @return {@code true} if the attribute value is the same as the default
	 * value
	 */
	boolean hasDefaultValue(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required byte attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a byte
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	byte getByte(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required byte array attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a byte array
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	byte[] getByteArray(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required boolean attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a boolean
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	boolean getBoolean(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required boolean array attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a boolean array
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	boolean[] getBooleanArray(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required char attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a char
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	char getChar(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required char array attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a char array
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	char[] getCharArray(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required short attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a short
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	short getShort(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required short array attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a short array
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	short[] getShortArray(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required int attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as an int
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	int getInt(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required int array attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as an int array
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	int[] getIntArray(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required long attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a long
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	long getLong(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required long array attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a long array
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	long[] getLongArray(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required double attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a double
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	double getDouble(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required double array attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a double array
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	double[] getDoubleArray(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required float attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a float
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	float getFloat(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required float array attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a float array
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	float[] getFloatArray(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required string attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a string
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	String getString(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required string array attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a string array
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	String[] getStringArray(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required class attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a class
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	Class<?> getClass(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required class array attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @return the value as a class array
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	Class<?>[] getClassArray(String attributeName) throws NoSuchElementException;

	/**
	 * Get a required enum attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @param type the enum type
	 * @return the value as a enum
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	<E extends Enum<E>> E getEnum(String attributeName, Class<E> type) throws NoSuchElementException;

	/**
	 * Get a required enum array attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @param type the enum type
	 * @return the value as a enum array
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	<E extends Enum<E>> E[] getEnumArray(String attributeName, Class<E> type) throws NoSuchElementException;

	/**
	 * Get a required annotation attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @param type the annotation type
	 * @return the value as a {@link MergedAnnotation}
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	<T extends Annotation> MergedAnnotation<T> getAnnotation(String attributeName, Class<T> type)
			throws NoSuchElementException;

	/**
	 * Get a required annotation array attribute value from the annotation.
	 * @param attributeName the attribute name
	 * @param type the annotation type
	 * @return the value as a {@link MergedAnnotation} array
	 * @throws NoSuchElementException if there is no matching attribute
	 */
	<T extends Annotation> MergedAnnotation<T>[] getAnnotationArray(String attributeName, Class<T> type)
			throws NoSuchElementException;

	/**
	 * Get an optional attribute value from the annotation.
	 * 获取属性值
	 * @param attributeName the attribute name
	 * @return an optional value or {@link Optional#empty()} if there is no
	 * matching attribute
	 */
	Optional<Object> getValue(String attributeName);

	/**
	 * Get an optional attribute value from the annotation.
	 * 通过属性名称+类型返回属性值
	 * @param attributeName the attribute name
	 * @param type the attribute type. Must be compatible with the underlying
	 * attribute type or {@code Object.class}.
	 * @return an optional value or {@link Optional#empty()} if there is no
	 * matching attribute
	 */
	<T> Optional<T> getValue(String attributeName, Class<T> type);

	/**
	 * Get the default attribute value from the annotation as specified in
	 * the annotation declaration.
	 * 获取默认属性值
	 * @param attributeName the attribute name
	 * @return an optional of the default value or {@link Optional#empty()} if
	 * there is no matching attribute or no defined default
	 */
	Optional<Object> getDefaultValue(String attributeName);

	/**
	 * Get the default attribute value from the annotation as specified in
	 * the annotation declaration.
	 * 获取默认属性值带类型
	 * @param attributeName the attribute name
	 * @param type the attribute type. Must be compatible with the underlying
	 * attribute type or {@code Object.class}.
	 * @return an optional of the default value or {@link Optional#empty()} if
	 * there is no matching attribute or no defined default
	 */
	<T> Optional<T> getDefaultValue(String attributeName, Class<T> type);

	/**
	 * Create a new view of the annotation with all attributes that have default
	 * values removed.
	 * 创建一个新的 MergedAnnotation 删除默认属性值
	 * @return a filtered view of the annotation without any attributes that
	 * have a default value
	 * @see #filterAttributes(Predicate)
	 */
	MergedAnnotation<A> filterDefaultValues();

	/**
	 * Create a new view of the annotation with only attributes that match the
	 * given predicate.
	 * 创建一个新的 MergedAnnotation 过滤部分属性
	 * @param predicate a predicate used to filter attribute names
	 * @return a filtered view of the annotation
	 * @see #filterDefaultValues()
	 * @see MergedAnnotationPredicates
	 */
	MergedAnnotation<A> filterAttributes(Predicate<String> predicate);

	/**
	 * Create a new view of the annotation that exposes non-merged attribute values.
	 * <p>Methods from this view will return attribute values with only alias mirroring
	 * rules applied. Aliases to {@link #getMetaSource() meta-source} attributes will
	 * not be applied.
	 * 创建于给新的 MergedAnnotation 具有所有属性, 不合并属性
	 * @return a non-merged view of the annotation
	 */
	MergedAnnotation<A> withNonMergedAttributes();

	/**
	 * Create a new mutable {@link AnnotationAttributes} instance from this
	 * merged annotation.
	 * <p>The {@link Adapt adaptations} may be used to change the way that values
	 * are added.
	 *
	 * 获取注解属性
	 * @param adaptations adaptations that should be applied to the annotation values
	 * @return an immutable map containing the attributes and values
	 */
	AnnotationAttributes asAnnotationAttributes(Adapt... adaptations);

	/**
	 * Get an immutable {@link Map} that contains all the annotation attributes.
	 * <p>The {@link Adapt adaptations} may be used to change the way that values are added.
	 *
	 * 将注解属性转换成 map
	 * @param adaptations adaptations that should be applied to the annotation values
	 * @return an immutable map containing the attributes and values
	 */
	Map<String, Object> asMap(Adapt... adaptations);

	/**
	 * Create a new {@link Map} instance of the given type that contains all the annotation
	 * attributes.
	 * <p>The {@link Adapt adaptations} may be used to change the way that values are added.
	 *
	 * 注解转 map
	 * @param factory a map factory
	 * @param adaptations adaptations that should be applied to the annotation values
	 * @return a map containing the attributes and values
	 */
	<T extends Map<String, Object>> T asMap(Function<MergedAnnotation<?>, T> factory, Adapt... adaptations);

	/**
	 * Create a type-safe synthesized version of this annotation that can be
	 * used directly in code.
	 * <p>The result is synthesized using a JDK {@link Proxy} and as a result may
	 * incur a computational cost when first invoked.
	 * 注解合并
	 * @return a synthesized version of the annotation.
	 * @throws NoSuchElementException on a missing annotation
	 */
	A synthesize() throws NoSuchElementException;

	/**
	 * Optionally create a type-safe synthesized version of this annotation based
	 * on a condition predicate.
	 * <p>The result is synthesized using a JDK {@link Proxy} and as a result may
	 * incur a computational cost when first invoked.
	 * 注解合并
	 * @param condition the test to determine if the annotation can be synthesized
	 * @return a optional containing the synthesized version of the annotation or
	 * an empty optional if the condition doesn't match
	 * @throws NoSuchElementException on a missing annotation
	 * @see MergedAnnotationPredicates
	 */
	Optional<A> synthesize(Predicate<? super MergedAnnotation<A>> condition) throws NoSuchElementException;


	/**
	 * Adaptations that can be applied to attribute values when creating
	 * {@linkplain MergedAnnotation#asMap(Adapt...) Maps} or
	 * {@link MergedAnnotation#asAnnotationAttributes(Adapt...) AnnotationAttributes}.
	 */
	enum Adapt {

		/**
		 * Adapt class or class array attributes to strings.
		 *
		 * class 转换成 string
		 */
		CLASS_TO_STRING,

		/**
		 * Adapt nested annotation or annotation arrays to maps rather
		 * than synthesizing the values.
		 * 注解 转换成 map
		 */
		ANNOTATION_TO_MAP;

		/**
		 * Factory method to create an {@link Adapt} array from a set of boolean flags.
		 * @param classToString if {@link Adapt#CLASS_TO_STRING} is included
		 * @param annotationsToMap if {@link Adapt#ANNOTATION_TO_MAP} is included
		 * @return a new {@link Adapt} array
		 */
		public static Adapt[] values(boolean classToString, boolean annotationsToMap) {
			EnumSet<Adapt> result = EnumSet.noneOf(Adapt.class);
			addIfTrue(result, Adapt.CLASS_TO_STRING, classToString);
			addIfTrue(result, Adapt.ANNOTATION_TO_MAP, annotationsToMap);
			return result.toArray(new Adapt[0]);
		}

		private static <T> void addIfTrue(Set<T> result, T value, boolean test) {
			if (test) {
				result.add(value);
			}
		}

		protected final boolean isIn(Adapt... adaptations) {
			for (Adapt candidate : adaptations) {
				if (candidate == this) {
					return true;
				}
			}
			return false;
		}
	}

}
