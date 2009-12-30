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

/**
 * The conversation listener can be registered within a {@link Conversation} in
 * order to be invoked for special conversation events. If the listener
 * interface is implemented on a conversation scoped bean, the bean will be
 * registered with its conversation automatically through the conversation
 * manager.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public interface ConversationListener {
	/**
	 * Invoked by the conversation manager if the given conversation was ended.
	 * The additional status object contains further information like how the
	 * conversation was ended for instance.
	 * 
	 * @param conversation the conversation being ended
	 * @param endingType the type qualifying how the conversation was ended
	 */
	void conversationEnded(Conversation conversation, ConversationEndingType endingType);

	/**
	 * Invoked by the conversation manager if the given conversation was made
	 * active. This is called if a new conversation was created and made active,
	 * if a conversation was switched to or if a nested one ended and its parent
	 * was made active.
	 * 
	 * @param conversation the conversation made active
	 * @param oldCurrentConversation the old current conversation, if available,
	 * <code>null</code> otherwise
	 * @param activationType the type of activation
	 */
	void conversationActivated(Conversation conversation, Conversation oldCurrentConversation,
			ConversationActivationType activationType);

	/**
	 * Invoked by the conversation manager if the given conversation was
	 * deactivated. This is called if a conversation was switched to or if a
	 * nested child conversation was created.
	 * 
	 * @param conversation the conversation which has been deactivated
	 * @param newCurrentConversation the conversation made the current one
	 * @param deactivationType the type of deactivation
	 */
	void conversationDeactivated(Conversation conversation, Conversation newCurrentConversation,
			ConversationDeactivationType deactivationType);

	// TODO: add more event-based methods
}
