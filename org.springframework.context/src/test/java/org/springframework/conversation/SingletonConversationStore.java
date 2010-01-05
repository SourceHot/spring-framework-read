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
package org.springframework.conversation;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.conversation.manager.AbstractConversationStore;

/**
 * @author Micha Kiener
 * @since 3.1
 */
public class SingletonConversationStore extends AbstractConversationStore {
	/** The map for the conversation storage. */
	private Map<String, Conversation> conversationMap;

	@PostConstruct
	public void initialize() {
		conversationMap = new HashMap<String, Conversation>();
	}

	/**
	 * @see org.springframework.conversation.manager.AbstractConversationStore#getConversationMap()
	 */
	@Override
	public Map<String, Conversation> getConversationMap() {
		return conversationMap;
	}

	/**
	 * @see org.springframework.conversation.manager.AbstractConversationStore#getConversationMapFallback()
	 */
	@Override
	public Map<String, Conversation> getConversationMapFallback() {
		return conversationMap;
	}

}
