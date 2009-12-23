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

import org.springframework.conversation.Conversation;
import org.springframework.conversation.ConversationType;

/**
 * This callback interface may be implemented by any bean or component to be
 * invoked after a new conversation has been started. Usually such a callback is
 * used to implement the merging / reloading necessary by the start of a new
 * conversation to align with the underlying entity manager scoped to the
 * boundaries of a conversation.<br/>
 * For instance, assume there is a list of entities where a new conversation is
 * started by selecting one of it to start a use case. Obviously the entity was
 * loaded using another entity manager as the new one being bound to the
 * conversation and hence it has to be merged into the persistence context to
 * become a managed entity. Thats where the initialization callback comes in
 * place.
 * 
 * @author Micha Kiener
 * 
 * @param <T> the conversation type passed along to distinguish the use case
 * behind starting a new conversation
 */
public interface ConversationInitializationCallback<T extends ConversationType> {
	/**
	 * Invoked by the {@link ConversationManager} after a new conversation has
	 * been started to basically initialize the conversation.<br/>
	 * 
	 * The conversation type is optional and might be used to distinguish the
	 * type of use case represented by the new conversation.
	 * 
	 * @param conversation the conversation which has been started
	 * @param type the optional conversation type representing the use case this
	 * conversation was created for
	 */
	void conversationStarted(Conversation conversation, T type);

}
