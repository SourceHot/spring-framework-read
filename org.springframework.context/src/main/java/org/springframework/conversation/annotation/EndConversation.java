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
import org.springframework.conversation.manager.ConversationManager;

/**
 * This annotation can be placed on a method to end the current conversation. It
 * has the same effect as a manual invocation of
 * {@link ConversationManager#endCurrentConversation(ConversationEndingType)}.<br/>
 * The conversation is ended AFTER the method was invoked as an after-advice.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface EndConversation {
	/**
	 * Returns the qualifier on how the conversation is about to be ended. This
	 * value will be passed on to any {@link ConversationListener} registered
	 * with the conversation being ended.
	 * 
	 * @return the type of ending, {@link ConversationEndingType#SUCCESS} if not
	 * explicitly specified
	 */
	ConversationEndingType value() default ConversationEndingType.SUCCESS;
}
