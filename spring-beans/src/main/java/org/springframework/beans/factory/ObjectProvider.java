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

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A variant of {@link ObjectFactory} designed specifically for injection points,
 * allowing for programmatic optionality and lenient not-unique handling.
 *
 * <p>As of 5.1, this interface extends {@link Iterable} and provides {@link Stream}
 * support. It can be therefore be used in {@code for} loops, provides {@link #forEach}
 * iteration and allows for collection-style {@link #stream} access.
 *
 * @param <T> the object type
 * @author Juergen Hoeller
 * @see BeanFactory#getBeanProvider
 * @see org.springframework.beans.factory.annotation.Autowired
 * @since 4.3
 */
public interface ObjectProvider<T> extends ObjectFactory<T>, Iterable<T> {

    /**
     * Return an instance (possibly shared or independent) of the object
     * managed by this factory.
     * <p>Allows for specifying explicit construction arguments, along the
     * lines of {@link BeanFactory#getBean(String, Object...)}.
     *
     * @param args arguments to use when creating a corresponding instance
     * @return an instance of the bean
     * @throws BeansException in case of creation errors
     * @see #getObject()
     */
    T getObject(Object... args) throws BeansException;

    /**
     * Return an instance (possibly shared or independent) of the object
     * managed by this factory.
     *
     * @return an instance of the bean, or {@code null} if not available
     * @throws BeansException in case of creation errors
     * @see #getObject()
     */
    @Nullable
    T getIfAvailable() throws BeansException;

    /**
     * Return an instance (possibly shared or independent) of the object
     * managed by this factory.
     *
     * @param defaultSupplier a callback for supplying a default object
     *                        if none is present in the factory
     * @return an instance of the bean, or the supplied default object
     * if no such bean is available
     * @throws BeansException in case of creation errors
     * @see #getIfAvailable()
     * @since 5.0
     */
    default T getIfAvailable(Supplier<T> defaultSupplier) throws BeansException {
        T dependency = getIfAvailable();
        return (dependency != null ? dependency : defaultSupplier.get());
    }

    /**
     * Consume an instance (possibly shared or independent) of the object
     * managed by this factory, if available.
     *
     * @param dependencyConsumer a callback for processing the target object
     *                           if available (not called otherwise)
     * @throws BeansException in case of creation errors
     * @see #getIfAvailable()
     * @since 5.0
     */
    default void ifAvailable(Consumer<T> dependencyConsumer) throws BeansException {
        T dependency = getIfAvailable();
        if (dependency != null) {
            dependencyConsumer.accept(dependency);
        }
    }

    /**
     * Return an instance (possibly shared or independent) of the object
     * managed by this factory.
     *
     * @return an instance of the bean, or {@code null} if not available or
     * not unique (i.e. multiple candidates found with none marked as primary)
     * @throws BeansException in case of creation errors
     * @see #getObject()
     */
    @Nullable
    T getIfUnique() throws BeansException;

    /**
     * Return an instance (possibly shared or independent) of the object
     * managed by this factory.
     *
     * @param defaultSupplier a callback for supplying a default object
     *                        if no unique candidate is present in the factory
     * @return an instance of the bean, or the supplied default object
     * if no such bean is available or if it is not unique in the factory
     * (i.e. multiple candidates found with none marked as primary)
     * @throws BeansException in case of creation errors
     * @see #getIfUnique()
     * @since 5.0
     */
    default T getIfUnique(Supplier<T> defaultSupplier) throws BeansException {
        T dependency = getIfUnique();
        return (dependency != null ? dependency : defaultSupplier.get());
    }

    /**
     * Consume an instance (possibly shared or independent) of the object
     * managed by this factory, if unique.
     *
     * @param dependencyConsumer a callback for processing the target object
     *                           if unique (not called otherwise)
     * @throws BeansException in case of creation errors
     * @see #getIfAvailable()
     * @since 5.0
     */
    default void ifUnique(Consumer<T> dependencyConsumer) throws BeansException {
        T dependency = getIfUnique();
        if (dependency != null) {
            dependencyConsumer.accept(dependency);
        }
    }

    /**
     * Return an {@link Iterator} over all matching object instances,
     * without specific ordering guarantees (but typically in registration order).
     *
     * @see #stream()
     * @since 5.1
     */
    @Override
    default Iterator<T> iterator() {
        return stream().iterator();
    }

    /**
     * Return a sequential {@link Stream} over all matching object instances,
     * without specific ordering guarantees (but typically in registration order).
     *
     * @see #iterator()
     * @see #orderedStream()
     * @since 5.1
     */
    default Stream<T> stream() {
        throw new UnsupportedOperationException("Multi element access not supported");
    }

    /**
     * Return a sequential {@link Stream} over all matching object instances,
     * pre-ordered according to the factory's common order comparator.
     * <p>In a standard Spring application context, this will be ordered
     * according to {@link org.springframework.core.Ordered} conventions,
     * and in case of annotation-based configuration also considering the
     * {@link org.springframework.core.annotation.Order} annotation,
     * analogous to multi-element injection points of list/array type.
     *
     * @see #stream()
     * @see org.springframework.core.OrderComparator
     * @since 5.1
     */
    default Stream<T> orderedStream() {
        throw new UnsupportedOperationException("Ordered element access not supported");
    }

}
