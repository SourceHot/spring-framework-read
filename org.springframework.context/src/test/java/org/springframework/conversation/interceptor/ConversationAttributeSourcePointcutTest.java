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

package org.springframework.conversation.interceptor;

import java.lang.reflect.Method;

import org.junit.Test;

import org.springframework.conversation.annotation.Conversational;
import org.springframework.util.ReflectionUtils;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/** @author Agim Emruli */
public class ConversationAttributeSourcePointcutTest {

	@Test
	public void testMethodMatches() throws Exception {
		final ConversationAttributeSource source = createMock(ConversationAttributeSource.class);
		Method method = ReflectionUtils.findMethod(SimpleConversationalBean.class, "conversationalMethod");

		ConversationAttributeSourcePointcut pointcut = new ConversationAttributeSourcePointcut() {
			@Override
			protected ConversationAttributeSource getConversationAttributeSource() {
				return source;
			}
		};
		ConversationAttribute attribute = createMock(ConversationAttribute.class);
		expect(source.getConversationAttribute(method, SimpleConversationalBean.class)).andReturn(attribute).once();
		replay(source);
		assertTrue("Pointcut must match with ConversationAttribute",
				pointcut.matches(method, SimpleConversationalBean.class));
		verify(source);
	}

	@Test
	public void testMethodDoesNotMatch() throws Exception {
		final ConversationAttributeSource source = createMock(ConversationAttributeSource.class);
		Method method = ReflectionUtils.findMethod(SimpleConversationalBean.class, "conversationalMethod");

		ConversationAttributeSourcePointcut pointcut = new ConversationAttributeSourcePointcut() {
			@Override
			protected ConversationAttributeSource getConversationAttributeSource() {
				return source;
			}
		};
		expect(source.getConversationAttribute(method, SimpleConversationalBean.class)).andReturn(null).once();
		replay(source);
		assertFalse("Pointcut must not match without ConversationAttribute",
				pointcut.matches(method, SimpleConversationalBean.class));
		verify(source);
	}

	@Test
	public void testNoConversationAttributeSource() throws Exception {

		Method method = ReflectionUtils.findMethod(SimpleConversationalBean.class, "conversationalMethod");

		ConversationAttributeSourcePointcut pointcut = new ConversationAttributeSourcePointcut() {
			@Override
			protected ConversationAttributeSource getConversationAttributeSource() {
				return null;
			}
		};
		assertFalse("Pointcut must not match without ConversationAttributeSource",
				pointcut.matches(method, SimpleConversationalBean.class));
	}

	private static class SimpleConversationalBean {

		@Conversational
		private void conversationalMethod() {
		}
	}
}
