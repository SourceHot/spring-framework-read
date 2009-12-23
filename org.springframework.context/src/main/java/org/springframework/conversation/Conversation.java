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

import java.util.Map;

import org.springframework.conversation.manager.ConversationManager;
import org.springframework.conversation.scope.ConversationScope;

/**
 * The interface for a conversation, the instance behind the
 * {@link ConversationScope}.<br/>
 * Conversations are created through the {@link ConversationManager} which is
 * the main API for conversation management unless the annotations are used. See
 * {@link ConversationManager} for a more detailed description on how the
 * conversation management works.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public interface Conversation {

	/**
	 * This method starts a long running conversation, if not done already.
	 * Moves a temporary conversation into long running mode.
	 */
	void begin();

	/**
	 * Ends the current conversation. If this is a nested conversation, this
	 * method will switch back to its parent, making it the current
	 * conversation. This method is invoking the
	 * {@link ConversationManager#endConversation(Conversation, ConversationEndingType)}
	 * accordingly to remove this conversation from the conversation store.
	 * 
	 * @param endingType the type qualifying on how this conversation is to be
	 * ended (only passed on to any listeners, does not have an impact on the
	 * conversation manager)
	 */
	void end(ConversationEndingType endingType);

	/**
	 * Stores the given value in this conversation using the specified name. If
	 * this state already contains a value attached to the given name, it is
	 * returned, <code>null</code> otherwise.<br/>
	 * This method stores the attribute within this conversation so it will be
	 * available through this and all nested conversations.
	 * 
	 * @param name the name of the value to be stored in this conversation
	 * @param value the value to be stored
	 * @return the old value attached to the same name, if any,
	 * <code>null</code> otherwise
	 */
	Object setAttribute(String name, Object value);

	/**
	 * Returns the value attached to the given name, if any previously
	 * registered, <code>null</code> otherwise.<br/>
	 * Returns the attribute stored with the given name within this conversation
	 * or any within the path through its parent to the top level root
	 * conversation. If this is a nested, isolated conversation, attributes are
	 * only being resolved within this conversation, not from its parent.
	 * 
	 * @param name the name of the value to be retrieved
	 * @return the value, if available in the current state, <code>null</code>
	 * otherwise
	 */
	Object getAttribute(String name);

	/**
	 * Removes the value in the current conversation having the given name and
	 * returns it, if found and removed, <code>null</code> otherwise.<br/>
	 * Removes the attribute from this specific conversation, does not remove
	 * it, if found within its parent.
	 * 
	 * @param name the name of the value to be removed from this conversation
	 * @return the removed value, if found, <code>null</code> otherwise
	 */
	Object removeAttribute(String name);

	/**
	 * Returns the map holding the attributes of this conversation. The map will
	 * NOT include the attributes being stored within the parent of this
	 * conversation, if it is a nested one, hence {@link #getAttribute(String)}
	 * could return attributes which are not available within the map being
	 * returned by this method, if stored within the map of the parent.
	 * 
	 * @return the map holding the attributes of this conversation
	 */
	Map<String, Object> getAttributeMap();

	/**
	 * Returns the id of this conversation which is its identifier to be used
	 * within forms, redirections or anywhere else to define the current
	 * conversation.
	 * 
	 * @return the id of this conversation
	 */
	String getId();

	/**
	 * Returns <code>true</code>, if this is a temporary conversation (started
	 * automatically, not through an annotation or through the API). Such a
	 * temporary conversation is merged, if there is a later annotation or API
	 * call to begin a new conversation.
	 * 
	 * @return <code>true</code>, if this is a temporary conversation,
	 * <code>false</code> for a long running conversation
	 */
	boolean isTemporary();

	/**
	 * Returns <code>true</code>, if this conversation has already be ended.
	 * This is the case, if the method {@link #end(ConversationEndingType)} has
	 * been invoked or the conversation was ended through
	 * {@link ConversationManager#endConversation(Conversation, ConversationEndingType)}
	 * .
	 * 
	 * @return <code>true</code>, if this conversation has already be ended
	 */
	boolean isEnded();

	/**
	 * Returns the parent conversation, if this is a nested conversation,
	 * <code>null</code> otherwise.
	 * 
	 * @return the parent conversation, if any, <code>null</code> otherwise
	 */
	Conversation getParent();

	/**
	 * Returns the top level root conversation, if this is a nested conversation
	 * or this conversation, if it is the top level root conversation. This
	 * method never returns <code>null</code>.
	 * 
	 * @return the root conversation (top level conversation)
	 */
	Conversation getRoot();

	/**
	 * Returns <code>true</code>, if this is a nested conversation and hence
	 * {@link #getParent()} will returns a non-null value.
	 * 
	 * @return <code>true</code>, if this is a nested conversation,
	 * <code>false</code> otherwise
	 */
	boolean isNested();

	/**
	 * Returns <code>true</code>, if this is a nested, isolated conversation so
	 * that it does not inherit the state from its parent but rather has its own
	 * state. See {@link JoinMode#ISOLATED} for more details.
	 * 
	 * @return <code>true</code>, if this is a nested, isolated conversation
	 */
	boolean isIsolated();

	/**
	 * Adds the given listener to this conversation to be invoked upon certain
	 * conversation events as a hooking possibility.
	 * 
	 * @param listener the listener to be added
	 */
	void addListener(ConversationListener listener);

	/**
	 * Removes the given listener from the list of listener of this
	 * conversation.
	 * 
	 * @param listener the listener to be removed
	 */
	void removeListener(ConversationListener listener);

	/**
	 * Returns the system time in milliseconds this conversation was last
	 * accessed (usually through a {@link #getAttribute(String)},
	 * {@link #setAttribute(String, Object)} or {@link #removeAttribute(String)}
	 * access).
	 * 
	 * @return the system time in milliseconds for the last access of this
	 * conversation
	 */
	long getLastAccess();

	/**
	 * Returns the timeout of this conversation in milliseconds, if one has been
	 * specified. It declares the time in milliseconds a conversation will be
	 * timed out after having no activity. If a timeout value of <code>0</code>
	 * is specified, the conversation is never timed out (this is the default).
	 * 
	 * @return the timeout in milliseconds or <code>0</code>, if no timeout
	 * specified
	 */
	long getTimeout();

	/**
	 * Set a timeout in milliseconds for this conversation specifying the time a
	 * conversation is timed out after a certain amount of inactivity.
	 * 
	 * @param timeout the timeout in milliseconds after inactivity of the
	 * conversation or <code>0</code>, if there should be no timeout
	 */
	void setTimeout(long timeout);
}
