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
package org.springframework.conversation.manager;

import java.util.HashMap;
import java.util.Map;

import org.springframework.conversation.Conversation;

/**
 * An abstract extension for the {@link AbstractConversationStore} supporting
 * the fallback map using a thread local.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public abstract class AbstractThreadBackedConversationStore extends AbstractConversationStore {

	/** The fallback conversation map stored in a thread local. */
	private static final ThreadLocal<Map<String, Conversation>> FALLBACK_MAP = new ThreadLocal<Map<String, Conversation>>();

	/**
	 * @see org.springframework.conversation.manager.AbstractConversationStore#getConversationMapFallback()
	 */
	@Override
	public Map<String, Conversation> getConversationMapFallback() {
		Map<String, Conversation> map = FALLBACK_MAP.get();
		if (map == null) {
			map = new HashMap<String, Conversation>();
			FALLBACK_MAP.set(map);
		}

		return map;
	}
}
