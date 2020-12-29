/*
 * Copyright 2002-2017 the original author or authors.
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

import org.springframework.lang.Nullable;

/**
 * Interface that defines abstract metadata of a specific class,
 * in a form that does not require that class to be loaded yet.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see StandardClassMetadata
 * @see org.springframework.core.type.classreading.MetadataReader#getClassMetadata()
 * @see AnnotationMetadata
 */
public interface ClassMetadata {

    /**
     * Return the name of the underlying class.
     * 类名
     */
    String getClassName();

    /**
     * Return whether the underlying class represents an interface.
     * 是否是接口
     */
    boolean isInterface();

    /**
     * Return whether the underlying class represents an annotation.
     * 是否是注解
     * @since 4.1
     */
    boolean isAnnotation();

    /**
     * Return whether the underlying class is marked as abstract.
     * 是否是超类
     */
    boolean isAbstract();

    /**
     * Return whether the underlying class represents a concrete class,
     * i.e. neither an interface nor an abstract class.
     * 是否允许创建,实例化
     */
    default boolean isConcrete() {
        return !(isInterface() || isAbstract());
    }

    /**
     * Return whether the underlying class is marked as 'final'.
     * 是否有final修饰
     */
    boolean isFinal();

    /**
     * Determine whether the underlying class is independent, i.e. whether
     * it is a top-level class or a nested class (static inner class) that
     * can be constructed independently from an enclosing class.
     *
     * 是否独立
     * 1. 不是内部类
     * 2. 不是继承类
     */
    boolean isIndependent();

    /**
     * Return whether the underlying class is declared within an enclosing
     * class (i.e. the underlying class is an inner/nested class or a
     * local class within a method).
     * <p>If this method returns {@code false}, then the underlying
     * class is a top-level class.
     *
     * 是否有内部类
     */
    default boolean hasEnclosingClass() {
        return (getEnclosingClassName() != null);
    }

    /**
     * Return the name of the enclosing class of the underlying class,
     * or {@code null} if the underlying class is a top-level class.
     * 获取内部类名称
     */
    @Nullable
    String getEnclosingClassName();

    /**
     * Return whether the underlying class has a super class.
     *
     * 是否有父类
     */
    default boolean hasSuperClass() {
        return (getSuperClassName() != null);
    }

    /**
     * Return the name of the super class of the underlying class,
     * or {@code null} if there is no super class defined.
     * 父类名称
     */
    @Nullable
    String getSuperClassName();

    /**
     * Return the names of all interfaces that the underlying class
     * implements, or an empty array if there are none.
     *
     * 实现接口列表
     */
    String[] getInterfaceNames();

    /**
     * Return the names of all classes declared as members of the class represented by
     * this ClassMetadata object. This includes public, protected, default (package)
     * access, and private classes and interfaces declared by the class, but excludes
     * inherited classes and interfaces. An empty array is returned if no member classes
     * or interfaces exist.
     *
     * 成员列表
     * @since 3.1
     */
    String[] getMemberClassNames();

}
