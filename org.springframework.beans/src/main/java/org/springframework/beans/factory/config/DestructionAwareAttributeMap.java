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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A map implementation supporting destruction callbacks to be invoked if an
 * attribute is about to be removed from the map or if the map itself is cleared
 * out in its entirety. This implementation can be used as a base for a scope
 * implementation based on the {@link Scope} interface.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public class DestructionAwareAttributeMap implements Serializable {
	/** The map containing the registered attributes. */
	private final Map<String, Object> attributes;

	/**
	 * The optional map having any destruction callbacks registered using the
	 * name of the bean as the key.
	 */
	private Map<String, Runnable> registeredDescructionCallbacks;

	public DestructionAwareAttributeMap() {
		this.attributes = new HashMap<String, Object>();
	}

	/**
	 * Returns the map representation of the registered attributes directly. Be
	 * aware to synchronize any invocations to it on the map object itself to
	 * avoid concurrent modification exceptions.
	 * 
	 * @return the attributes as a map representation
	 */
	public Map<String, Object> getAttributeMap() {
		return attributes;
	}

	/**
	 * Returns the attribute having the specified name, if available,
	 * <code>null</code> otherwise.
	 * 
	 * @param name
	 *            the name of the attribute to be returned
	 * @return the attribute value or <code>null</code> if not available
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		synchronized (attributes) {
			return (T) attributes.get(name);
		}
	}

	/**
	 * Puts the given object with the specified name as an attribute to the
	 * underlying map.
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param value
	 *            the value to be stored
	 * @return any previously object stored under the same name, if any,
	 *         <code>null</code> otherwise
	 */
	@SuppressWarnings("unchecked")
	public <T> T setAttribute(String name, T value) {
		synchronized (attributes) {
			return (T) attributes.put(name, value);
		}
	}

	/**
	 * Remove the object with the given <code>name</code> from the underlying
	 * scope.
	 * <p>
	 * Returns <code>null</code> if no object was found; otherwise returns the
	 * removed <code>Object</code>.
	 * <p>
	 * Note that an implementation should also remove a registered destruction
	 * callback for the specified object, if any. It does, however, <i>not</i>
	 * need to <i>execute</i> a registered destruction callback in this case,
	 * since the object will be destroyed by the caller (if appropriate).
	 * <p>
	 * <b>Note: This is an optional operation.</b> Implementations may throw
	 * {@link UnsupportedOperationException} if they do not support explicitly
	 * removing an object.
	 * 
	 * @param name
	 *            the name of the object to remove
	 * @return the removed object, or <code>null</code> if no object was present
	 * @see #registerDestructionCallback
	 */
	@SuppressWarnings("unchecked")
	public <T> T removeAttribute(String name) {
		T value = null;
		synchronized (attributes) {
			value = (T) attributes.remove(name);
		}

		// check for a destruction callback to be invoked
		Runnable callback = getDesctructionCallback(name, true);
		if (callback != null) {
			callback.run();
		}

		return value;
	}

	/**
	 * Clears the map by removing all registered attribute values and invokes
	 * every destruction callback registered.
	 */
	public void clear() {
		synchronized (this) {
			// step through the attribute map and invoke destruction callbacks,
			// if any
			if (registeredDescructionCallbacks != null) {
				Iterator<Runnable> it = registeredDescructionCallbacks.values()
						.iterator();
				while (it.hasNext()) {
					it.next().run();
				}

				registeredDescructionCallbacks.clear();
			}
		}

		// clear out the registered attribute map
		attributes.clear();
	}

	/**
	 * Register a callback to be executed on destruction of the specified object
	 * in the scope (or at destruction of the entire scope, if the scope does
	 * not destroy individual objects but rather only terminates in its
	 * entirety).
	 * <p>
	 * <b>Note: This is an optional operation.</b> This method will only be
	 * called for scoped beans with actual destruction configuration
	 * (DisposableBean, destroy-method, DestructionAwareBeanPostProcessor).
	 * Implementations should do their best to execute a given callback at the
	 * appropriate time. If such a callback is not supported by the underlying
	 * runtime environment at all, the callback <i>must be ignored and a
	 * corresponding warning should be logged</i>.
	 * <p>
	 * Note that 'destruction' refers to to automatic destruction of the object
	 * as part of the scope's own lifecycle, not to the individual scoped object
	 * having been explicitly removed by the application. If a scoped object
	 * gets removed via this facade's {@link #removeAttribute(String)} method,
	 * any registered destruction callback should be removed as well, assuming
	 * that the removed object will be reused or manually destroyed.
	 * 
	 * @param name
	 *            the name of the object to execute the destruction callback for
	 * @param callback
	 *            the destruction callback to be executed. Note that the
	 *            passed-in Runnable will never throw an exception, so it can
	 *            safely be executed without an enclosing try-catch block.
	 *            Furthermore, the Runnable will usually be serializable,
	 *            provided that its target object is serializable as well.
	 * @see org.springframework.beans.factory.DisposableBean
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getDestroyMethodName()
	 * @see DestructionAwareBeanPostProcessor
	 */
	public void registerDestructionCallback(String name, Runnable callback) {
		synchronized (this) {
			if (registeredDescructionCallbacks == null) {
				registeredDescructionCallbacks = new HashMap<String, Runnable>();
			}

			registeredDescructionCallbacks.put(name, callback);
		}
	}

	/**
	 * Returns the destruction callback, if any registered for the attribute
	 * with the given name or <code>null</code> if no such callback was
	 * registered.
	 * 
	 * @param name
	 *            the name of the registered callback requested
	 * @param remove
	 *            <code>true</code>, if the callback should be removed after
	 *            this call, <code>false</code>, if it stays
	 * @return the callback, if found, <code>null</code> otherwise
	 */
	public Runnable getDesctructionCallback(String name, boolean remove) {
		synchronized (this) {
			if (registeredDescructionCallbacks == null) {
				return null;
			}

			if (remove) {
				return registeredDescructionCallbacks.remove(name);
			}
			return registeredDescructionCallbacks.get(name);
		}
	}
}
