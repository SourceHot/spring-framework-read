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

package org.springframework.test.context.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.style.ToStringCreator;
import org.springframework.lang.Nullable;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.util.Assert;

/**
 * Default implementation of the {@link ContextCache} API.
 *
 * <p>Uses a synchronized {@link Map} configured with a maximum size
 * and a <em>least recently used</em> (LRU) eviction policy to cache
 * {@link ApplicationContext} instances.
 *
 * <p>The maximum size may be supplied as a {@linkplain #DefaultContextCache(int)
 * constructor argument} or set via a system property or Spring property named
 * {@code spring.test.context.cache.maxSize}.
 *
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @since 2.5
 * @see ContextCacheUtils#retrieveMaxCacheSize()
 */
public class DefaultContextCache implements ContextCache {

	private static final Log statsLogger = LogFactory.getLog(CONTEXT_CACHE_LOGGING_CATEGORY);

	/**
	 * Map of context keys to Spring {@code ApplicationContext} instances.
	 * 上下文map容器
	 */
	private final Map<MergedContextConfiguration, ApplicationContext> contextMap =
			Collections.synchronizedMap(new LruCache(32, 0.75f));

	/**
	 * Map of parent keys to sets of children keys, representing a top-down <em>tree</em>
	 * of context hierarchies. This information is used for determining which subtrees
	 * need to be recursively removed and closed when removing a context that is a parent
	 * of other contexts.
	 * 上下文层次结构
	 */
	private final Map<MergedContextConfiguration, Set<MergedContextConfiguration>> hierarchyMap =
			new ConcurrentHashMap<>(32);

	private final int maxSize;
	/**
	 * 命中数量
	 */
	private final AtomicInteger hitCount = new AtomicInteger();
	/**
	 * 未命中数量
	 */
	private final AtomicInteger missCount = new AtomicInteger();


	/**
	 * Create a new {@code DefaultContextCache} using the maximum cache size
	 * obtained via {@link ContextCacheUtils#retrieveMaxCacheSize()}.
	 * @since 4.3
	 * @see #DefaultContextCache(int)
	 * @see ContextCacheUtils#retrieveMaxCacheSize()
	 */
	public DefaultContextCache() {
		this(ContextCacheUtils.retrieveMaxCacheSize());
	}

	/**
	 * Create a new {@code DefaultContextCache} using the supplied maximum
	 * cache size.
	 * @param maxSize the maximum cache size
	 * @throws IllegalArgumentException if the supplied {@code maxSize} value
	 * is not positive
	 * @since 4.3
	 * @see #DefaultContextCache()
	 */
	public DefaultContextCache(int maxSize) {
		Assert.isTrue(maxSize > 0, "'maxSize' must be positive");
		this.maxSize = maxSize;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(MergedContextConfiguration key) {
		Assert.notNull(key, "Key must not be null");
		return this.contextMap.containsKey(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Nullable
	public ApplicationContext get(MergedContextConfiguration key) {
		Assert.notNull(key, "Key must not be null");
		ApplicationContext context = this.contextMap.get(key);
		if (context == null) {
			this.missCount.incrementAndGet();
		}
		else {
			this.hitCount.incrementAndGet();
		}
		return context;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(MergedContextConfiguration key, ApplicationContext context) {
		Assert.notNull(key, "Key must not be null");
		Assert.notNull(context, "ApplicationContext must not be null");

		// 设置到上下文容器中
		this.contextMap.put(key, context);
		// 获取子对象
		MergedContextConfiguration child = key;
		// 获取父对象
		MergedContextConfiguration parent = child.getParent();
		// 递归回去父对象为成员变量hierarchyMap进行数据设置
		while (parent != null) {
			Set<MergedContextConfiguration> list = this.hierarchyMap.computeIfAbsent(parent, k -> new HashSet<>());
			list.add(child);
			child = parent;
			parent = child.getParent();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(MergedContextConfiguration key, @Nullable HierarchyMode hierarchyMode) {
		Assert.notNull(key, "Key must not be null");

		// startKey is the level at which to begin clearing the cache,
		// depending on the configured hierarchy mode.s

		// 确认开始清除的key
		MergedContextConfiguration startKey = key;
		// 如果清除模式是EXHAUSTIVE,会搜索到最顶层的合并上下文配置
		if (hierarchyMode == HierarchyMode.EXHAUSTIVE) {
			MergedContextConfiguration parent = startKey.getParent();
			while (parent != null) {
				startKey = parent;
				parent = startKey.getParent();
			}
		}

		// 需要移除的上下文配置集合
		List<MergedContextConfiguration> removedContexts = new ArrayList<>();
		// 移除
		remove(removedContexts, startKey);

		// Remove all remaining references to any removed contexts from the
		// hierarchy map.
		// 删除hierarchyMap变量中的引用
		for (MergedContextConfiguration currentKey : removedContexts) {
			for (Set<MergedContextConfiguration> children : this.hierarchyMap.values()) {
				children.remove(currentKey);
			}
		}

		// Remove empty entries from the hierarchy map.
		// 删除空数据
		for (Map.Entry<MergedContextConfiguration, Set<MergedContextConfiguration>> entry : this.hierarchyMap.entrySet()) {
			if (entry.getValue().isEmpty()) {
				this.hierarchyMap.remove(entry.getKey());
			}
		}
	}

	private void remove(List<MergedContextConfiguration> removedContexts, MergedContextConfiguration key) {
		Assert.notNull(key, "Key must not be null");

		// 从hierarchyMap成员变量中获取子集
		Set<MergedContextConfiguration> children = this.hierarchyMap.get(key);
		// 子集不为空的情况下处理
		if (children != null) {
			for (MergedContextConfiguration child : children) {
				// Recurse through lower levels
				remove(removedContexts, child);
			}
			// Remove the set of children for the current context from the hierarchy map.
			this.hierarchyMap.remove(key);
		}

		// Physically remove and close leaf nodes first (i.e., on the way back up the
		// stack as opposed to prior to the recursive call).
		// 从上下文
		ApplicationContext context = this.contextMap.remove(key);
		// 上下文类型是ConfigurableApplicationContext的情况下关闭上下文
		if (context instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) context).close();
		}
		// 加入移除集合中
		removedContexts.add(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return this.contextMap.size();
	}

	/**
	 * Get the maximum size of this cache.
	 */
	public int getMaxSize() {
		return this.maxSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getParentContextCount() {
		return this.hierarchyMap.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getHitCount() {
		return this.hitCount.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMissCount() {
		return this.missCount.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset() {
		synchronized (this.contextMap) {
			clear();
			clearStatistics();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		synchronized (this.contextMap) {
			this.contextMap.clear();
			this.hierarchyMap.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearStatistics() {
		synchronized (this.contextMap) {
			this.hitCount.set(0);
			this.missCount.set(0);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void logStatistics() {
		if (statsLogger.isDebugEnabled()) {
			statsLogger.debug("Spring test ApplicationContext cache statistics: " + this);
		}
	}

	/**
	 * Generate a text string containing the implementation type of this
	 * cache and its statistics.
	 * <p>The string returned by this method contains all information
	 * required for compliance with the contract for {@link #logStatistics()}.
	 * @return a string representation of this cache, including statistics
	 */
	@Override
	public String toString() {
		return new ToStringCreator(this)
				.append("size", size())
				.append("maxSize", getMaxSize())
				.append("parentContextCount", getParentContextCount())
				.append("hitCount", getHitCount())
				.append("missCount", getMissCount())
				.toString();
	}


	/**
	 * Simple cache implementation based on {@link LinkedHashMap} with a maximum
	 * size and a <em>least recently used</em> (LRU) eviction policy that
	 * properly closes application contexts.
	 * @since 4.3
	 */
	@SuppressWarnings("serial")
	private class LruCache extends LinkedHashMap<MergedContextConfiguration, ApplicationContext> {

		/**
		 * Create a new {@code LruCache} with the supplied initial capacity
		 * and load factor.
		 * @param initialCapacity the initial capacity
		 * @param loadFactor the load factor
		 */
		LruCache(int initialCapacity, float loadFactor) {
			super(initialCapacity, loadFactor, true);
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<MergedContextConfiguration, ApplicationContext> eldest) {
			if (this.size() > DefaultContextCache.this.getMaxSize()) {
				// Do NOT delete "DefaultContextCache.this."; otherwise, we accidentally
				// invoke java.util.Map.remove(Object, Object).
				DefaultContextCache.this.remove(eldest.getKey(), HierarchyMode.CURRENT_LEVEL);
			}

			// Return false since we invoke a custom eviction algorithm.
			return false;
		}
	}

}
