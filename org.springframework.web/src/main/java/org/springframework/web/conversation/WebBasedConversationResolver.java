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
package org.springframework.web.conversation;

import org.springframework.conversation.Conversation;
import org.springframework.conversation.scope.ConversationResolver;
import org.springframework.conversation.scope.ThreadAttachedConversationResolver;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * A web environment based implementation for the {@link ConversationResolver},
 * extending the {@link ThreadAttachedConversationResolver} used as a fallback
 * in the case the conversation management is used in a non web thread.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public class WebBasedConversationResolver extends
		ThreadAttachedConversationResolver {
	/**
	 * The name of the attribute to store the current conversation id within the
	 * configured scope.
	 */
	public static final String CURRENT_CONVERSATION_ID_NAME = Conversation.class
			.getName()
			+ ".Id";

	/**
	 * Flag, whether to use the thread attached fallback for non web threads or
	 * not. If disabled and used from within a non web thread will raise an
	 * exception.
	 */
	private boolean useFallback;

	/**
	 * The scope where the current conversation id should be stored in, refers
	 * to the scope options given by {@link RequestAttributes}.
	 */
	private int scope;

	/**
	 * Default constructor, initializing the scope to
	 * {@link RequestAttributes#SCOPE_GLOBAL_SESSION} and setting fallback
	 * strategy to <code>true</code>.
	 */
	public WebBasedConversationResolver() {
		scope = RequestAttributes.SCOPE_GLOBAL_SESSION;
		useFallback = true;
	}

	/**
	 * @see org.springframework.conversation.scope.ThreadAttachedConversationResolver#getCurrentConversationId()
	 */
	@Override
	public String getCurrentConversationId() {
		try {
			RequestAttributes attributes = RequestContextHolder
					.currentRequestAttributes();
			return (String) attributes.getAttribute(
					CURRENT_CONVERSATION_ID_NAME, getScope());
		} catch (IllegalStateException e) {
			if (useFallback) {
				return super.getCurrentConversationId();
			} else {
				throw e;
			}
		}
	}

	/**
	 * @see org.springframework.conversation.scope.ThreadAttachedConversationResolver#setCurrentConversationId(java.lang.String)
	 */
	@Override
	public void setCurrentConversationId(String conversationId) {
		try {
			RequestAttributes attributes = RequestContextHolder
					.currentRequestAttributes();
			attributes.setAttribute(CURRENT_CONVERSATION_ID_NAME,
					conversationId, getScope());
		} catch (IllegalStateException e) {
			if (useFallback) {
				super.setCurrentConversationId(conversationId);
			} else {
				throw e;
			}
		}
	}

	/**
	 * Configure whether to use the thread attached fallback if the conversation
	 * management is used from within a non web thread. If this is set to
	 * <code>true</code>, this resolver will attach and resolve the current
	 * conversation id to the current thread.
	 * 
	 * @param useFallback
	 *            the useFallback to set
	 */
	public void setUseFallback(boolean useFallback) {
		this.useFallback = useFallback;
	}

	/**
	 * @return whether the thread attached fallback should be used or not
	 */
	public boolean isUseFallback() {
		return useFallback;
	}

	/**
	 * Set the scope where the current conversation id will be stored with,
	 * refers to {@link RequestAttributes} and defaults to
	 * {@link RequestAttributes#SCOPE_GLOBAL_SESSION}.
	 * 
	 * @param scope
	 *            the scope to be used for storing the current conversation id
	 */
	public void setScope(int scope) {
		this.scope = scope;
	}

	/**
	 * @return the scope where the current conversation id should be stored in
	 */
	public int getScope() {
		return scope;
	}
}
