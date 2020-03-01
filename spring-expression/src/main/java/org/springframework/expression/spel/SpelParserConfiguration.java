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

package org.springframework.expression.spel;

import org.springframework.core.SpringProperties;
import org.springframework.lang.Nullable;

/**
 * Configuration object for the SpEL expression parser.
 *
 * SpEL 配置类
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Andy Clement
 * @since 3.0
 * @see org.springframework.expression.spel.standard.SpelExpressionParser#SpelExpressionParser(SpelParserConfiguration)
 */
public class SpelParserConfiguration {


    private static final SpelCompilerMode defaultCompilerMode;

    static {
        // 读取 spring.expression.compiler.mode 中信息 ,该信息可放在spring.properties文件中
        String compilerMode = SpringProperties.getProperty("spring.expression.compiler.mode");

        defaultCompilerMode = (compilerMode != null ?
                SpelCompilerMode.valueOf(compilerMode.toUpperCase()) : SpelCompilerMode.OFF);
    }


    private final SpelCompilerMode compilerMode;

    @Nullable
    private final ClassLoader compilerClassLoader;

    // 遇到null,创建新对象用
    private final boolean autoGrowNullReferences;
    // 集合类型遇到null创建对象
    private final boolean autoGrowCollections;
    // 最大值
    private final int maximumAutoGrowSize;


    /**
     * Create a new {@code SpelParserConfiguration} instance with default settings.
     */
    public SpelParserConfiguration() {
        this(null, null, false, false, Integer.MAX_VALUE);
    }

    /**
     * Create a new {@code SpelParserConfiguration} instance.
     *
     * @param compilerMode        the compiler mode for the parser
     * @param compilerClassLoader the ClassLoader to use as the basis for expression compilation
     */
    public SpelParserConfiguration(@Nullable SpelCompilerMode compilerMode, @Nullable ClassLoader compilerClassLoader) {
        this(compilerMode, compilerClassLoader, false, false, Integer.MAX_VALUE);
    }

    /**
     * Create a new {@code SpelParserConfiguration} instance.
     *
     * @param autoGrowNullReferences if null references should automatically grow
     * @param autoGrowCollections    if collections should automatically grow
     * @see #SpelParserConfiguration(boolean, boolean, int)
     */
    public SpelParserConfiguration(boolean autoGrowNullReferences, boolean autoGrowCollections) {
        this(null, null, autoGrowNullReferences, autoGrowCollections, Integer.MAX_VALUE);
    }

    /**
     * Create a new {@code SpelParserConfiguration} instance.
     *
     * @param autoGrowNullReferences if null references should automatically grow
     * @param autoGrowCollections    if collections should automatically grow
     * @param maximumAutoGrowSize    the maximum size that the collection can auto grow
     */
    public SpelParserConfiguration(boolean autoGrowNullReferences, boolean autoGrowCollections, int maximumAutoGrowSize) {
        this(null, null, autoGrowNullReferences, autoGrowCollections, maximumAutoGrowSize);
    }

    /**
     * Create a new {@code SpelParserConfiguration} instance.
     *
     * @param compilerMode           the compiler mode that parsers using this configuration object should use
     * @param compilerClassLoader    the ClassLoader to use as the basis for expression compilation
     * @param autoGrowNullReferences if null references should automatically grow
     * @param autoGrowCollections    if collections should automatically grow
     * @param maximumAutoGrowSize    the maximum size that the collection can auto grow
     */
    public SpelParserConfiguration(@Nullable SpelCompilerMode compilerMode, @Nullable ClassLoader compilerClassLoader,
            boolean autoGrowNullReferences, boolean autoGrowCollections, int maximumAutoGrowSize) {

        this.compilerMode = (compilerMode != null ? compilerMode : defaultCompilerMode);
        this.compilerClassLoader = compilerClassLoader;
        this.autoGrowNullReferences = autoGrowNullReferences;
        this.autoGrowCollections = autoGrowCollections;
        this.maximumAutoGrowSize = maximumAutoGrowSize;
    }


    /**
     * Return the configuration mode for parsers using this configuration object.
     */
    public SpelCompilerMode getCompilerMode() {
        return this.compilerMode;
    }

    /**
     * Return the ClassLoader to use as the basis for expression compilation.
     */
    @Nullable
    public ClassLoader getCompilerClassLoader() {
        return this.compilerClassLoader;
    }

    /**
     * Return {@code true} if {@code null} references should be automatically grown.
     */
    public boolean isAutoGrowNullReferences() {
        return this.autoGrowNullReferences;
    }

    /**
     * Return {@code true} if collections should be automatically grown.
     */
    public boolean isAutoGrowCollections() {
        return this.autoGrowCollections;
    }

    /**
     * Return the maximum size that a collection can auto grow.
     */
    public int getMaximumAutoGrowSize() {
        return this.maximumAutoGrowSize;
    }

}
