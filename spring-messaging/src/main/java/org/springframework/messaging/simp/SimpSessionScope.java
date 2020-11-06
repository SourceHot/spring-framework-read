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

package org.springframework.messaging.simp;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.lang.Nullable;

/**
 * A {@link Scope} implementation exposing the attributes of a SiMP session
 * (e.g. WebSocket session).
 *
 * <p>Relies on a thread-bound {@link SimpAttributes} instance exported by
 * {@link org.springframework.messaging.simp.annotation.support.SimpAnnotationMethodMessageHandler}.
 *
 * @author Rossen Stoyanchev
 * @since 4.1
 */
public class SimpSessionScope implements Scope {

	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		// 获取当前的属性
		SimpAttributes simpAttributes = SimpAttributesContextHolder.currentAttributes();
		// 从属性表中获取对象
		Object scopedObject = simpAttributes.getAttribute(name);
		if (scopedObject != null) {
			return scopedObject;
		}
		// 处理线程问题
		synchronized (simpAttributes.getSessionMutex()) {
			// 从属性表中获取对象
			scopedObject = simpAttributes.getAttribute(name);
			if (scopedObject == null) {
				// 通过 object factory 创建
				scopedObject = objectFactory.getObject();
				// 设置属性
				simpAttributes.setAttribute(name, scopedObject);
			}
			return scopedObject;
		}
	}

	@Override
	@Nullable
	public Object remove(String name) {
		SimpAttributes simpAttributes = SimpAttributesContextHolder.currentAttributes();
		synchronized (simpAttributes.getSessionMutex()) {
			Object value = simpAttributes.getAttribute(name);
			if (value != null) {
				simpAttributes.removeAttribute(name);
				return value;
			}
			else {
				return null;
			}
		}
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback) {
		SimpAttributesContextHolder.currentAttributes().registerDestructionCallback(name, callback);
	}

	@Override
	@Nullable
	public Object resolveContextualObject(String key) {
		return null;
	}

	@Override
	public String getConversationId() {
		return SimpAttributesContextHolder.currentAttributes().getSessionId();
	}

}
