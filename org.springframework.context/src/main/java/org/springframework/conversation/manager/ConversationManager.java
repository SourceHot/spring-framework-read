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
import org.springframework.conversation.ConversationEndingType;
import org.springframework.conversation.ConversationListener;
import org.springframework.conversation.ConversationType;
import org.springframework.conversation.JoinMode;
import org.springframework.conversation.annotation.BeginConversation;
import org.springframework.conversation.annotation.Conversational;
import org.springframework.conversation.annotation.EndConversation;

/**
 * <p> The interface to be implemented by any conversation manager. It is used by the advice behind the conversational
 * annotations like {@link BeginConversation}, {@link EndConversation} and {@link Conversational} but might be used for
 * even fine grained access to the underlying conversation management, if a initializer callback is needed for instance
 * or if conversation switching should be used. If used directly, the conversation manager can be injected into any bean
 * to be used as it is a singleton and thread safe.<br/>
 *
 * Conversations are a good way to scope beans and attributes depending on business logic boundary rather than a
 * technical boundary of a scope like session, request etc. Usually a conversation boundary is defined by the starting
 * point of a use case and ended accordingly or in other words a conversation defines the boundary for a unit of
 * work.<br/> <br/>
 *
 * Here is a short description on how to use the conversation management:<br/> <br/> <b>Starting a conversation</b><br/>
 * A conversation can be started in two different ways, either by placing the {@link BeginConversation} annotation on a
 * method defining the starting point of the conversation (use case) or by invoking {@link
 * ConversationManager#beginConversation(boolean, JoinMode)} manually through the conversation manager. The {@link
 * JoinMode} declares on how a new conversation is created (see its javadoc for more details on the different join
 * modes).<br/> <br/>
 *
 * <b>Ending a conversation</b><br/> A current conversation is ended by either placing the {@link EndConversation}
 * annotation on the ending method or by manually invoke the {@link ConversationManager#endCurrentConversation(ConversationEndingType)}
 * method. A current conversation might also be ended, if a new conversation is started having {@link JoinMode#NEW}
 * being declared where the current conversation is ended silently and a new one is created. This might be the obvious
 * way to end a conversation, if the end of a use case is not always forced to be invoked by a user.<br/> <br/>
 *
 * <b>Temporary conversations</b><br/> A temporary conversation is automatically created, if the container is about to
 * create a bean having conversation scope but there is no current conversation in progress, hence a new, temporary
 * conversation is created. A temporary conversation might be turned into a long running one by joining it or manually
 * invoke {@link Conversation#begin()}.<br/> <br/>
 *
 * <b>Initializing the conversation</b><br/> If there is any need to initialize beans or entities while starting a new
 * conversation, the {@link ConversationInitializationCallback} might be used to be invoked if the new conversation was
 * started in order to initialize it. This is done by providing such a callback to the {@link
 * ConversationManager#beginConversation(JoinMode, ConversationType, ConversationInitializationCallback...)} method. The
 * callback feature is not available through the annotation support and hence is only available through the conversation
 * manager API. Here is an example on such an initializing feature; if a conversation is used in conjunction of a JPA
 * entity manager or Hibernate session, there might be entity beans already loaded, cached or referenced within backing
 * beans which have to be merged into the entity manager or session in order to be used within the conversation's work.
 * So it would be possible to implement the callback, merging the necessary entities used within the conversation into
 * the entity manager.<br/> <br/>
 *
 * <b>Listening to conversation events</b><br/> If there is a need to listening to events of a conversation, add
 * yourself as a {@link ConversationListener} to a new {@link Conversation} as being returned by this {@link
 * ConversationManager}. The same goal can be achieved by implementing the {@link ConversationListener} interface on a
 * conversation scoped bean which will be registered automatically to receive events.<br/> <br/>
 *
 * <b>Nesting conversations</b><br/> Conversations might be nested either by inheriting the state of the parent ( {@link
 * JoinMode#NESTED}) or by isolating its state from the parent ( {@link JoinMode#ISOLATED}). Ending a nested
 * conversation automatically switches back to its parent making it the current conversation.<br/> <br/>
 *
 * <b>Where are conversations stored?</b><br/> Conversations are created by the {@link ConversationManager} and
 * registered within the {@link org.springframework.conversation.scope.ConversationScope}. The scope handler is
 * injected into the manager by Spring (statically as both beans are singletons) and registered as a custom scope. As
 * the scope handler is a singleton, it needs a store where conversations are stored within which is usually bound to
 * the current session or window of a user and represented by the {@link ConversationStore} interface. The store is
 * usually being injected into the scope handler using method injection as the store has a narrower scope than the scope
 * handler. In a web environment, the store would typically live in session scope to separate the conversations from
 * each of the sessions. There is always one current conversation either stored within the session or as a request
 * parameter passed along every request to set the current conversation and is accessed by the manager using a {@link
 * ConversationResolver}. </p>
 *
 * @author Micha Kiener
 * @since 3.1
 */
public interface ConversationManager {

	/**
	 * This method starts a new {@link Conversation} and registers it within the
	 * {@link org.springframework.conversation.scope.ConversationScope} to be exposed as the current conversation.
	 * 
	 * @param temporary flag indicating whether this new conversation is
	 * temporary (if invoked from elsewhere as the scope, it should always be
	 * <code>false</code>)
	 * @param joinMode the mode declaring on how to use an already existing
	 * conversation, if any
	 * @return the newly created conversation which may be a nested one
	 */
	Conversation beginConversation(boolean temporary, JoinMode joinMode);

	/**
	 * In addition to {@link #beginConversation(boolean, JoinMode)} this method
	 * provides the possibility of callbacks to be invoked after the
	 * conversation was created to be initialized. See
	 * {@link ConversationInitializationCallback} for more details about
	 * conversation initialization callbacks.
	 * 
	 * @param joinMode the mode declaring on how to use an already existing
	 * conversation, if any
	 * @param conversationType the optional type of conversation to be started,
	 * used to pass along to the callback(s)
	 * @param callbacks the optional callback(s) to be invoked after the new
	 * conversation has been created in exactly the same order they are passed
	 * in
	 * @return the newly created conversation which may be a nested one
	 * 
	 * @param <T> the conversation type passed along to distinguish the use case
	 * behind starting a new conversation
	 */
	<T extends ConversationType> Conversation beginConversation(JoinMode joinMode, T conversationType,
			ConversationInitializationCallback<T>... callbacks);

	/**
	 * Ends the given conversation. If it is a nested one, ends it and switches
	 * back to its parent to be the current conversation. This method is usually
	 * used to end a switched conversation which is not the current one. To end
	 * the current conversation, the method
	 * {@link #endCurrentConversation(ConversationEndingType)} might be used.
	 * 
	 * @param conversation the conversation to end
	 * @param endingType the type qualifying on how this conversation is to be
	 * ended (only passed on to any listeners, does not have an impact on the
	 * conversation manager)
	 * @throws IllegalStateException if the given conversation has a nested
	 * conversation which is not ended which is an illegal condition
	 */
	void endConversation(Conversation conversation, ConversationEndingType endingType);

	/**
	 * Ends the given conversation to the final root conversation, if it is a
	 * nested or joined one. In addition to
	 * {@link #endConversation(Conversation, ConversationEndingType)} this one
	 * recursively invokes itself until the root conversation has been ended. If
	 * the given conversation is not the tail of the conversation, the most
	 * nested one is searched and ended too.<br/>
	 * Use this method with caution as this could break the chain of nested
	 * conversations.
	 * 
	 * @param conversation the conversation to be ended finally
	 * @param endingType the type qualifying on how this conversation is to be
	 * ended (only passed on to any listeners, does not have an impact on the
	 * conversation manager)
	 */
	void finalEndConversation(Conversation conversation, ConversationEndingType endingType);

	/**
	 * Returns the current conversation, if any available, <code>null</code>
	 * otherwise. Does not start a new temporary transaction, if none available.
	 * This method delegates to the {@link ConversationResolver} and the
	 * {@link ConversationStore} to finally get the currently used conversation.
	 * 
	 * @return the current conversation if any, <code>null</code> otherwise
	 */
	Conversation getCurrentConversation();

	/**
	 * Ends the current conversation, if any is in progress. This is the same as
	 * ending the conversation returned by {@link #getCurrentConversation()}
	 * using {@link #endConversation(Conversation, ConversationEndingType)}.
	 * 
	 * @param endingType the type qualifying on how this conversation is to be
	 * ended (only passed on to any listeners, does not have an impact on the
	 * conversation manager)
	 * @return the current conversation which has been ended
	 */
	Conversation endCurrentConversation(ConversationEndingType endingType);

	/**
	 * Returns a list of available conversations within the current store.
	 * Depending on the setup of the {@link ConversationStore}, this will return
	 * all conversations available for the current session if the store is
	 * stored within the session for instance.
	 * 
	 * @return the list of available conversations or an empty list, never
	 * <code>null</code>
	 */
	List<Conversation> getConversations();

	/**
	 * Switches to the given conversation, making it the current one, however,
	 * the previously conversation is not removed.
	 * 
	 * @param conversationId the id of the conversation to be switched to,
	 * making it the current one
	 * @return the conversation object switched to, if the given id was actually
	 * available, <code>null</code>, if the conversation was not found and hence
	 * the old current conversation remains the same
	 */
	Conversation switchConversation(String conversationId);

	/**
	 * Returns the timeout to be set on a newly created conversation by default
	 * whereas <code>0</code> means no timeout and any positive value represents
	 * the timeout in milliseconds.
	 * 
	 * @return the default timeout in milliseconds for a new conversation
	 */
	int getDefaultConversationTimeout();

	/**
	 * Set the default timeout to be set on newly created conversations, if not
	 * overwritten by the conversation itself. A value of <code>0</code> means
	 * no timeout at all.
	 * 
	 * @param timeout the timeout in milliseconds or <code>0</code> if no
	 * timeout should be set by default
	 */
	void setDefaultConversationTimeout(int timeout);
}
