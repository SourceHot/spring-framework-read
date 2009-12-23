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

import java.util.List;

import org.springframework.conversation.Conversation;
import org.springframework.conversation.scope.ConversationResolver;
import org.springframework.conversation.scope.ConversationScope;

/**
 * The conversation store is used within the {@link ConversationScope} to store
 * conversations in depending on where they should be mapped.<br/>
 * Usually the store is session scoped, if there is a session available and the
 * store should provide alternatives, if a conversation is being initiated from
 * within a non-web thread.<br/>
 * The store is divided out from the conversation scope to make it more
 * pluggable and easier to change.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public interface ConversationStore {

	/**
	 * Invoked by the manager to register the given conversation within the
	 * store. Registering should be done using the id of the conversation as
	 * being returned by {@link Conversation#getId()}. The manager usually
	 * invoked this method to register a newly created conversation and will
	 * invoke {@link ConversationResolver#setCurrentConversation(String)} to
	 * make it the current one.
	 * 
	 * @param conversation the conversation to be registered
	 */
	void registerConversation(Conversation conversation);

	/**
	 * Returns the conversation with the given id which has to be registered
	 * before. If no such conversation is found, <code>null</code> is returned
	 * rather than throwing an exception.
	 * 
	 * @param id the id to return the conversation from this store
	 * @return the conversation, if found, <code>null</code> otherwise
	 */
	Conversation getConversation(String id);

	/**
	 * Removes the conversation with the given id from this store.
	 * 
	 * @param id the id of the conversation to be removed
	 * @return the removed conversation, if found and removed, <code>null</code>
	 * otherwise
	 */
	Conversation removeConversation(String id);

	/**
	 * Returns the number of registered conversations within this store.
	 * 
	 * @return the number of registered conversations
	 */
	int size();

	/**
	 * Returns a list of available conversations in the current store which have
	 * been registered using the {@link #registerConversation(Conversation)}
	 * method.
	 * 
	 * @return the list of available conversations or an empty list, never
	 * <code>null</code>
	 */
	List<Conversation> getConversations();
}
