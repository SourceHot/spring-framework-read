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

package org.springframework.conversation.interceptor;

import org.springframework.conversation.ConversationEndingType;
import org.springframework.conversation.JoinMode;

/**
 * Default implementation of the ConversationAttribute used by the conversation system.
 *
 * @author Agim Emruli
 */
public class DefaultConversationAttribute implements ConversationAttribute {

	private final boolean shouldStartConversation;

	private final boolean shouldEndConversation;

	private final JoinMode joinMode;

	private final ConversationEndingType conversationEndingType;

	private int timeout = DEFAULT_TIMEOUT;

	private DefaultConversationAttribute(boolean startConversation,
			boolean endConversation,
			JoinMode mode,
			ConversationEndingType type,
			int timeout) {
		shouldStartConversation = startConversation;
		shouldEndConversation = endConversation;
		joinMode = mode;
		conversationEndingType = type;
		this.timeout = timeout;
	}

	public DefaultConversationAttribute(JoinMode joinMode, int timeout) {
	    this(true,false,joinMode,null,timeout);
	}

	public DefaultConversationAttribute(ConversationEndingType endingType) {
	    this(false,true,null,endingType,DEFAULT_TIMEOUT);
	}

	public DefaultConversationAttribute(JoinMode joinMode, int timeout, ConversationEndingType endingType) {
	    this(true,true,joinMode,endingType,timeout);
	}

	public boolean shouldStartConversation() {
		return shouldStartConversation;
	}

	public boolean shouldEndConversation() {
		return shouldEndConversation;
	}

	public JoinMode getJoinMode() {
		return joinMode;
	}

	public ConversationEndingType getConversationEndingType() {
		return conversationEndingType;
	}

	public int getTimeout() {
		return timeout;
	}
}