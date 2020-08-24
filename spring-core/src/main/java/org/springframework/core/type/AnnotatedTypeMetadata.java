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

package org.springframework.core.type;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotation.Adapt;
import org.springframework.core.annotation.MergedAnnotationCollectors;
import org.springframework.core.annotation.MergedAnnotationPredicates;
import org.springframework.core.annotation.MergedAnnotationSelectors;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;

/**
 * Defines access to the annotations of a specific type ({@link AnnotationMetadata class}
 * or {@link MethodMetadata method}), in a form that does not necessarily require the
 * class-loading.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Mark Pollack
 * @author Chris Beams
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 4.0
 * @see AnnotationMetadata
 * @see MethodMetadata
 */
public interface AnnotatedTypeMetadata {

    /**
     * Return annotation details based on the direct annotations of the
     * underlying element.
     * 获取所有注解
     * @return merged annotations based on the direct annotations
     * @since 5.2
     */
    MergedAnnotations getAnnotations();

    /**
     * Determine whether the underlying element has an annotation or meta-annotation
     * of the given type defined.
     * <p>If this method returns {@code true}, then
     * {@link #getAnnotationAttributes} will return a non-null Map.
     * @param annotationName the fully qualified class name of the annotation
     * type to look for
     * @return whether a matching annotation is defined
     *
     * 是否有注解
     */
    default boolean isAnnotated(String annotationName) {
        return getAnnotations().isPresent(annotationName);
    }

    /**
     * Retrieve the attributes of the annotation of the given type, if any (i.e. if
     * defined on the underlying element, as direct annotation or meta-annotation),
     * also taking attribute overrides on composed annotations into account.
     * @param annotationName the fully qualified class name of the annotation
     * type to look for
     * @return a Map of attributes, with the attribute name as key (e.g. "value")
     * and the defined attribute value as Map value. This return value will be
     * {@code null} if no matching annotation is defined.
     *
     * 获取注解的属性
     */
    @Nullable
    default Map<String, Object> getAnnotationAttributes(String annotationName) {
        return getAnnotationAttributes(annotationName, false);
    }

    /**
     * Retrieve the attributes of the annotation of the given type, if any (i.e. if
     * defined on the underlying element, as direct annotation or meta-annotation),
     * also taking attribute overrides on composed annotations into account.
     * @param annotationName the fully qualified class name of the annotation
     * type to look for
     * @param classValuesAsString whether to convert class references to String
     * class names for exposure as values in the returned Map, instead of Class
     * references which might potentially have to be loaded first
     * @return a Map of attributes, with the attribute name as key (e.g. "value")
     * and the defined attribute value as Map value. This return value will be
     * {@code null} if no matching annotation is defined.
     *
     */
    @Nullable
    default Map<String, Object> getAnnotationAttributes(String annotationName,
            boolean classValuesAsString) {

        MergedAnnotation<Annotation> annotation = getAnnotations().get(annotationName,
                null, MergedAnnotationSelectors.firstDirectlyDeclared());
        if (!annotation.isPresent()) {
            return null;
        }
        return annotation.asAnnotationAttributes(Adapt.values(classValuesAsString, true));
    }

    /**
     * Retrieve all attributes of all annotations of the given type, if any (i.e. if
     * defined on the underlying element, as direct annotation or meta-annotation).
     * Note that this variant does <i>not</i> take attribute overrides into account.
     * @param annotationName the fully qualified class name of the annotation
     * type to look for
     * @return a MultiMap of attributes, with the attribute name as key (e.g. "value")
     * and a list of the defined attribute values as Map value. This return value will
     * be {@code null} if no matching annotation is defined.
     * @see #getAllAnnotationAttributes(String, boolean)
     */
    @Nullable
    default MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName) {
        return getAllAnnotationAttributes(annotationName, false);
    }

    /**
     * Retrieve all attributes of all annotations of the given type, if any (i.e. if
     * defined on the underlying element, as direct annotation or meta-annotation).
     * Note that this variant does <i>not</i> take attribute overrides into account.
     * @param annotationName the fully qualified class name of the annotation
     * type to look for
     * @param classValuesAsString  whether to convert class references to String
     * @return a MultiMap of attributes, with the attribute name as key (e.g. "value")
     * and a list of the defined attribute values as Map value. This return value will
     * be {@code null} if no matching annotation is defined.
     * @see #getAllAnnotationAttributes(String)
     */
    @Nullable
    default MultiValueMap<String, Object> getAllAnnotationAttributes(
            String annotationName, boolean classValuesAsString) {

        Adapt[] adaptations = Adapt.values(classValuesAsString, true);
        return getAnnotations().stream(annotationName)
                .filter(MergedAnnotationPredicates.unique(MergedAnnotation::getMetaTypes))
                .map(MergedAnnotation::withNonMergedAttributes)
                .collect(MergedAnnotationCollectors.toMultiValueMap(map ->
                        map.isEmpty() ? null : map, adaptations));
    }

}
