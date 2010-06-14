/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.conversation;

/**
 * Interface that defines all Spring-specific properties of a conversation. The underlying conversation system will
 * start and manage a conversation based on this definition. This definition can be built up from different sources like
 * annotations or xml meta-data.
 *
 * @author Agim Emruli
 */
public interface ConversationDefinition {

	/**
	 * The default timeout for a conversation, means there is no timeout defined for the conversation. It is up to the
	 * concrete conversation manager implementation to handle conversation without a timeout, like a indefinite
	 * conversation or some other system specific timeout
	 */
	int DEFAULT_TIMEOUT = -1;

	/**
	 * Returns the {@link JoinMode} used to start a new conversation. See {@link JoinMode} for a more detailed description
	 * of the different modes available.<br/> Default join mode is {@link JoinMode#NEW} which will create a new root
	 * conversation and will automatically end the current one, if not ended before.
	 *
	 * @return the join mode to use for creating a new conversation
	 */
	JoinMode getJoinMode();

	/**
	 * Returns the qualifier on how the conversation is about to be ended. This value will be passed on to any {@link
	 * ConversationListener} registered with the conversation being ended.
	 *
	 * @return the type of ending, {@link ConversationEndingType#SUCCESS} if not explicitly specified
	 */
	ConversationEndingType getConversationEndingType();

	/**
	 * Returns the timeout to be set within the newly created conversation, default is <code>-1</code> which means to use
	 * the default timeout as being configured on the {@link org.springframework.conversation.manager.ConversationManager}.
	 * A value of <code>0</code> means there is no timeout any other positive value is interpreted as a timeout in
	 * milliseconds.
	 *
	 * @return the timeout in milliseconds to be set on the new conversation
	 */
	int getTimeout();
}