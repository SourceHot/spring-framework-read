/*
 * Copyright 2002-2018 the original author or authors.
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

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * General utility for determining the order of an object based on its type declaration.
 * Handles Spring's {@link Order} annotation as well as {@link javax.annotation.Priority}.
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @see Order
 * @see javax.annotation.Priority
 * @since 4.1
 */
@SuppressWarnings("unchecked")
public abstract class OrderUtils {

    /**
     * Cache marker for a non-annotated Class.
     */
    private static final Object NOT_ANNOTATED = new Object();
    /**
     * Cache for @Order value (or NOT_ANNOTATED marker) per Class.
     * key: 类名,value: intValue
     */
    private static final Map<Class<?>, Object> orderCache = new ConcurrentReferenceHashMap<>(64);
    /**
     * Cache for @Priority value (or NOT_ANNOTATED marker) per Class.
     * key: 类名,value: intValue
     */
    private static final Map<Class<?>, Object> priorityCache = new ConcurrentReferenceHashMap<>();
    /**
     * clazz
     */
    @Nullable
    private static Class<? extends Annotation> priorityAnnotationType;

    static {
        try {
            priorityAnnotationType = (Class<? extends Annotation>)
                    ClassUtils.forName("javax.annotation.Priority", OrderUtils.class.getClassLoader());
        } catch (Throwable ex) {
            // javax.annotation.Priority not available
            priorityAnnotationType = null;
        }
    }

    /**
     * Return the order on the specified {@code type}, or the specified
     * default value if none can be found.
     * <p>Takes care of {@link Order @Order} and {@code @javax.annotation.Priority}.
     *
     * @param type the type to handle
     * @return the priority value, or the specified default order if none can be found
     * @see #getPriority(Class)
     * @since 5.0
     */
    public static int getOrder(Class<?> type, int defaultOrder) {
        Integer order = getOrder(type);
        return (order != null ? order : defaultOrder);
    }

    /**
     * Return the order on the specified {@code type}, or the specified
     * default value if none can be found.
     * <p>Takes care of {@link Order @Order} and {@code @javax.annotation.Priority}.
     * <p>
     * <p>
     * 获取 Order 的value值
     *
     * @param type the type to handle
     *             带有{@link Order}的类
     * @return the priority value, or the specified default order if none can be found
     * {@link Order}的value
     * @see #getPriority(Class)
     */
    @Nullable
    public static Integer getOrder(Class<?> type, @Nullable Integer defaultOrder) {
        Integer order = getOrder(type);
        // 如果不存在则返回默认值,Integer.MAX_VALUE
        return (order != null ? order : defaultOrder);
    }

    /**
     * Return the order on the specified {@code type}.
     * <p>Takes care of {@link Order @Order} and {@code @javax.annotation.Priority}.
     *
     * @param type the type to handle
     * @return the order value, or {@code null} if none can be found
     * @see #getPriority(Class)
     */
    @Nullable
    public static Integer getOrder(Class<?> type) {
        // 缓存中获取
        Object cached = orderCache.get(type);
        if (cached != null) {
            // 返回 int
            return (cached instanceof Integer ? (Integer) cached : null);
        }
        /**
         * 注解工具类,寻找{@link Order}注解
         */
        Order order = AnnotationUtils.findAnnotation(type, Order.class);
        Integer result;
        if (order != null) {
            // 返回
            result = order.value();
        } else {
            result = getPriority(type);
        }
        // key: 类名,value: intValue
        orderCache.put(type, (result != null ? result : NOT_ANNOTATED));
        return result;
    }

    /**
     * Return the value of the {@code javax.annotation.Priority} annotation
     * declared on the specified type, or {@code null} if none.
     *
     * @param type the type to handle
     * @return the priority value if the annotation is declared, or {@code null} if none
     */
    @Nullable
    public static Integer getPriority(Class<?> type) {
        if (priorityAnnotationType == null) {
            return null;
        }
        // 缓存中获取
        Object cached = priorityCache.get(type);
        if (cached != null) {
            // 不为空返回
            return (cached instanceof Integer ? (Integer) cached : null);
        }
        // 注解工具获取注解
        Annotation priority = AnnotationUtils.findAnnotation(type, priorityAnnotationType);
        Integer result = null;
        if (priority != null) {
            // 获取 value
            result = (Integer) AnnotationUtils.getValue(priority);
        }
        // 向缓存插入数据
        priorityCache.put(type, (result != null ? result : NOT_ANNOTATED));
        return result;
    }

}
