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

import org.springframework.core.NamedThreadLocal;

/**
 * An implementation of the {@link ConversationResolver} where the currently
 * used conversation id is bound to the current thread. This implementation
 * should only be used in a batch environment, never in a web environment.
 * 
 * @author Micha Kiener
 * @author Agim Emruli
 * @since 3.1
 */
public class ThreadAttachedConversationResolver extends AbstractConversationResolver {
	/** The thread local attribute where the current conversation id is stored. */
	private static final NamedThreadLocal<String> CURRENT_CONVERSATION_ID = new NamedThreadLocal<String>("Current Conversation");

	/**
	 * @see org.springframework.conversation.manager.ConversationResolver#getCurrentConversationId()
	 */
	public String getCurrentConversationId() {
		return CURRENT_CONVERSATION_ID.get();
	}

	/**
	 * @see org.springframework.conversation.manager.ConversationResolver#setCurrentConversationId(java.lang.String)
	 */
	public void setCurrentConversationId(String conversationId) {
		CURRENT_CONVERSATION_ID.set(conversationId);
	}
}
