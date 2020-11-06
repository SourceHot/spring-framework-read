/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.context.testfixture;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class SimpleMapScope implements Scope, Serializable {

	/**
	 * 存储对象容器
	 * key: name
	 * value: object instance
	 */
	private final Map<String, Object> map = new HashMap<>();

	/**
	 * 存储回调方法
	 */
	private final List<Runnable> callbacks = new LinkedList<>();


	public SimpleMapScope() {
	}

	public final Map<String, Object> getMap() {
		return this.map;
	}


	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		synchronized (this.map) {
			// 从 容器中获取 .
			Object scopedObject = this.map.get(name);
			// 不存在从 ObjectFactory 中获取
			if (scopedObject == null) {
				scopedObject = objectFactory.getObject();
				// 添加到容器
				this.map.put(name, scopedObject);
			}
			return scopedObject;
		}
	}

	@Override
	public Object remove(String name) {
		synchronized (this.map) {
			return this.map.remove(name);
		}
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback) {
		this.callbacks.add(callback);
	}

	@Override
	public Object resolveContextualObject(String key) {
		return null;
	}

	public void close() {
		for (Iterator<Runnable> it = this.callbacks.iterator(); it.hasNext();) {
			Runnable runnable = it.next();
			runnable.run();
		}
	}

	@Override
	public String getConversationId() {
		return null;
	}

}
