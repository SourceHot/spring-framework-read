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
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.config.DestructionAwareAttributeMap;
import org.springframework.conversation.Conversation;
import org.springframework.conversation.ConversationActivationType;
import org.springframework.conversation.ConversationDeactivationType;
import org.springframework.conversation.ConversationEndingType;
import org.springframework.conversation.ConversationListener;
import org.springframework.conversation.JoinMode;

/**
 * <p>
 * The default implementation of the {@link Conversation} and
 * {@link MutableConversation} interface.
 * </p>
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public class ConversationImpl extends DestructionAwareAttributeMap implements MutableConversation {
	/** Serializable identifier. */
	private static final long serialVersionUID = 1L;

	/** The conversation id which is unique. */
	private String id;

	/**
	 * The reference to the conversation manager this conversation was created
	 * with. The manager is used to end this conversation.
	 */
	private ConversationManager manager;

	/** The parent conversation, if this is a nested conversation. */
	private MutableConversation parent;

	/** The optional nested conversation, if this is a parent conversation. */
	private MutableConversation child;

	/** The temporary flag. */
	private boolean temporary;

	/** Flag indicating whether this conversation has already be ended. */
	private boolean ended;

	/**
	 * If set to <code>true</code>, this conversation does not inherit the state
	 * of its parent but rather has its own, isolated state. This is set to
	 * <code>true</code>, if a new conversation with {@link JoinMode#ISOLATED}
	 * is created.
	 */
	private boolean isolated;

	/** Flag, indicating whether this is a switched conversation. */
	private boolean switched;

	/**
	 * The join count which is increased on the joining method and decreased on
	 * the end method.
	 */
	private int joinCount;

	/** The optional list of listeners being registered for this conversation. */
	private List<ConversationListener> listeners;

	/** The timeout in milliseconds or <code>0</code>, if no timeout specified. */
	private long timeout;

	/** The timestamp in milliseconds of the last access to this conversation. */
	private long lastAccess;

	/**
	 * Default constructor, setting the last access timestamp to the current
	 * system time in milliseconds.
	 */
	public ConversationImpl() {
		touch();
	}

	/**
	 * Considers the internal attribute map as well as the map from the parent,
	 * if this is a nested conversation and is not isolated.
	 * 
	 * @see org.springframework.conversation.Conversation#getAttribute(java.lang.String)
	 */
	@Override
	public <T> T getAttribute(String name) {
		touch();

		// first try to get the attribute from this conversation state
		T value = super.getAttribute(name);
		if (value != null) {
			return value;
		}

		// the value was not found, try the parent conversation, if any and if
		// not isolated
		if (parent != null && !isolated) {
			return parent.getAttribute(name);
		}

		// this is the root conversation and the requested bean is not
		// available, so return null instead
		return null;
	}

	/**
	 * @see org.springframework.conversation.Conversation#setAttribute(java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public <T> T setAttribute(String name, T value) {
		touch();

		// check for implementing the ConversationListener interface and
		// register the object as a listener, if so
		if (value instanceof ConversationListener) {
			addListener((ConversationListener) value);
		}

		return super.setAttribute(name, value);
	}

	/**
	 * @see org.springframework.conversation.Conversation#removeAttribute(java.lang.String)
	 */
	@Override
	public <T> T removeAttribute(String name) {
		touch();
		T value = super.removeAttribute(name);

		// if the attribute implements the listener interface, remove it from
		// the registered listeners
		if (value instanceof ConversationListener) {
			removeListener((ConversationListener) value);
		}

		return value;
	}

	/**
	 * @see org.springframework.conversation.manager.MutableConversation#setTemporary(boolean)
	 */
	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}

	/**
	 * @see org.springframework.conversation.manager.MutableConversation#setSwitched(boolean)
	 */
	public void setSwitched(boolean switched) {
		this.switched = switched;
	}

	/**
	 * @see org.springframework.conversation.Conversation#begin()
	 */
	public void begin() {
		temporary = false;
	}

	/**
	 * @see org.springframework.conversation.Conversation#switchTo()
	 */
	public void switchTo() {
		manager.switchConversation(getId());
	}

	/**
	 * Is just delegated to
	 * {@link ConversationManager#endConversation(Conversation, ConversationEndingType)}
	 * .
	 * 
	 * @see org.springframework.conversation.Conversation#end(org.springframework.conversation.ConversationEndingType)
	 */
	public void end(ConversationEndingType endingType) {
		manager.endConversation(this, endingType);
	}

	/**
	 * Is just delegated to
	 * {@link ConversationManager#finalEndConversation(Conversation, ConversationEndingType)}
	 * .
	 * 
	 * @see org.springframework.conversation.manager.MutableConversation#finalEnd(org.springframework.conversation.ConversationEndingType)
	 */
	public void finalEnd(ConversationEndingType endingType) {
		manager.finalEndConversation(this, endingType);
	}

	/**
	 * @see org.springframework.conversation.manager.MutableConversation#internallyEndConversation(org.springframework.conversation.ConversationEndingType)
	 */
	public void internallyEndConversation(ConversationEndingType endingType) {
		// check, if this conversation was joined before and do not end it if so
		if (joinCount > 0) {
			joinCount--;
			return;
		}

		ended = true;

		// invoke listeners, if any
		List<ConversationListener> list = getListeners();
		if (list != null) {
			for (ConversationListener listener : list) {
				listener.conversationEnded(this, endingType);
			}
		}

		// flush the state of this conversation AFTER the listeners have been
		// invoked as there could be some state still needed
		clear();

		// remove this conversation from its parent, if it is a nested one
		removeFromParent();
	}

	/**
	 * Just increases the join count of this conversation to mark it being
	 * joined which is taken into account while ending it.
	 * 
	 * @see org.springframework.conversation.manager.MutableConversation#joinConversation()
	 */
	public void joinConversation() {
		joinCount++;
	}

	/**
	 * @see org.springframework.conversation.manager.MutableConversation#getJoinCount()
	 */
	public int getJoinCount() {
		return joinCount;
	}

	/**
	 * @see org.springframework.conversation.manager.MutableConversation#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @see org.springframework.conversation.Conversation#getId()
	 */
	public String getId() {
		return id;
	}

	/**
	 * @see org.springframework.conversation.Conversation#getParent()
	 */
	public Conversation getParent() {
		return parent;
	}

	/**
	 * @see org.springframework.conversation.Conversation#getRoot()
	 */
	public Conversation getRoot() {
		// check for having a parent to be returned as the root
		if (parent != null) {
			return parent.getRoot();
		}

		return this;
	}

	/**
	 * @see org.springframework.conversation.manager.MutableConversation#setParentConversation(org.springframework.conversation.manager.MutableConversation,
	 * boolean)
	 */
	public void setParentConversation(MutableConversation parentConversation, boolean isIsolated) {
		this.parent = parentConversation;
		this.isolated = isIsolated;

		// set the nested conversation within the parent to double link them
		this.parent.setNestedConversation(this);
	}

	/**
	 * @see org.springframework.conversation.manager.MutableConversation#setNestedConversation(org.springframework.conversation.manager.MutableConversation)
	 */
	public void setNestedConversation(MutableConversation nestedConversation) {
		this.child = nestedConversation;
	}

	/**
	 * If this is a nested conversation, this method removes it from its parent
	 * by setting the nested child conversation to <code>null</code> on its
	 * parent.
	 */
	protected void removeFromParent() {
		if (parent != null) {
			parent.setNestedConversation(null);
		}
	}

	/**
	 * @see org.springframework.conversation.manager.MutableConversation#getTail()
	 */
	public MutableConversation getTail() {
		// if this is the last conversation (has no more nested ones), return
		// it, otherwise recursively invoke this
		// method on the nested one
		if (child != null) {
			return child.getTail();
		}

		return this;
	}

	/**
	 * @see org.springframework.conversation.Conversation#isNested()
	 */
	public boolean isNested() {
		return (parent != null);
	}

	/**
	 * @see org.springframework.conversation.manager.MutableConversation#isParent()
	 */
	public boolean isParent() {
		return (child != null && !child.isEnded());
	}

	/**
	 * @see org.springframework.conversation.Conversation#isTemporary()
	 */
	public boolean isTemporary() {
		return temporary;
	}

	/**
	 * @see org.springframework.conversation.Conversation#isIsolated()
	 */
	public boolean isIsolated() {
		return isolated;
	}

	/**
	 * @see org.springframework.conversation.Conversation#isSwitched()
	 */
	public boolean isSwitched() {
		return switched;
	}

	/**
	 * @see org.springframework.conversation.Conversation#isEnded()
	 */
	public boolean isEnded() {
		return ended;
	}

	/**
	 * @see org.springframework.conversation.Conversation#getTimeout()
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * @see org.springframework.conversation.Conversation#setTimeout(long)
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return <code>true</code>, if this conversation has been timed out
	 * according to the last access timestamp and the timeout value being set
	 */
	public boolean isTimeout() {
		return (timeout != 0 && (lastAccess + timeout < System.currentTimeMillis()));
	}

	/**
	 * @see org.springframework.conversation.Conversation#getLastAccess()
	 */
	public long getLastAccess() {
		return lastAccess;
	}

	/**
	 * @see org.springframework.conversation.manager.MutableConversation#touch()
	 */
	public void touch() {
		lastAccess = System.currentTimeMillis();

		// if this is a nested conversation, also touch its parent to make sure
		// the parent is never timed out, if the
		// current conversation is one of its nested conversations
		if (parent != null) {
			parent.touch();
		}
	}

	/**
	 * @see org.springframework.conversation.manager.MutableConversation#activated(org.springframework.conversation.ConversationActivationType,
	 * org.springframework.conversation.Conversation)
	 */
	public void activated(ConversationActivationType activationType, Conversation oldCurrentConversation) {
		touch();
		List<ConversationListener> list = getListeners();
		if (list == null) {
			return;
		}

		for (ConversationListener listener : list) {
			listener.conversationActivated(this, oldCurrentConversation, activationType);
		}
	}

	/**
	 * @see org.springframework.conversation.manager.MutableConversation#deactivated(org.springframework.conversation.ConversationDeactivationType,
	 * org.springframework.conversation.Conversation)
	 */
	public void deactivated(ConversationDeactivationType deactivationType, Conversation newCurrentConversation) {
		touch();
		List<ConversationListener> list = getListeners();
		if (list == null) {
			return;
		}

		for (ConversationListener listener : list) {
			listener.conversationDeactivated(this, newCurrentConversation, deactivationType);
		}
	}

	/**
	 * @see org.springframework.conversation.manager.MutableConversation#getListeners()
	 */
	public List<ConversationListener> getListeners() {
		if (listeners == null) {
			return Collections.emptyList();
		}

		return listeners;
	}

	/**
	 * @see org.springframework.conversation.Conversation#addListener(org.springframework.conversation.ConversationListener)
	 */
	public void addListener(ConversationListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<ConversationListener>();
		}

		listeners.add(listener);
	}

	/**
	 * @see org.springframework.conversation.Conversation#removeListener(org.springframework.conversation.ConversationListener)
	 */
	public void removeListener(ConversationListener listener) {
		if (listeners == null) {
			return;
		}

		listeners.remove(listener);
	}

	/**
	 * @param manager the conversation manager which created this conversation
	 * object
	 */
	public void setManager(ConversationManager manager) {
		this.manager = manager;
	}
}
