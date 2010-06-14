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

import org.springframework.aop.Pointcut;
import org.springframework.util.ReflectionUtils;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/** @author Agim Emruli */
public class BeanFactoryConversationAttributeSourceAdvisorTest {

	@Test
	public void testPointcutSetup() {
		ConversationAttributeSource source = createMock(ConversationAttributeSource.class);
		BeanFactoryConversationAttributeSourceAdvisor advisor = new BeanFactoryConversationAttributeSourceAdvisor();
 		advisor.setConversationAttributeSource(source);

		Pointcut pointcut = advisor.getPointcut();
		assertNotNull("Pointcut can't be null",pointcut);

		Method method = ReflectionUtils.findMethod(SimpleConversationalClass.class, "conversationalMethod");

		ConversationAttribute attribute = createMock(ConversationAttribute.class);
		expect(source.getConversationAttribute(method,SimpleConversationalClass.class)).andReturn(attribute);
		replay(source);
		assertTrue("Pointcut must match",pointcut.getMethodMatcher().matches(method,SimpleConversationalClass.class));
		assertTrue("Pointcut must match",pointcut.getClassFilter().matches(SimpleConversationalClass.class));
		verify(source);
	}


	@SuppressWarnings({"UnusedDeclaration"})
	private static class SimpleConversationalClass{
		void conversationalMethod(){
		}
	}

}
