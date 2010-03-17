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
import org.springframework.conversation.ConversationActivationType;
import org.springframework.conversation.ConversationDeactivationType;
import org.springframework.conversation.ConversationEndingType;
import org.springframework.conversation.ConversationType;
import org.springframework.conversation.JoinMode;

/**
 * The default implementation for the {@link ConversationManager} interface.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public class ConversationManagerImpl implements ConversationManager {
	/** The next conversation id to be assigned to a newly created instance. */
	private static int nextConversationId = 1;

	/**
	 * The conversation resolver used to resolve and expose the currently used
	 * conversation id. Do not use this attribute directly, always use the
	 * {@link #getConversationResolver()} method as the getter could have been
	 * injected.
	 */
	private ConversationResolver conversationResolver;

	/**
	 * If the store was injected, this attribute holds the reference to it.
	 * Never use the attribute directly, always use the
	 * {@link #getConversationStore()} getter as it could have been injected.
	 */
	private ConversationStore conversationStore;

	/**
	 * The default timeout in milliseconds for new conversations, can be setup
	 * using the Spring configuration of the manager. A value of <code>0</code>
	 * means there is no timeout.
	 */
	private long defaultTimeout = 0;

	/**
	 * @see org.springframework.conversation.manager.ConversationManager#getCurrentConversation()
	 */
	public Conversation getCurrentConversation() {
		ConversationResolver resolver = getConversationResolver();
		String conversationId = resolver.getCurrentConversationId();
		if (conversationId != null) {
			ConversationStore store = getConversationStore();
			return store.getConversation(conversationId);
		}
		return null;
	}

	/**
	 * @see org.springframework.conversation.manager.ConversationManager#getConversations()
	 */
	public List<Conversation> getConversations() {
		return getConversationStore().getConversations();
	}

	/**
	 * @see org.springframework.conversation.manager.ConversationManager#switchConversation(java.lang.String)
	 */
	public Conversation switchConversation(String conversationId) {
		ConversationStore store = getConversationStore();
		MutableConversation conversation = silentlyCastConversation(store.getConversation(conversationId));
		if (conversation != null) {
			MutableConversation oldConversation = silentlyCastConversation(getCurrentConversation());
			ConversationResolver resolver = getConversationResolver();
			resolver.setCurrentConversationId(conversationId);

			// invoke listeners
			if (oldConversation != null) {
				oldConversation.deactivated(ConversationDeactivationType.SWITCHED, conversation);
			}

			conversation.activated(ConversationActivationType.SWITCHED, oldConversation);
		}

		return conversation;
	}

	/**
	 * @see org.springframework.conversation.manager.ConversationManager#endCurrentConversation(org.springframework.conversation.ConversationEndingType)
	 */
	public Conversation endCurrentConversation(ConversationEndingType endingType) {
		Conversation conversation = getCurrentConversation();
		if (conversation != null) {
			endConversation(conversation, endingType);
		}

		return conversation;
	}

	/**
	 * @see org.springframework.conversation.manager.ConversationManager#endConversation(org.springframework.conversation.Conversation,
	 * org.springframework.conversation.ConversationEndingType)
	 */
	public void endConversation(Conversation conversation, ConversationEndingType endingType) {
		MutableConversation c = silentlyCastConversation(conversation);
		if (c.isParent()) {
			throw new IllegalStateException(
					"Illegal attempt to end a conversation, still having a nested, active conversation.");
		}

		ConversationResolver resolver = getConversationResolver();
		String currentId = resolver.getCurrentConversationId();
		boolean wasCurrent = (currentId != null && currentId.equals(conversation.getId()));

		// let the conversation object end (this will invoke any listeners too,
		// if the conversation was finally ended)
		c.internallyEndConversation(endingType);

		// if the conversation object was really ended, remove it from the store
		// and make its parent the current one, if nested
		if (conversation.isEnded()) {
			ConversationStore store = getConversationStore();
			store.removeConversation(conversation.getId());

			if (wasCurrent) {
				// if this was a nested conversation and it was the current one,
				// switch to its parent and set it as the current one
				if (conversation.isNested()) {
					MutableConversation parent = silentlyCastConversation(conversation.getParent());
					resolver.setCurrentConversationId(parent.getId());

					// invoke listener on the parent because it was made active
					parent.activated(conversation.isIsolated() ? ConversationActivationType.ISOLATING_ENDED
							: ConversationActivationType.NESTING_ENDED, conversation);
				} else {
					resolver.setCurrentConversationId(null);
				}
			}
		}

		// invoke ending hook
		conversationEnded(c, !c.isEnded());
	}

	/**
	 * Internal hook method to be overwritten if anything has to be done after
	 * the given conversation has been ended finally. This hook method is also
	 * invoked, if the given conversation was ended but was a joined one and
	 * hence is not finished already, so flushing is only done, if the
	 * conversation was not a joined one or is the last one and really ended.<br/>
	 * Do not overwrite the
	 * {@link #endConversation(Conversation, ConversationEndingType)} method
	 * unless you really have to, rather override this one.
	 * 
	 * @param conversation the conversation which was ended
	 * @param wasJoined flag, indicating whether the given conversation was a
	 * joined one and hence is not really finished
	 */
	protected void conversationEnded(MutableConversation conversation, boolean wasJoined) {

	}

	/**
	 * @see org.springframework.conversation.manager.ConversationManager#finalEndConversation(org.springframework.conversation.Conversation,
	 * org.springframework.conversation.ConversationEndingType)
	 */
	public void finalEndConversation(Conversation conversation, ConversationEndingType endingType) {
		// make sure we have the tail of the conversation provided
		Conversation conv = silentlyCastConversation(conversation).getTail();

		while (conv != null) {
			// end the current conversation
			endConversation(conv, endingType);

			// check if the conversation is really ended (if not, it was a
			// joined one)
			if (conv.isEnded()) {
				// get the parent for the next loop to be ended, if this was a
				// nested conversation
				conv = conv.getParent();
			}
		}
	}

	/**
	 * @see org.springframework.conversation.manager.ConversationManager#beginConversation(boolean,
	 * org.springframework.conversation.JoinMode)
	 */
	public Conversation beginConversation(boolean temporary, JoinMode joinMode) {
		ConversationResolver resolver = getConversationResolver();
		ConversationStore store = getConversationStore();
		MutableConversation currentConversation = silentlyCastConversation(getCurrentConversation());
		MutableConversation newConversation = null;

		if (currentConversation == null || !joinMode.mustBeJoined()) {
			// create a new conversation and initialize it
			newConversation = createNewConversation();

			// set the default timeout according the setup of this manager
			newConversation.setTimeout(getDefaultConversationTimeout());

			// set the temporary flag of the newly created conversation and also
			// assign it a newly created, unique id
			newConversation.setTemporary(temporary);
			newConversation.setSwitched(joinMode == JoinMode.SWITCHED);
			newConversation.setId(createNewConversationId());

			store.registerConversation(newConversation);
		}

		// check, if there is a current conversation
		if (currentConversation != null) {
			// check, if nesting is not allowed and throw an exception if so
			if (joinMode.mustBeRoot()) {
				throw new IllegalStateException(
						"Beginning a new conversation while one is still in progress and nesting is not allowed is not possible.");
			}

			if (joinMode.mustBeJoined()) {
				currentConversation.joinConversation();
				return currentConversation;
			}

			if (joinMode.mustBeSwitched() && currentConversation.isSwitched()) {
				currentConversation.deactivated(ConversationDeactivationType.NEW_SWITCHED, newConversation);
			}

			// check, if the current one must be ended before creating a new one
			if (joinMode.mustEndCurrent(currentConversation)) {
				endConversation(currentConversation, ConversationEndingType.TRANSCRIBED);
			}

			// nest the new conversation to the current one, if available and
			// set the current one as the parent of the new one
			if (joinMode.mustBeNested()) {
				newConversation.setParentConversation(currentConversation, joinMode.mustBeIsolated());
				currentConversation.deactivated(joinMode.mustBeIsolated() ? ConversationDeactivationType.NEW_ISOLATED
						: ConversationDeactivationType.NEW_NESTED, newConversation);
			}
		}

		// make the newly created conversation the current one, invoke
		// listeners and return it
		resolver.setCurrentConversationId(newConversation.getId());
		newConversation.activated(joinMode.mustBeSwitched() ? ConversationActivationType.SWITCHED
				: ConversationActivationType.NEW, currentConversation);

		return newConversation;
	}

	/**
	 * @see org.springframework.conversation.manager.ConversationManager#
	 * beginConversation(org.springframework.conversation.JoinMode,
	 * org.springframework.conversation.ConversationType,
	 * org.springframework.conversation
	 * .manager.ConversationInitializationCallback<T>[])
	 */
	public <T extends ConversationType> Conversation beginConversation(JoinMode joinMode, T conversationType,
			ConversationInitializationCallback<T>... callbacks) {
		// first create a new conversation according the given join type (not as
		// a temporary one though)
		Conversation conversation = beginConversation(false, joinMode);

		// invoke callbacks, if any
		if (callbacks != null) {
			for (ConversationInitializationCallback<T> callback : callbacks) {
				callback.conversationStarted(conversation, conversationType);
			}
		}

		// return the initialized conversation
		return conversation;
	}

	/**
	 * Hook method creating a new conversation object which still is
	 * uninitialized.
	 * 
	 * @return the newly created conversation object
	 */
	protected MutableConversation createNewConversation() {
		ConversationImpl conversation = new ConversationImpl();
		conversation.setManager(this);
		return conversation;
	}

	/**
	 * Creates a new conversation id to be assigned to the next conversation
	 * being created.
	 * 
	 * @return the next, newly created, unique conversation id
	 */
	protected String createNewConversationId() {
		// create the next id by just increasing the counter
		int id = 0;
		synchronized (this) {
			id = nextConversationId++;
		}

		return Integer.toString(id);
	}

	/**
	 * Internally used to silently cast the given conversation into a mutable
	 * one, throwing an exception, if not castable.
	 * 
	 * @param conversation the conversation to be casted (might be
	 * <code>null</code> as well for convenience)
	 * @return the mutable conversation
	 */
	protected MutableConversation silentlyCastConversation(Conversation conversation) {
		try {
			if (conversation == null) {
				return null;
			}

			return (MutableConversation) conversation;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("The conversation implementation must implement the ["
					+ MutableConversation.class.getName() + "] interface.");
		}
	}

	/**
	 * @see org.springframework.conversation.manager.ConversationManager#setDefaultConversationTimeout(long)
	 */
	public void setDefaultConversationTimeout(long defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
	}

	/**
	 * @see org.springframework.conversation.manager.ConversationManager#getDefaultConversationTimeout()
	 */
	public long getDefaultConversationTimeout() {
		return defaultTimeout;
	}

	/**
	 * Returns the store where conversation objects are being registered. If the
	 * manager is in a wider scope than the store (if the store is not singleton
	 * scoped), this method has to be injected.
	 * 
	 * @return the conversation store used to register conversation objects
	 */
	public ConversationStore getConversationStore() {
		return conversationStore;
	}

	/**
	 * Inject the conversation store to this manager which should only be done,
	 * if the method {@link #getConversationStore()} is not injected and hence
	 * the store has the same scope as the manager or wider.
	 * 
	 * @param conversationStore the store to be injected
	 */
	public void setConversationStore(ConversationStore conversationStore) {
		this.conversationStore = conversationStore;
	}

	/**
	 * Returns the conversation resolver used to resolve the currently used
	 * conversation id. If the resolver itself has another scope than singleton,
	 * this method must be injected.
	 * 
	 * @return the conversation resolver
	 */
	public ConversationResolver getConversationResolver() {
		return conversationResolver;
	}

	/**
	 * Inject the conversation resolver, if the method
	 * {@link #getConversationResolver()} is not injected and if the resolver
	 * has singleton scope.
	 * 
	 * @param conversationResolver the resolver to be injected
	 */
	public void setConversationResolver(ConversationResolver conversationResolver) {
		this.conversationResolver = conversationResolver;
	}
}
