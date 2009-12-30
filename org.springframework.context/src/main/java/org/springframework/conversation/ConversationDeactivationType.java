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
 * This enumeration supports the different types used on an deactivation event
 * for a conversation as a conversation can be deactivated in different ways.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public enum ConversationDeactivationType {
	/**
	 * The conversation was deactivated as there was another conversation
	 * manually switched to.
	 */
	SWITCHED,

	/**
	 * The conversation was deactivated as there was another newly, switched
	 * conversation created.
	 */
	NEW_SWITCHED,

	/**
	 * The conversation was deactivated as a new nested one was created and made
	 * active.
	 */
	NEW_NESTED,

	/**
	 * The conversation was deactivated as a new isolated one was created and
	 * made active.
	 */
	NEW_ISOLATED,
}
