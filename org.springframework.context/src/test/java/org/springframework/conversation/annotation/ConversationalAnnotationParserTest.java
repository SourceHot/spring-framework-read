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
import org.springframework.conversation.ConversationEndingType;
import org.springframework.conversation.JoinMode;
import org.springframework.conversation.interceptor.ConversationAttribute;
import org.springframework.util.ReflectionUtils;

import static org.junit.Assert.*;

/** @author Agim Emruli */
public class ConversationalAnnotationParserTest {

	@Test
	public void testParseConversationAnnotationWithDefaultSettings() throws Exception {
		Method method = ReflectionUtils.findMethod(ConversationalBean.class, "conversationalMethodWithDefaultSettings");
		ConversationalAnnotationParser parser = new ConversationalAnnotationParser();
		ConversationAttribute attribute = parser.parseConversationAnnotation(method);
		assertTrue("Conversational starts a conversation", attribute.shouldStartConversation());
		assertTrue("Conversational ends a conversation", attribute.shouldEndConversation());
		assertEquals("Conversational must have a default JoinMode", JoinMode.getDefaultJoinMode(),
				attribute.getJoinMode());
		assertEquals("Conversational must have a default ConversationEndingType", ConversationEndingType.SUCCESS,
				attribute.getConversationEndingType());
		assertEquals("Conversational must have a default timeout", ConversationDefinition.DEFAULT_TIMEOUT,
				attribute.getTimeout());
	}

	@Test
	public void testParseConversationAnnotationWithCustomSettings() throws Exception {
		Method method = ReflectionUtils.findMethod(ConversationalBean.class, "conversationalWithCustomSettings");
		ConversationalAnnotationParser parser = new ConversationalAnnotationParser();
		ConversationAttribute attribute = parser.parseConversationAnnotation(method);
		assertTrue("Conversational starts a conversation", attribute.shouldStartConversation());
		assertTrue("Conversational ends a conversation", attribute.shouldEndConversation());
		assertEquals("Conversational must have the custom JoinMode", JoinMode.ISOLATED, attribute.getJoinMode());
		assertEquals("Conversational must have the custom ConversationEndingType", ConversationEndingType.CANCEL,
				attribute.getConversationEndingType());
		assertEquals("Conversational must have the custom timeout", 11, attribute.getTimeout());
	}

	@Test
	public void testNonConversationalMethod() throws Exception {
		Method method = ReflectionUtils.findMethod(ConversationalBean.class, "nonConversationalMethod");
		ConversationalAnnotationParser parser = new ConversationalAnnotationParser();
		assertNull("No ConversationAttribute available for non-annotated method",
				parser.parseConversationAnnotation(method));
	}

	@SuppressWarnings({"UnusedDeclaration"})
	private static class ConversationalBean {

		@Conversational
		void conversationalMethodWithDefaultSettings() {
		}

		@Conversational(timeout = 11, joinMode = JoinMode.ISOLATED, endingType = ConversationEndingType.CANCEL)
		void conversationalWithCustomSettings() {
		}

		void nonConversationalMethod() {
		}
	}
}
