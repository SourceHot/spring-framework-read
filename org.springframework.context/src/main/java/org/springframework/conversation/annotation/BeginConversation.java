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

import org.springframework.conversation.Conversation;
import org.springframework.conversation.JoinMode;

/**
 * This annotation can be placed on any method to start a new conversation. This
 * has the same effect as invoking
 * {@link ConversationManager#beginConversation(boolean, JoinMode)} using
 * <code>false</code> for the temporary mode and the join mode as being
 * specified within the annotation or {@link JoinMode#NEW} as the default.<br/>
 * The new conversation is always long running (not a temporary one) and is
 * ended by either manually invoke
 * {@link ConversationManager#endCurrentConversation()}, invoking the
 * {@link Conversation#end()} method on the conversation itself or by placing
 * the {@link EndConversation} annotation on a method.<br/>
 * The new conversation is created BEFORE the method itself is invoked as a
 * before-advice.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface BeginConversation {
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
	JoinMode value() default JoinMode.NEW;
}
