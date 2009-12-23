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

import java.util.List;

import org.springframework.conversation.manager.ConversationManager;

/**
 * <p>
 * The extended {@link Conversation} interface for a conversation object to be
 * modified.
 * </p>
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public interface MutableConversation extends Conversation {

	/**
	 * Set the id for this conversation which must be unique.
	 * 
	 * @param id the id of the conversation
	 */
	void setId(String id);

	/**
	 * Set the temporary flag of this conversation to the given value. If
	 * <code>true</code>, the conversation is made long running,
	 * <code>false</code> to be identified as a temporary one.
	 * 
	 * @param temporary the temporary flag
	 */
	void setTemporary(boolean temporary);

	/**
	 * Set the given conversation as the parent conversation of this one. So
	 * this conversation will be a nested conversation of the given parent.
	 * 
	 * @param parentConversation the parent conversation to be set
	 * @param isIsolated flag indicating whether this conversation should be
	 * isolated from the given parent conversation so that it does not inherit
	 * the state of the parent but rather has its own, isolated state
	 */
	void setParentConversation(MutableConversation parentConversation, boolean isIsolated);

	/**
	 * Invoked by {@link #setParentConversation(MutableConversation, boolean)}
	 * on the parent conversation to double link parent and nested conversations
	 * in order to be able to return the tail of nested conversations.
	 * 
	 * @param nestedConversation the nested conversation to be set on this
	 * parent conversation
	 */
	void setNestedConversation(MutableConversation nestedConversation);

	/**
	 * Returns the tail of this conversation which is the last nested
	 * conversation or this instance, if there are no nested conversations or
	 * this is the last nested one.
	 * 
	 * @return the last nested conversation
	 */
	MutableConversation getTail();

	/**
	 * This method is invoked if this conversation is joined which is the case,
	 * if a new conversation is being created through the manager with join mode
	 * allowing to join. This method should increase an internal counter which
	 * should be decreased accordingly by ending this conversation.
	 */
	void joinConversation();

	/**
	 * Returns the join count telling how many times this conversation has been
	 * joined and the end method may be invoked accordingly before the
	 * conversation is really ended.
	 * 
	 * @return the join count
	 */
	int getJoinCount();

	/**
	 * This method is invoked by the conversation manager to finally end this
	 * conversation. The method {@link #end()} is just invoking the
	 * {@link ConversationManager#endConversation(Conversation)} method but does
	 * not actually end it properly, this is done within this method which is
	 * guaranteed to be invoked as the {@link #end()} method is not.
	 */
	void endConversation();

	/**
	 * This method is a convenience method to {@link Conversation#end()} and
	 * will not only end this conversation but also end its parent until the
	 * root to finally end it. This method should only be invoked internally as
	 * it could break the chain of nested conversations. It is usually just used
	 * to finally end a timed out conversation.
	 */
	void finalEnd();

	/**
	 * Reset the last access timestamp using the current time in milliseconds
	 * from the system. This is usually done if a conversation is used behind a
	 * scope and beans are being accessed or added to it.
	 */
	void touch();

	/**
	 * @return a list of registered listeners of this conversation, might be
	 * empty, never <code>null</code>
	 */
	List<ConversationListener> getListeners();
}
