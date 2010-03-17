/*
 * Copyright 2002-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.web.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.filter.FlashMapFilter;

/**
 * The Redirect-After-Post pattern is a solution for many problems and the flash map utility is able to store state
 * which persists to the next request. For instance, if you're having an error in the login handler, you could actually
 * store messages or any other state within the flash map and then redirect to an appropriate view where the state would
 * be still available. The content of the flash map is actually added as request parameters in the next request and then
 * being flushed out which is done by the {@link FlashMapFilter}.
 * <p>
 * If window management is activated, the flash map is stored within window scope to avoid concurrent tabs / windows
 * affecting each other, it falls back to the session, if window management is not activated.
 * <p>
 * Typically, you will use the flash map by manually adding parameters to it before redirecting which could look like:
 * 
 * <pre>
 * FlashMap.put(&quot;paramName&quot;, paramValue);
 * redirect(...);
 * </pre>
 * 
 * @author Micha Kiener
 * @since 3.1
 * 
 * @see FlashMapFilter
 */
public class FlashMap {
	public static final String FLASH_MAP_ATTRIBUTE = FlashMap.class.getName();

	/**
	 * Stores the given name / value pair in the flash map to become available as a request parameter in the next
	 * request using the same name.
	 * 
	 * @param <T> the type of the parameter value to put in the flash map
	 * @param parameterName the name of the parameter under which the value will become available in the next request
	 * @param parameterValue the parameter value to be stored in flash
	 * @return any old value already in the flash map, if any, <code>null</code> otherwise
	 */
	@SuppressWarnings("unchecked")
	public static <T> T put(String parameterName, T parameterValue) {
		Map<String, Object> flashMap = getFlashMap(true);
		return (T) flashMap.put(parameterName, parameterValue);
	}

	/**
	 * Returns the flash map typically stored in window or session scope and creates a new one, if not existing and if
	 * needed.
	 * 
	 * @param createIfNotExisting flag, indicating whether to create the flash map, if it does not exist already
	 * @return the flash map currently bound to the session or window scope, if available
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getFlashMap(boolean createIfNotExisting) {
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		Map<String, Object> flashMap = (Map<String, Object>) requestAttributes.getAttribute(FLASH_MAP_ATTRIBUTE,
				getFlashMapScope());

		if (flashMap == null && createIfNotExisting) {
			flashMap = new HashMap<String, Object>();
			requestAttributes.setAttribute(FLASH_MAP_ATTRIBUTE, flashMap, getFlashMapScope());
		}

		return flashMap;
	}

	/**
	 * Removes the flash map from the window or session scope.
	 */
	public static void removeFlashMap() {
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		requestAttributes.removeAttribute(FLASH_MAP_ATTRIBUTE, getFlashMapScope());
	}

	/**
	 * @return the scope to store the flash map in which is window scope by default, if window management is activated,
	 * session otherwise
	 */
	protected static int getFlashMapScope() {
		// TODO: check for window management and return value accordingly
		return RequestAttributes.SCOPE_SESSION;
	}

	/**
	 * Private constructor as the map is only to be used using its static accessor methods.
	 */
	private FlashMap() {
	}
}
