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

import org.springframework.conversation.annotation.BeginConversation;
import org.springframework.conversation.annotation.Conversational;
import org.springframework.conversation.annotation.EndConversation;
import org.springframework.conversation.manager.ConversationManager;

/**
 * A test service bean using the different conversation annotations.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public class ConversationalServiceBeanImpl implements ConversationalServiceBean {
	private ConversationManager manager;

	private Conversation startingConversation;
	private Conversation endingConversation;
	private Conversation conversationalConversation;

	@BeginConversation
	public void startConversation() {
		startingConversation = manager.getCurrentConversation();
	}

	@EndConversation
	public void endConversation() {
		endingConversation = manager.getCurrentConversation();
	}

	@Conversational
	public void doInConversation() {
		conversationalConversation = manager.getCurrentConversation();
	}

	public Conversation getStartingConversation() {
		return startingConversation;
	}

	public Conversation getEndingConversation() {
		return endingConversation;
	}

	public Conversation getConversationalConversation() {
		return conversationalConversation;
	}

	public void clean() {
		startingConversation = null;
		endingConversation = null;
		conversationalConversation = null;
	}

	public void setConversationManager(ConversationManager manager) {
		this.manager = manager;
	}
}
