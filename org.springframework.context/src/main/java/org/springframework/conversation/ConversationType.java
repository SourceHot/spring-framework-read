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

import org.springframework.conversation.manager.ConversationInitializationCallback;

/**
 * The conversation type passed along to any callback while initializing a new
 * conversation. Its implementation is usually specific to the conversations
 * used within the system and only necessary for implementations of the
 * {@link ConversationInitializationCallback} interface to distinguish which
 * entities to be merged / reloaded by the initialization of a new conversation
 * according the type of conversation (like a use case) being started.<br/>
 * 
 * A good practice is creating an enumeration implementing this interface as the
 * conversation type.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public interface ConversationType {
	/**
	 * Returns the id of the conversation type which must be unique within the
	 * same system.
	 * 
	 * @return the id of this conversation type
	 */
	public String getId();
}
