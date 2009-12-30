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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.conversation.Conversation;

/**
 * An abstract base implementation for the {@link ConversationStore} interface
 * used by the {@link ConversationManager} to register conversation objects
 * within. This implementation supports the basic mechanism and exposes two
 * methods to be implemented by concrete stores to provide the conversation map
 * and optionally a fallback map used, if the default one is not reachable. This
 * could be the case, if the default one is session scoped and the conversation
 * management is used from within a non web thread so the fallback map could be
 * a thread attached map using a {@link ThreadLocal} for instance.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public abstract class AbstractConversationStore implements ConversationStore {
	/**
	 * Flag indicating whether to use the fallback conversation map, if the
	 * default one is not reachable, see {@link #getConversationMapSafe()} for
	 * more information. Default is <code>true</code>.
	 */
	private boolean useFallbackMap;

	/**
	 * Default constructor, turning the fallback map on by default.
	 */
	public AbstractConversationStore() {
		useFallbackMap = true;
	}

	/**
	 * @see org.springframework.conversation.manager.ConversationStore#getConversation(java.lang.String)
	 */
	public Conversation getConversation(String id) {
		Map<String, Conversation> map = getConversationMapSafe();
		return map.get(id);
	}

	/**
	 * @see org.springframework.conversation.manager.ConversationStore#getConversations()
	 */
	public List<Conversation> getConversations() {
		Map<String, Conversation> map = getConversationMapSafe();
		return new ArrayList<Conversation>(map.values());
	}

	/**
	 * @see org.springframework.conversation.manager.ConversationStore#registerConversation(org.springframework.conversation.Conversation)
	 */
	public void registerConversation(Conversation conversation) {
		Map<String, Conversation> map = getConversationMapSafe();
		map.put(conversation.getId(), conversation);
	}

	/**
	 * @see org.springframework.conversation.manager.ConversationStore#removeConversation(java.lang.String)
	 */
	public Conversation removeConversation(String id) {
		Map<String, Conversation> map = getConversationMapSafe();
		return map.remove(id);
	}

	/**
	 * @see org.springframework.conversation.manager.ConversationStore#size()
	 */
	public int size() {
		return getConversationMapSafe().size();
	}

	/**
	 * Internal method returning the conversation map in a safe way as the
	 * method {@link #getConversationMap()} could actually raise an exception,
	 * if the method is injected and the map is currently not available (for
	 * instance if the map is in session scope and this method is invoked from a
	 * non web thread). If the fallback mechanism is turned on (as done by
	 * default), in this case the fallback map as being returned by
	 * {@link #getConversationMapFallback()} is returned in order to use the
	 * conversation management in a non web thread as well (for instance for
	 * asynchronous tasks or batch stuff).
	 * 
	 * @return the conversation map to be used currently
	 */
	protected Map<String, Conversation> getConversationMapSafe() {
		try {
			return getConversationMap();
		} catch (Exception e) {
			// if the map is not reachable and fallback is on, return the
			// fallback map
			if (isUseFallbackMap()) {
				return getConversationMapFallback();
			}
		}

		throw new IllegalAccessError("The conversation map to store conversations in is currently not available.");
	}

	/**
	 * Returns the map used to store the conversations in using their id as the
	 * key. This method can either be injected by providing a map implementation
	 * in session or window scope or might be implemented.
	 * 
	 * @return the map for storing the conversations
	 */
	public abstract Map<String, Conversation> getConversationMap();

	/**
	 * Returns the conversation map used if the default one returned by
	 * {@link #getConversationMap()} is not available. This could be the case,
	 * if the default one is session based and the conversation management is
	 * used from within a non web thread. This method must always be able to
	 * provide a map, if the conversation management should be available in any
	 * situation.
	 * 
	 * @return the fallback conversation map
	 */
	public abstract Map<String, Conversation> getConversationMapFallback();

	/**
	 * @param useFallbackMap <code>true</code>, if the fallback map should be
	 * used in the case the default one is not reachable, <code>false</code> to
	 * raise an exception in that case
	 */
	public void setUseFallbackMap(boolean useFallbackMap) {
		this.useFallbackMap = useFallbackMap;
	}

	/**
	 * @return the <code>true</code>, if the fallback map should be used in the
	 * case the default one is not reachable
	 */
	public boolean isUseFallbackMap() {
		return useFallbackMap;
	}
}
