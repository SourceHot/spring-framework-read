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
import org.springframework.conversation.interceptor.ConversationAttribute;
import org.springframework.util.ReflectionUtils;

import static org.junit.Assert.*;

/** @author Agim Emruli */
public class EndConversationAnnotationParserTest {

	@Test
	public void testParseConversationAnnotationWithDefaultSettings() throws Exception {
		Method method = ReflectionUtils.findMethod(ConversationalBean.class, "endConversationWithDefaultSettings");
		EndConversationAnnotationParser parser = new EndConversationAnnotationParser();
		ConversationAttribute attribute = parser.parseConversationAnnotation(method);
		assertFalse("EndConversation should not start a conversation", attribute.shouldStartConversation());
		assertTrue("EndConversation should end a conversation", attribute.shouldEndConversation());
		assertNull("EndConversation must not have a default JoinMode", attribute.getJoinMode());
		assertEquals("EndConversation must have a default ConversationEndingType", ConversationEndingType.SUCCESS,
				attribute.getConversationEndingType());
		assertEquals("EndConversation must have a default timeout", ConversationDefinition.DEFAULT_TIMEOUT,
				attribute.getTimeout());
	}

	@Test
	public void testParseConversationAnnotationWithCustomSettings() throws Exception {
		Method method = ReflectionUtils.findMethod(ConversationalBean.class, "endConversationWithCustomSettings");
		EndConversationAnnotationParser parser = new EndConversationAnnotationParser();
		ConversationAttribute attribute = parser.parseConversationAnnotation(method);
		assertEquals("EndConversation must have a custom ConversationEndingType", ConversationEndingType.CANCEL,
				attribute.getConversationEndingType());
	}

	@Test
	public void testNonConversationalMethod() throws Exception {
		Method method = ReflectionUtils.findMethod(ConversationalBean.class, "nonConversationalMethod");
		EndConversationAnnotationParser parser = new EndConversationAnnotationParser();
		assertNull("No ConversationAttribute available for non-annotated method",
				parser.parseConversationAnnotation(method));
	}

	@SuppressWarnings({"UnusedDeclaration"})
	private static class ConversationalBean {

		@EndConversation
		void endConversationWithDefaultSettings() {
		}

		@EndConversation(value = ConversationEndingType.CANCEL)
		void endConversationWithCustomSettings() {
		}

		void nonConversationalMethod() {
		}
	}

}
