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

package org.springframework.conversation.annotation;

import java.lang.reflect.Method;

import org.junit.Test;

import org.springframework.conversation.ConversationDefinition;
import org.springframework.conversation.JoinMode;
import org.springframework.conversation.interceptor.ConversationAttribute;
import org.springframework.util.ReflectionUtils;

import static org.junit.Assert.*;

/** @author Agim Emruli */
public class BeginConversationAnnotationParserTest {

	@Test
	public void testParseConversationAnnotationWithDefaultSettings() throws Exception {
		Method method = ReflectionUtils.findMethod(ConversationalBean.class, "beginConversationWithDefaultSettings");
		BeginConversationAnnotationParser parser = new BeginConversationAnnotationParser();
		ConversationAttribute attribute = parser.parseConversationAnnotation(method);
		assertTrue("BeginConversation starts a conversation", attribute.shouldStartConversation());
		assertFalse("BeginConversation does not end a conversation", attribute.shouldEndConversation());
		assertEquals("Default JoinMode set", JoinMode.getDefaultJoinMode(), attribute.getJoinMode());
		assertNull("BeginConversation does not have a ending type", attribute.getConversationEndingType());
		assertEquals("Default Timeout if no timeout set", ConversationDefinition.DEFAULT_TIMEOUT,
				attribute.getTimeout());
	}

	@Test
	public void testParseConversationAnnotationWithCustomSettings() throws Exception {
		Method method = ReflectionUtils.findMethod(ConversationalBean.class, "beginConversationWithCustomSettings");
		BeginConversationAnnotationParser parser = new BeginConversationAnnotationParser();
		ConversationAttribute attribute = parser.parseConversationAnnotation(method);
		assertTrue("BeginConversation starts a conversation", attribute.shouldStartConversation());
		assertFalse("BeginConversation does not end a conversation", attribute.shouldEndConversation());
		assertNull("BeginConversation does not have a ending type", attribute.getConversationEndingType());

		assertEquals("Custom JoinMode must be set", JoinMode.ISOLATED, attribute.getJoinMode());
		assertEquals("Custom timeout  must be set", 11, attribute.getTimeout());
	}

	@Test
	public void testNonConversationalMethod() throws Exception {
		Method method = ReflectionUtils.findMethod(ConversationalBean.class, "nonConversationalMethod");
		BeginConversationAnnotationParser parser = new BeginConversationAnnotationParser();
		assertNull("No ConversationAttribute available for non-annotated method", parser.parseConversationAnnotation(method));
	}

	@SuppressWarnings({"UnusedDeclaration"})
	private static class ConversationalBean {

		@BeginConversation
		void beginConversationWithDefaultSettings() {
		}

		@BeginConversation(timeout = 11, value = JoinMode.ISOLATED)
		void beginConversationWithCustomSettings() {
		}

		void nonConversationalMethod() {
		}
	}
}