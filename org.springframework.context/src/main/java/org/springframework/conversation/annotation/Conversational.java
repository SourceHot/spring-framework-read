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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.conversation.ConversationEndingType;
import org.springframework.conversation.ConversationListener;
import org.springframework.conversation.JoinMode;
import org.springframework.conversation.manager.ConversationManager;

/**
 * If this annotation is placed on a method, it will create a new conversation
 * using the specified {@link JoinMode} or {@link JoinMode#NEW} as the default.<br/>
 * The new conversation is created BEFORE the method itself is invoked (as a
 * before-advice) and the conversation is always ended AFTER the method was
 * invoked (as an after-advice), no matter if it was successful or not.<br/>
 * Placing this annotation on a method has most likely the same effect as
 * placing {@link BeginConversation} and {@link EndConversation} the same time
 * on the same method.<br/>
 * If a method should run within its own, isolated conversation, this annotation
 * might be handy using join mode {@link JoinMode#ISOLATED} so that a new,
 * isolated conversation is started and ended while executing the method and
 * switched back to the already ongoing conversation after the method has been
 * terminated.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Conversational {
	/**
	 * Returns the {@link JoinMode} used to start a new conversation. See
	 * {@link JoinMode} for a more detailed description of the different modes
	 * available.<br/>
	 * Default join mode is {@link JoinMode#NEW} which will create a new root
	 * conversation and will automatically end the current one, if not ended
	 * before.
	 * 
	 * @return the join mode to use for creating a new conversation
	 */
	JoinMode joinMode() default JoinMode.NEW;

	/**
	 * Returns the timeout to be set within the newly created conversation,
	 * default is <code>-1</code> which means to use the default timeout as
	 * being configured on the {@link ConversationManager}. A value of
	 * <code>0</code> means there is no timeout any other positive value is
	 * interpreted as a timeout in milliseconds.
	 * 
	 * @return the timeout in milliseconds to be set on the new conversation
	 */
	long timeout() default -1;

	/**
	 * Returns the qualifier on how the conversation is about to be ended. This
	 * value will be passed on to any {@link ConversationListener} registered
	 * with the conversation being ended.
	 * 
	 * @return the type of ending, {@link ConversationEndingType#SUCCESS} if not
	 * explicitly specified
	 */
	ConversationEndingType endingType() default ConversationEndingType.SUCCESS;
}
