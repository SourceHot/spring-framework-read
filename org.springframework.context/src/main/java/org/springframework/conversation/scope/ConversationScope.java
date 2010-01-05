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

import org.springframework.beans.factory.config.Scope;

/**
 * This interface is implemented by the conversation scope used to hold and
 * expose conversation scoped beans. It will be registered as a custom scope
 * within the application context.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public interface ConversationScope extends Scope {
	/** The name of the scope being exposed within the application context. */
	public static final String CONVERSATION_SCOPE_NAME = "conversation";

	/**
	 * The name of the contextual object for the conversation manager (see
	 * {@link Scope#resolveContextualObject(String)}).
	 */
	public static final String REFERENCE_CONVERSATION_MANAGER = "conversationManager";

	/**
	 * The name of the contextual object for the conversation store (see
	 * {@link Scope#resolveContextualObject(String)}).
	 */
	public static final String REFERENCE_CONVERSATION_STORE = "conversationStore";

	/**
	 * The name of the contextual object for the conversation resolver (see
	 * {@link Scope#resolveContextualObject(String)}).
	 */
	public static final String REFERENCE_CONVERSATION_RESOLVER = "conversationResolver";
}
