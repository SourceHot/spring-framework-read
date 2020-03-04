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

package org.springframework.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple implementation of the {@link AliasRegistry} interface.
 * Serves as base class for
 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
 * implementations.
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 */
public class SimpleAliasRegistry implements AliasRegistry {

    /**
     * Logger available to subclasses.
     */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Map from alias to canonical name.
     * 存放别名的map(线程安全的),
     * 结构: alias-> name
     */
    private final Map<String, String> aliasMap = new ConcurrentHashMap<>(16);


    /**
     * {@code <alias name="appleBean" alias="hhh"/>}
     *
     * @param name  the canonical name
     *              alias 标签的name属性
     * @param alias the alias to be registered
     *              alias 标签的alias属性
     */
    @Override
    public void registerAlias(String name, String alias) {
        Assert.hasText(name, "'name' must not be empty");
        Assert.hasText(alias, "'alias' must not be empty");
        synchronized (this.aliasMap) {
            // 判断: 别名和名字是否相同
            if (alias.equals(name)) {
                //相同在别名map中移除
                this.aliasMap.remove(alias);
                if (logger.isDebugEnabled()) {
                    logger.debug("Alias definition '" + alias + "' ignored since it points to same name");
                }
            } else {
                // 不相同
                // 从map对象中获取别名为alias的value
                String registeredName = this.aliasMap.get(alias);
                if (registeredName != null) {
                    // 判断map中是否有有一个别名和传入的name相同的内容
                    if (registeredName.equals(name)) {
                        // An existing alias - no need to re-register
                        return;
                    }
                    if (!allowAliasOverriding()) {
                        throw new IllegalStateException("Cannot define alias '" + alias + "' for name '" +
                                name + "': It is already registered for name '" + registeredName + "'.");
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Overriding alias '" + alias + "' definition for registered name '" +
                                registeredName + "' with new target name '" + name + "'");
                    }
                }
                // 别名环检查
                // 第一条依赖:A-B,第二条依赖:A-C-B 抛出异常
                checkForAliasCircle(name, alias);
                // 放入 map 对象中 alias-> name
                this.aliasMap.put(alias, name);
                if (logger.isTraceEnabled()) {
                    logger.trace("Alias definition '" + alias + "' registered for name '" + name + "'");
                }
            }
        }
    }

    /**
     * Return whether alias overriding is allowed.
     * Default is {@code true}.
     * 是否允许重写别名
     */
    protected boolean allowAliasOverriding() {
        return true;
    }

    /**
     * Determine whether the given name has the given alias registered.
     * <p>
     * 递归判断是否已经存在别名
     *
     * @param name  the name to check
     * @param alias the alias to look for
     * @since 4.2.1
     */
    public boolean hasAlias(String name, String alias) {
        for (Map.Entry<String, String> entry : this.aliasMap.entrySet()) {
            // 获取key值
            String registeredName = entry.getValue();
            if (registeredName.equals(name)) {
                String registeredAlias = entry.getKey();
                // 循环引用判断
                if (registeredAlias.equals(alias) || hasAlias(registeredAlias, alias)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 别名移除
     *
     * @param alias the alias to remove
     */
    @Override
    public void removeAlias(String alias) {
        synchronized (this.aliasMap) {
            // 判断是否移除成功
            String name = this.aliasMap.remove(alias);
            if (name == null) {
                throw new IllegalStateException("No alias '" + alias + "' registered");
            }
        }
    }

    /**
     * 判断是否是一个别名,校验方式{@link org.springframework.core.SimpleAliasRegistry#aliasMap} 的key是否包含
     *
     * @param name the name to check
     * @return
     */
    @Override
    public boolean isAlias(String name) {
        return this.aliasMap.containsKey(name);
    }

    /**
     * 获取别名列表
     *
     * @param name the name to check for aliases
     * @return
     */
    @Override
    public String[] getAliases(String name) {
        List<String> result = new ArrayList<>();
        synchronized (this.aliasMap) {
            retrieveAliases(name, result);
        }
        return StringUtils.toStringArray(result);
    }

    /**
     * Transitively retrieve all aliases for the given name.
     * 根据 name 获取别名
     *
     * @param name   the target name to find aliases for
     * @param result the resulting aliases list
     */
    private void retrieveAliases(String name, List<String> result) {
        // 循环获取
        this.aliasMap.forEach((alias, registeredName) -> {
            if (registeredName.equals(name)) {
                result.add(alias);
                // 递归查询循环引用的别名
                retrieveAliases(alias, result);
            }
        });
    }

    /**
     * Resolve all alias target names and aliases registered in this
     * factory, applying the given StringValueResolver to them.
     * <p>The value resolver may for example resolve placeholders
     * in target bean names and even in alias names.
     *
     * @param valueResolver the StringValueResolver to apply
     */
    public void resolveAliases(StringValueResolver valueResolver) {
        Assert.notNull(valueResolver, "StringValueResolver must not be null");
        synchronized (this.aliasMap) {
            Map<String, String> aliasCopy = new HashMap<>(this.aliasMap);
            aliasCopy.forEach((alias, registeredName) -> {
                String resolvedAlias = valueResolver.resolveStringValue(alias);
                String resolvedName = valueResolver.resolveStringValue(registeredName);
                if (resolvedAlias == null || resolvedName == null || resolvedAlias.equals(resolvedName)) {
                    this.aliasMap.remove(alias);
                } else if (!resolvedAlias.equals(alias)) {
                    String existingName = this.aliasMap.get(resolvedAlias);
                    if (existingName != null) {
                        if (existingName.equals(resolvedName)) {
                            // Pointing to existing alias - just remove placeholder
                            this.aliasMap.remove(alias);
                            return;
                        }
                        throw new IllegalStateException(
                                "Cannot register resolved alias '" + resolvedAlias + "' (original: '" + alias +
                                        "') for name '" + resolvedName + "': It is already registered for name '" +
                                        registeredName + "'.");
                    }
                    checkForAliasCircle(resolvedName, resolvedAlias);
                    this.aliasMap.remove(alias);
                    this.aliasMap.put(resolvedAlias, resolvedName);
                } else if (!registeredName.equals(resolvedName)) {
                    this.aliasMap.put(alias, resolvedName);
                }
            });
        }
    }

    /**
     * Check whether the given name points back to the given alias as an alias
     * in the other direction already, catching a circular reference upfront
     * and throwing a corresponding IllegalStateException.
     * <p>
     * 判断是否循环别名
     *
     * @param name  the candidate name
     * @param alias the candidate alias
     * @see #registerAlias
     * @see #hasAlias
     */
    protected void checkForAliasCircle(String name, String alias) {
        if (hasAlias(alias, name)) {
            throw new IllegalStateException("Cannot register alias '" + alias +
                    "' for name '" + name + "': Circular reference - '" +
                    name + "' is a direct or indirect alias for '" + alias + "' already");
        }
    }

    /**
     * Determine the raw name, resolving aliases to canonical names.
     *
     * @param name the user-specified name
     * @return the transformed name
     */
    public String canonicalName(String name) {
        String canonicalName = name;
        // Handle aliasing...
        String resolvedName;
        do {
            resolvedName = this.aliasMap.get(canonicalName);
            if (resolvedName != null) {
                canonicalName = resolvedName;
            }
        }
        while (resolvedName != null);
        return canonicalName;
    }

}
