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
package org.springframework.conversation.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.conversation.Conversation;
import org.springframework.conversation.JoinMode;
import org.springframework.conversation.manager.ConversationManager;
import org.springframework.conversation.manager.ConversationResolver;
import org.springframework.conversation.manager.ConversationStore;
import org.springframework.util.Assert;

/**
 * The default implementation of the {@link ConversationScope} exposed as
 * <code>"conversation"</code> scope. It needs the {@link ConversationStore} and
 * the {@link ConversationResolver} to resolve and request the current
 * conversation where attributes are resolved with and registered in.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public class DefaultConversationScope implements ConversationScope{

	/** Holds the conversation manager reference, if statically injected. */
	private ConversationManager conversationManager;

	/** Holds the conversation store reference, if statically injected. */
	private ConversationStore conversationStore;

	/** Holds the conversation resolver reference, if statically injected. */
	private ConversationResolver conversationResolver;

	/**
	 * This method is invoked to resolve the current conversation used where
	 * attributes having conversation scope are being resolved with or stored
	 * in.
	 * 
	 * @return the currently used conversation, or <code>null</code>, if no one
	 * currently available and <code>createIfNotExisting</code> is
	 * <code>false</code>
	 */
	protected Conversation getCurrentConversation(boolean createNewIfNotExisting) {
		ConversationResolver resolver = getConversationResolver();
		Assert.notNull(resolver, "No conversation resolver available within the conversation scope");

		String conversationId = resolver.getCurrentConversationId();
		Conversation conversation;
		if (conversationId == null) {
			if (createNewIfNotExisting) {
				// start a new, temporary conversation using the default join
				// mode
				ConversationManager manager = getConversationManager();
				conversation = manager.beginConversation(true, JoinMode.getDefaultJoinMode());
			} else {
				return null;
			}
		} else {
			ConversationStore store = getConversationStore();
			Assert.notNull(store, "No conversation store available within the conversation scope");
			conversation = store.getConversation(conversationId);
			Assert.notNull(conversation, "The conversation with id <" + conversationId
					+ "> is not available within the store");
		}

		return conversation;
	}

	/**
	 * @see org.springframework.beans.factory.config.Scope#get(java.lang.String,
	 * org.springframework.beans.factory.ObjectFactory)
	 */
	public Object get(String name, ObjectFactory<?> objectFactory) {
		Conversation conversation = getCurrentConversation(true);
		Object attribute = conversation.getAttribute(name);
		if (attribute == null) {
			attribute = objectFactory.getObject();
			conversation.setAttribute(name, attribute);
		}

		return attribute;
	}

	/**
	 * @see org.springframework.beans.factory.config.Scope#getConversationId()
	 */
	public String getConversationId() {
		Conversation conversation = getCurrentConversation(false);
		if (conversation != null) {
			return conversation.getId();
		}

		return null;
	}

	/**
	 * @see org.springframework.beans.factory.config.Scope#registerDestructionCallback(java.lang.String,
	 * java.lang.Runnable)
	 */
	public void registerDestructionCallback(String name, Runnable callback) {
		Conversation conversation = getCurrentConversation(false);
		if (conversation != null) {
			conversation.registerDestructionCallback(name, callback);
		}
	}

	/**
	 * @see org.springframework.beans.factory.config.Scope#remove(java.lang.String)
	 */
	public Object remove(String name) {
		Conversation conversation = getCurrentConversation(false);
		if (conversation != null) {
			return conversation.removeAttribute(name);
		}

		return null;
	}

	/**
	 * Supports the following objects:
	 * <ul>
	 * <li><code>"conversationManager"</code>, returns the
	 * {@link ConversationManager}</li>
	 * <li><code>"conversationStore"</code>, returns the
	 * {@link ConversationStore}</li>
	 * <li><code>"conversationResolver"</code>, returns the
	 * {@link ConversationResolver}</li>
	 * </ul>
	 * 
	 * @see org.springframework.beans.factory.config.Scope#resolveContextualObject(java.lang.String)
	 */
	public Object resolveContextualObject(String key) {
		if (ConversationScope.REFERENCE_CONVERSATION_MANAGER.equals(key)) {
			return getConversationManager();
		} else if (ConversationScope.REFERENCE_CONVERSATION_STORE.equals(key)) {
			return getConversationStore();
		} else if (ConversationScope.REFERENCE_CONVERSATION_RESOLVER.equals(key)) {
			return getConversationResolver();
		}

		return null;
	}

	/**
	 * @param conversationManager the conversation manager reference to be used
	 * by this scope
	 */
	public void setConversationManager(ConversationManager conversationManager) {
		this.conversationManager = conversationManager;
	}

	/**
	 * @return the conversation manager reference
	 */
	public ConversationManager getConversationManager() {
		return conversationManager;
	}

	/**
	 * @param conversationStore the reference of the conversation store to be
	 * injected and internally used to request registered conversation objects
	 */
	public void setConversationStore(ConversationStore conversationStore) {
		this.conversationStore = conversationStore;
	}

	/**
	 * Returns the reference of the conversation store to be used. If the store
	 * is not within the same scope as the conversation scope, this method has
	 * to be injected.
	 * 
	 * @return the reference of the conversation store
	 */
	public ConversationStore getConversationStore() {
		return conversationStore;
	}

	/**
	 * @param conversationResolver the reference of the conversation resolver
	 * used to determine the currently used conversation id
	 */
	public void setConversationResolver(ConversationResolver conversationResolver) {
		this.conversationResolver = conversationResolver;
	}

	/**
	 * Returns the reference of the conversation resolver to be used. If the
	 * resolver is not within the same scope as the conversation scope, this
	 * method has to be injected.
	 * 
	 * @return the reference of the conversation resolver
	 */
	public ConversationResolver getConversationResolver() {
		return conversationResolver;
	}
}