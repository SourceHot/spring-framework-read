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
package org.springframework.conversation.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.conversation.Conversation;
import org.springframework.conversation.ConversationEndingType;
import org.springframework.conversation.manager.ConversationManager;

/**
 * The advice used to handle the {@link BeginConversation}, the
 * {@link EndConversation} and the {@link Conversational} annotations. It
 * declares an AspectJ joinpoint to define where to weave the advice.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
@Aspect
public class ConversationAdvice {
	/***
	 * The conversation manager injected into this advice used to handle
	 * conversations according the annotations handled by the advice.
	 */
	private ConversationManager conversationManager;

	/**
	 * A method used by AspectJ if this advice is used in conjunction with
	 * AspectJ and the {@link BeginConversation} annotation used as the
	 * joinpoint. It starts a new conversation BEFORE the method is invoked.
	 * 
	 * @param joinPoint the join point of the method being adviced
	 * @return the return value of the target method
	 * @throws Throwable is thrown on any exception during the invocation
	 */
	@Around(value = "execution(@org.springframework.conversation.annotation.BeginConversation * *(..)) && @annotation(beginConversation)", argNames = "beginConversation")
	public Object invokeBegin(ProceedingJoinPoint joinPoint, BeginConversation beginConversation) throws Throwable {
		// start a new conversation according to the meta information provided
		// with the annotation
		Conversation conversation = getConversationManager().beginConversation(false, beginConversation.value());
		if (beginConversation.timeout() != -1) {
			conversation.setTimeout(beginConversation.timeout());
		}
		return joinPoint.proceed();
	}

	/**
	 * A method used by AspectJ if this advice is used in conjunction with
	 * AspectJ and the {@link EndConversation} annotation used as the joinpoint.
	 * It ends the current conversation AFTER the method has been invoked. If
	 * invoking the method threw an exception, the ending type given by the
	 * annotation is turned into a failure one.
	 * 
	 * @param joinPoint the join point of the method being adviced
	 * @return the return value of the target method
	 * @throws Throwable is thrown on any exception during the invocation
	 */
	@Around(value = "execution(@org.springframework.conversation.annotation.EndConversation * *(..)) && @annotation(endConversation)", argNames = "endConversation")
	public Object invokeEnd(ProceedingJoinPoint joinPoint, EndConversation endConversation) throws Throwable {
		try {
			// first invoke the advice method and if successful, end using the
			// success path, otherwise use the failure path
			Object returnValue = joinPoint.proceed();
			getConversationManager().endCurrentConversation(endConversation.value());
			return returnValue;
		} catch (Throwable th) {
			getConversationManager().endCurrentConversation(
					ConversationEndingType.getFailureType(endConversation.value()));
			throw th;
		}
	}

	/**
	 * A method used by AspectJ if this advice is used in conjunction with
	 * AspectJ and the {@link Conversational} annotation used as the joinpoint.
	 * It starts a new conversation BEFORE the method is invoked and it ends it
	 * AFTER the method has been invoked. This is the same as placing the
	 * {@link BeginConversation} and the {@link EndConversation} on the same
	 * method.
	 * 
	 * @param joinPoint the join point of the method being adviced
	 * @return the return value of the target method
	 * @throws Throwable is thrown on any exception during the invocation
	 */
	@Around(value = "execution(@org.springframework.conversation.annotation.Conversational * *(..)) && @annotation(conversational)", argNames = "conversational")
	public Object invokeConversational(ProceedingJoinPoint joinPoint, Conversational conversational) throws Throwable {
		Conversation conversation = null;
		try {
			// start a new conversation according to the meta information
			// provided with the annotation
			conversation = getConversationManager().beginConversation(false, conversational.joinMode());
			if (conversational.timeout() != -1) {
				conversation.setTimeout(conversational.timeout());
			}
			Object returnValue = joinPoint.proceed();

			// use the success path to end the conversation
			conversation.end(conversational.endingType());
			return returnValue;
		} catch (Throwable th) {
			if (conversation != null) {
				conversation.end(ConversationEndingType.getFailureType(conversational.endingType()));
			}
			throw th;
		}
	}

	/**
	 * @param conversationManager the conversation manager to be used by this
	 * advice
	 */
	@Required
	public void setConversationManager(ConversationManager conversationManager) {
		this.conversationManager = conversationManager;
	}

	/**
	 * @return the conversation manager
	 */
	public ConversationManager getConversationManager() {
		return conversationManager;
	}
}
