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

package org.springframework.context.annotation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.Conventions;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utilities for identifying {@link Configuration} classes.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 */
abstract class ConfigurationClassUtils {

    private static final String CONFIGURATION_CLASS_FULL = "full";

    private static final String CONFIGURATION_CLASS_LITE = "lite";

    private static final String CONFIGURATION_CLASS_ATTRIBUTE =
            Conventions.getQualifiedAttributeName(ConfigurationClassPostProcessor.class, "configurationClass");

    private static final String ORDER_ATTRIBUTE =
            Conventions.getQualifiedAttributeName(ConfigurationClassPostProcessor.class, "order");


    private static final Log logger = LogFactory.getLog(ConfigurationClassUtils.class);

    /**
     * 基础注解列表
     */
    private static final Set<String> candidateIndicators = new HashSet<>(8);

    static {
        candidateIndicators.add(Component.class.getName());
        candidateIndicators.add(ComponentScan.class.getName());
        candidateIndicators.add(Import.class.getName());
        candidateIndicators.add(ImportResource.class.getName());
    }


    /**
     * Check whether the given bean definition is a candidate for a configuration class
     * (or a nested component class declared within a configuration/component class,
     * to be auto-registered as well), and mark it accordingly.
     * <p>
     * <p>
     * 判断是否是{@link Configuration} 类
     *
     * @param beanDef               the bean definition to check
     * @param metadataReaderFactory the current factory in use by the caller
     * @return whether the candidate qualifies as (any kind of) configuration class
     */
    public static boolean checkConfigurationClassCandidate(
            BeanDefinition beanDef, MetadataReaderFactory metadataReaderFactory) {

        // 获取类名
        String className = beanDef.getBeanClassName();
        // 类名不存在或者没有工厂方法
        if (className == null || beanDef.getFactoryMethodName() != null) {
            return false;
        }

        // 注解信息获取
        AnnotationMetadata metadata;
        /**
         * 1. 类型判断是否为{@link AnnotatedBeanDefinition}
         * 2. className 是否相同
         */
        if (beanDef instanceof AnnotatedBeanDefinition &&
                className.equals(((AnnotatedBeanDefinition) beanDef).getMetadata().getClassName())) {
            // Can reuse the pre-parsed metadata from the given BeanDefinition...
            // 获取注解信息
            metadata = ((AnnotatedBeanDefinition) beanDef).getMetadata();
        }
        /**
         * 1. 类型判断是否为{@link AbstractBeanDefinition}
         * 2. 是否有beanClass
         */
        else if (beanDef instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) beanDef).hasBeanClass()) {
            // Check already loaded Class if present...
            // since we possibly can't even load the class file for this Class.
            // 获取beanClass
            Class<?> beanClass = ((AbstractBeanDefinition) beanDef).getBeanClass();
            // 注解获取信息获取
            metadata = new StandardAnnotationMetadata(beanClass, true);
        } else {
            try {
                // 通过metadataReaderFactory获取
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(className);
                metadata = metadataReader.getAnnotationMetadata();
            } catch (IOException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not find class file for introspecting configuration annotations: " +
                            className, ex);
                }
                return false;
            }
        }

        // 如果存在Configuration 注解,则为BeanDefinition 设置configurationClass属性为full
        if (isFullConfigurationCandidate(metadata)) {
            beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_FULL);
        }
        // 如果AnnotationMetadata 中有Component,ComponentScan,Import,ImportResource 注解中的任意一个,或者存在 被@bean 注解的方法,则返回true.
        else if (isLiteConfigurationCandidate(metadata)) {
            beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_LITE);
        } else {
            return false;
        }

        // It's a full or lite configuration candidate... Let's determine the order value, if any.
        // 获取Order信息
        Integer order = getOrder(metadata);
        if (order != null) {
            // 设置order信息
            beanDef.setAttribute(ORDER_ATTRIBUTE, order);
        }

        return true;
    }

    /**
     * Check the given metadata for a configuration class candidate
     * (or nested component class declared within a configuration/component class).
     *
     * @param metadata the metadata of the annotated class
     * @return {@code true} if the given class is to be registered as a
     * reflection-detected bean definition; {@code false} otherwise
     */
    public static boolean isConfigurationCandidate(AnnotationMetadata metadata) {
        return (isFullConfigurationCandidate(metadata) || isLiteConfigurationCandidate(metadata));
    }

    /**
     * Check the given metadata for a full configuration class candidate
     * (i.e. a class annotated with {@code @Configuration}).
     *
     * @param metadata the metadata of the annotated class
     * @return {@code true} if the given class is to be processed as a full
     * configuration class, including cross-method call interception
     */
    public static boolean isFullConfigurationCandidate(AnnotationMetadata metadata) {
        return metadata.isAnnotated(Configuration.class.getName());
    }

    /**
     * Check the given metadata for a lite configuration class candidate
     * (e.g. a class annotated with {@code @Component} or just having
     * {@code @Import} declarations or {@code @Bean methods}).
     * <p>
     * 如果AnnotationMetadata 中有Component,ComponentScan,Import,ImportResource 注解中的任意一个,或者存在 被@bean 注解的方法,则返回true.
     *
     * @param metadata the metadata of the annotated class
     * @return {@code true} if the given class is to be processed as a lite
     * configuration class, just registering it and scanning it for {@code @Bean} methods
     */
    public static boolean isLiteConfigurationCandidate(AnnotationMetadata metadata) {
        // Do not consider an interface or an annotation...
        if (metadata.isInterface()) {
            return false;
        }

        // Any of the typical annotations found?
        for (String indicator : candidateIndicators) {
            if (metadata.isAnnotated(indicator)) {
                return true;
            }
        }

        // Finally, let's look for @Bean methods...
        try {
            // 是否有bean注解
            return metadata.hasAnnotatedMethods(Bean.class.getName());
        } catch (Throwable ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to introspect @Bean methods on class [" + metadata.getClassName() + "]: " + ex);
            }
            return false;
        }
    }

    /**
     * Determine whether the given bean definition indicates a full {@code @Configuration}
     * class, through checking {@link #checkConfigurationClassCandidate}'s metadata marker.
     * <p>
     * 判断是 configuration
     */
    public static boolean isFullConfigurationClass(BeanDefinition beanDef) {
        return CONFIGURATION_CLASS_FULL.equals(beanDef.getAttribute(CONFIGURATION_CLASS_ATTRIBUTE));
    }

    /**
     * Determine whether the given bean definition indicates a lite {@code @Configuration}
     * class, through checking {@link #checkConfigurationClassCandidate}'s metadata marker.
     * <p>
     * 判断是否带有{@link  Configuration}
     */
    public static boolean isLiteConfigurationClass(BeanDefinition beanDef) {
        return CONFIGURATION_CLASS_LITE.equals(beanDef.getAttribute(CONFIGURATION_CLASS_ATTRIBUTE));
    }

    /**
     * Determine the order for the given configuration class metadata.
     *
     * @param metadata the metadata of the annotated class
     * @return the {@code @Order} annotation value on the configuration class,
     * or {@code Ordered.LOWEST_PRECEDENCE} if none declared
     * @since 5.0
     */
    @Nullable
    public static Integer getOrder(AnnotationMetadata metadata) {
        Map<String, Object> orderAttributes = metadata.getAnnotationAttributes(Order.class.getName());
        return (orderAttributes != null ? ((Integer) orderAttributes.get(AnnotationUtils.VALUE)) : null);
    }

    /**
     * Determine the order for the given configuration class bean definition,
     * as set by {@link #checkConfigurationClassCandidate}.
     *
     * @param beanDef the bean definition to check
     * @return the {@link Order @Order} annotation value on the configuration class,
     * or {@link Ordered#LOWEST_PRECEDENCE} if none declared
     * @since 4.2
     */
    public static int getOrder(BeanDefinition beanDef) {
        Integer order = (Integer) beanDef.getAttribute(ORDER_ATTRIBUTE);
        return (order != null ? order : Ordered.LOWEST_PRECEDENCE);
    }

}
