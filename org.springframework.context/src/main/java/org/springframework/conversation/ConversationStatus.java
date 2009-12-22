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
 * The conversation status represents the current state of a conversation. It is
 * most likely used within the {@link ConversationListener} for additional
 * information about a conversation event. It represents the current state in a
 * static way, said that, it must not be referenced as the state could actually
 * change and will not be reflected within this state object.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public interface ConversationStatus {

	// TODO: add state access methods
}
