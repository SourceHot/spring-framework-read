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
 * @author Micha Kiener
 * @since 3.1
 */
public class ConversationEventAwareBean implements ConversationListener {
	private Conversation conversation1;
	private Conversation conversation2;
	private ConversationActivationType activationType;
	private ConversationDeactivationType deactivationType;
	private ConversationEndingType endingType;

	/**
	 * @see org.springframework.conversation.ConversationListener#conversationActivated(org.springframework.conversation.Conversation,
	 * org.springframework.conversation.Conversation,
	 * org.springframework.conversation.ConversationActivationType)
	 */
	public void conversationActivated(Conversation conversation, Conversation oldCurrentConversation,
			ConversationActivationType activationType) {
		conversation1 = conversation;
		conversation2 = oldCurrentConversation;
		this.activationType = activationType;
	}

	/**
	 * @see org.springframework.conversation.ConversationListener#conversationDeactivated(org.springframework.conversation.Conversation,
	 * org.springframework.conversation.Conversation,
	 * org.springframework.conversation.ConversationDeactivationType)
	 */
	public void conversationDeactivated(Conversation conversation, Conversation newCurrentConversation,
			ConversationDeactivationType deactivationType) {
		conversation1 = conversation;
		conversation2 = newCurrentConversation;
		this.deactivationType = deactivationType;
	}

	/**
	 * @see org.springframework.conversation.ConversationListener#conversationEnded(org.springframework.conversation.Conversation,
	 * org.springframework.conversation.ConversationEndingType)
	 */
	public void conversationEnded(Conversation conversation, ConversationEndingType endingType) {
		conversation1 = conversation;
		this.endingType = endingType;
	}

	public Conversation getConversation1() {
		return conversation1;
	}

	public Conversation getConversation2() {
		return conversation2;
	}

	public ConversationActivationType getActivationType() {
		return activationType;
	}

	public ConversationDeactivationType getDeactivationType() {
		return deactivationType;
	}

	public ConversationEndingType getEndingType() {
		return endingType;
	}
}
