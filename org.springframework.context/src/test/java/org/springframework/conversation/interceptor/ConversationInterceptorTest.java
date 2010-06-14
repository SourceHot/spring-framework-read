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

import org.aopalliance.intercept.MethodInvocation;
import org.junit.Before;
import org.junit.Test;

import org.springframework.conversation.Conversation;
import org.springframework.conversation.ConversationEndingType;
import org.springframework.conversation.JoinMode;
import org.springframework.conversation.manager.ConversationManager;
import org.springframework.util.ReflectionUtils;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/** @author Agim Emruli */
public class ConversationInterceptorTest {

	private ConversationManager conversationManager;
	private ConversationAttributeSource conversationAttributeSource;
	private ConversationInterceptor conversationInterceptor;

	@Before
	public void setupInterceptor(){
		conversationManager = createMock(ConversationManager.class);
		conversationAttributeSource = createMock(ConversationAttributeSource.class);
		conversationInterceptor = new ConversationInterceptor();
		conversationInterceptor.setConversationAttributeSource(conversationAttributeSource);
		conversationInterceptor.setConversationManager(conversationManager);
	}

	@Test
	public void testStartConversationWithDefaultTimeout() throws Throwable {
		MethodInvocation invocation = createMock(MethodInvocation.class);

		Method method = ReflectionUtils.findMethod(SimpleConversationalBean.class,"conversationalMethod");
		ConversationAttribute attribute = createMock(ConversationAttribute.class);
		Conversation conversation = createMock(Conversation.class);

		//figure out the class of the invocation
		expect(invocation.getThis()).andReturn(new SimpleConversationalBean()).times(2);

		//get the method of the invocation
		expect(invocation.getMethod()).andReturn(method);

		//get meta-data for the invocation
		expect(conversationAttributeSource.getConversationAttribute(method,SimpleConversationalBean.class)).andReturn(attribute);

		//return that interceptor should start a conversation
		expect(attribute.shouldStartConversation()).andReturn(true);

		//conversation timeout
		expect(attribute.getTimeout()).andReturn(ConversationAttribute.DEFAULT_TIMEOUT);

		//join mode needed to start the conversation
		expect(attribute.getJoinMode()).andReturn(JoinMode.getDefaultJoinMode());

		//start the conversation
		expect(conversationManager.beginConversation(false, JoinMode.getDefaultJoinMode())).andReturn(conversation);

		//proceed with the method call
		expect(invocation.proceed()).andReturn("result");

		//don't end conversation
		expect(attribute.shouldEndConversation()).andReturn(false);		

		replay(conversationAttributeSource,conversationManager,attribute,invocation,conversation);
		Object result = conversationInterceptor.invoke(invocation);
		assertEquals("Return value must be the same from target","result",result);
		verify(conversationAttributeSource,conversationManager,attribute,invocation,conversation);
	}

	@Test
	public void testStartConversationWithCustomTimeout() throws Throwable {
		MethodInvocation invocation = createMock(MethodInvocation.class);

		Method method = ReflectionUtils.findMethod(SimpleConversationalBean.class,"conversationalMethod");
		ConversationAttribute attribute = createMock(ConversationAttribute.class);
		Conversation conversation = createMock(Conversation.class);

		//figure out the class of the invocation
		expect(invocation.getThis()).andReturn(new SimpleConversationalBean()).times(2);

		//get the method of the invocation
		expect(invocation.getMethod()).andReturn(method);

		//get meta-data for the invocation
		expect(conversationAttributeSource.getConversationAttribute(method,SimpleConversationalBean.class)).andReturn(attribute);

		//return that interceptor should start a conversation
		expect(attribute.shouldStartConversation()).andReturn(true);

		//conversation timeout
		expect(attribute.getTimeout()).andReturn(1000).times(2);

		//join mode needed to start the conversation
		expect(attribute.getJoinMode()).andReturn(JoinMode.getDefaultJoinMode());

		//start the conversation
		expect(conversationManager.beginConversation(false, JoinMode.getDefaultJoinMode())).andReturn(conversation);

		//set time out
		conversation.setTimeout(1000);
		expectLastCall();

		//proceed with the method call
		expect(invocation.proceed()).andReturn("result");

		//don't end conversation
		expect(attribute.shouldEndConversation()).andReturn(false);

		replay(conversationAttributeSource,conversationManager,attribute,invocation,conversation);
		Object result = conversationInterceptor.invoke(invocation);
		assertEquals("Return value must be the same from target","result",result);
		verify(conversationAttributeSource,conversationManager,attribute,invocation,conversation);
	}

	@Test
	public void testEndConversation() throws Throwable {
		MethodInvocation invocation = createMock(MethodInvocation.class);

		Method method = ReflectionUtils.findMethod(SimpleConversationalBean.class,"conversationalMethod");
		ConversationAttribute attribute = createMock(ConversationAttribute.class);
		Conversation conversation = createMock(Conversation.class);

		//figure out the class of the invocation
		expect(invocation.getThis()).andReturn(new SimpleConversationalBean()).times(2);

		//get the method of the invocation
		expect(invocation.getMethod()).andReturn(method);

		//get meta-data for the invocation
		expect(conversationAttributeSource.getConversationAttribute(method,SimpleConversationalBean.class)).andReturn(attribute);

		//return that interceptor should start a conversation
		expect(attribute.shouldStartConversation()).andReturn(false);

		//proceed with the method call
		expect(invocation.proceed()).andReturn("result");

		//don't end conversation
		expect(attribute.shouldEndConversation()).andReturn(true);

		//ending type
		expect(attribute.getConversationEndingType()).andReturn(ConversationEndingType.SUCCESS);

		expect(conversationManager.endCurrentConversation(ConversationEndingType.SUCCESS)).andReturn(conversation);

		replay(conversationAttributeSource,conversationManager,attribute,invocation,conversation);
		Object result = conversationInterceptor.invoke(invocation);
		assertEquals("Return value must be the same from target","result",result);
		verify(conversationAttributeSource,conversationManager,attribute,invocation,conversation);
	}

	@Test
	public void testEndConversationAfterFailure() throws Throwable {
		MethodInvocation invocation = createMock(MethodInvocation.class);

		Method method = ReflectionUtils.findMethod(SimpleConversationalBean.class,"conversationalMethod");
		ConversationAttribute attribute = createMock(ConversationAttribute.class);
		Conversation conversation = createMock(Conversation.class);

		//figure out the class of the invocation
		expect(invocation.getThis()).andReturn(new SimpleConversationalBean()).times(2);

		//get the method of the invocation
		expect(invocation.getMethod()).andReturn(method);

		//get meta-data for the invocation
		expect(conversationAttributeSource.getConversationAttribute(method,SimpleConversationalBean.class)).andReturn(attribute);

		//return that interceptor should start a conversation
		expect(attribute.shouldStartConversation()).andReturn(false);

		@SuppressWarnings({"ThrowableInstanceNeverThrown"}) Exception thrownException = new Exception();

		//proceed with the method call
		expect(invocation.proceed()).andThrow(thrownException);

		//ending type
		expect(attribute.getConversationEndingType()).andReturn(ConversationEndingType.SUCCESS);

		expect(conversationManager.endCurrentConversation(ConversationEndingType.FAILURE_SUCCESS)).andReturn(conversation);

		replay(conversationAttributeSource,conversationManager,attribute,invocation,conversation);

		try {
			conversationInterceptor.invoke(invocation);
		}
		catch (Exception exception) {
			assertSame("Thrown Exception from target must be the thrown one",thrownException,exception);
		}
		verify(conversationAttributeSource,conversationManager,attribute,invocation,conversation);
	}

	@Test
	public void testMethodWithNoConversationDefinition() throws Throwable {
		MethodInvocation invocation = createMock(MethodInvocation.class);

		Method method = ReflectionUtils.findMethod(SimpleConversationalBean.class,"conversationalMethod");

		expect(invocation.getThis()).andReturn(new SimpleConversationalBean()).times(2);

		expect(invocation.getMethod()).andReturn(method);

		expect(conversationAttributeSource.getConversationAttribute(method,SimpleConversationalBean.class)).andReturn(null);

		expect(invocation.proceed()).andReturn("result");

		replay(conversationAttributeSource,conversationManager,invocation);

		Object result = conversationInterceptor.invoke(invocation);
		assertEquals("Return value must be the same from target","result",result);
		verify(conversationAttributeSource,conversationManager,invocation);
	}

	@Test
	public void testMethodInvocationWithNotClassInformation() throws Throwable {
		MethodInvocation invocation = createMock(MethodInvocation.class);

		Method method = ReflectionUtils.findMethod(SimpleConversationalBean.class,"conversationalMethod");

		expect(invocation.getThis()).andReturn(null);

		expect(invocation.getMethod()).andReturn(method);

		expect(conversationAttributeSource.getConversationAttribute(method,null)).andReturn(null);

		expect(invocation.proceed()).andReturn("result");

		replay(conversationAttributeSource,conversationManager,invocation);

		Object result = conversationInterceptor.invoke(invocation);
		assertEquals("Return value must be the same from target","result",result);
		verify(conversationAttributeSource,conversationManager,invocation);
	}


	@SuppressWarnings({"UnusedDeclaration"})
	private static class SimpleConversationalBean{

		void conversationalMethod(){
		}
	}
}
