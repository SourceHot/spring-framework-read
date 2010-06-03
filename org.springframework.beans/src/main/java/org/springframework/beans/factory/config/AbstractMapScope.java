/*
 * Copyright 2002-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.beans.factory.config;

import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;

/**
 * A simple, abstract implementation of {@link Scope} based on a map holding the
 * state of the scope and also supporting destruction callbacks. It might be
 * used as the base for any other scope implementation or in tests to easily
 * register a scope based on a map implementation.
 *
 * @author Micha Kiener
 * @since 3.1
 */
public abstract class AbstractMapScope extends DestructionAwareAttributeMap
		implements Scope {

	/**
	 * @return a map based representation of the state of this scope
	 */
	public final Map<String, Object> getMap() {
		return super.getAttributeMap();
	}

	/**
	 * @see org.springframework.beans.factory.config.Scope#get(java.lang.String,
	 *      org.springframework.beans.factory.ObjectFactory)
	 */
	public Object get(String name, ObjectFactory<?> objectFactory) {
		Object scopedObject = super.getAttribute(name);
		if (scopedObject == null) {
			scopedObject = objectFactory.getObject();
			super.setAttribute(name, scopedObject);
		}

		return scopedObject;
	}

	/**
	 * @see org.springframework.beans.factory.config.Scope#remove(java.lang.String)
	 */
	public Object remove(String name) {
		return super.removeAttribute(name);
	}

	/**
	 * Closes this scope by removing any scoped object stored within it and
	 * invoking destruction callbacks accordingly.
	 */
	public void close() {
		super.clear();
	}
}
