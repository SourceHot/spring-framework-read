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
package org.springframework.web.conversation;

import java.util.HashMap;
import java.util.Map;

import org.springframework.conversation.Conversation;
import org.springframework.conversation.manager.AbstractThreadBackedConversationStore;
import org.springframework.conversation.manager.ConversationStore;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * A conversation store implementation ({@link ConversationStore}) based on a
 * web environment where the conversations are most likely to be stored in the
 * current session. It is based on the
 * {@link AbstractThreadBackedConversationStore} implementation in order to be
 * used outside a web thread as well.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public class WebBasedConversationStore extends
		AbstractThreadBackedConversationStore {
	/** The name of the attribute for the conversation map within the session. */
	public static final String CONVERSATION_STORE_ATTR_NAME = WebBasedConversationStore.class
			.getName();

	/**
	 * This implementation uses the {@link RequestContextHolder} to get access
	 * to the currently exposed session where the conversation map is stored
	 * within.
	 * 
	 * @see org.springframework.conversation.manager.AbstractConversationStore#getConversationMap()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Conversation> getConversationMap() {
		RequestAttributes attributes = RequestContextHolder
				.currentRequestAttributes();
		Map<String, Conversation> conversationMap = (Map<String, Conversation>) attributes
				.getAttribute(CONVERSATION_STORE_ATTR_NAME,
						RequestAttributes.SCOPE_GLOBAL_SESSION);

		if (conversationMap == null) {
			conversationMap = new HashMap<String, Conversation>();
			attributes.setAttribute(CONVERSATION_STORE_ATTR_NAME,
					conversationMap, RequestAttributes.SCOPE_GLOBAL_SESSION);
		}

		return conversationMap;
	}
}
