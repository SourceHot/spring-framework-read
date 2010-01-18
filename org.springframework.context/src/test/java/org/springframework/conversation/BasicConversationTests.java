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

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.conversation.manager.ConversationManager;
import org.springframework.conversation.manager.ConversationStore;
import org.springframework.conversation.manager.MutableConversation;
import org.springframework.conversation.scope.ConversationResolver;

/**
 * Basic conversation management tests.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public class BasicConversationTests {
	private static ConfigurableApplicationContext context;
	private static ConversationManager manager;
	private static ConversationStore store;
	private static ConversationResolver resolver;

	@BeforeClass
	public static void setUp() {
		context = loadContext(getContextLocation());
		manager = context.getBean(ConversationManager.class);
		store = context.getBean(ConversationStore.class);
		resolver = context.getBean(ConversationResolver.class);
	}

	@Test
	public void testContext() {
		assertNotNull(context);
		assertNotNull(manager);
		assertNotNull(resolver);
		assertNotNull(store);
	}

	@Test
	public void testTemporaryConversation() {
		ConversationalBean bean = (ConversationalBean) context.getBean("testBean");
		assertNotNull(bean);
		assertNull(bean.getName());
		String id = resolver.getCurrentConversationId();
		assertNotNull(id);
		Conversation conversation = store.getConversation(id);
		assertNotNull(conversation);
		assertTrue(conversation.isTemporary());
		Object attribute = conversation.getAttribute("testBean");
		assertNotNull(attribute);
		assertSame(bean, attribute);

		conversation.end(ConversationEndingType.SUCCESS);
		assertTrue(conversation.isEnded());
		assertNull(resolver.getCurrentConversationId());
	}

	@Test
	public void testNewConversation() {
		Conversation conversation = manager.beginConversation(false, JoinMode.NEW);
		assertNotNull(conversation);
		assertFalse(conversation.isTemporary());
		assertSame(conversation, manager.getCurrentConversation());

		ConversationalBean bean = (ConversationalBean) context.getBean("testBean");
		assertNotNull(bean);

		conversation.end(ConversationEndingType.SUCCESS);
		assertTrue(conversation.isEnded());
		assertNull(resolver.getCurrentConversationId());
		assertNull(manager.getCurrentConversation());
	}

	@Test
	public void testRootConversation() {
		Conversation conversation = manager.beginConversation(false, JoinMode.ROOT);
		assertNotNull(conversation);
		assertFalse(conversation.isTemporary());
		assertSame(conversation, manager.getCurrentConversation());

		ConversationalBean bean = (ConversationalBean) context.getBean("testBean");
		assertNotNull(bean);

		conversation.end(ConversationEndingType.SUCCESS);
		assertTrue(conversation.isEnded());
		assertNull(resolver.getCurrentConversationId());
		assertNull(manager.getCurrentConversation());
	}

	@Test
	public void testRootConversationFailure() {
		Conversation conversation = manager.beginConversation(false, JoinMode.ROOT);
		assertNotNull(conversation);
		assertFalse(conversation.isTemporary());
		assertFalse(conversation.isSwitched());
		assertSame(conversation, manager.getCurrentConversation());

		try {
			manager.beginConversation(false, JoinMode.ROOT);
			fail("IllegalStateException must be thrown as there is a current conversation in place already.");
		} catch (IllegalStateException e) {
			// must happen!
		}

		conversation.end(ConversationEndingType.SUCCESS);
		assertTrue(conversation.isEnded());
		assertNull(resolver.getCurrentConversationId());
		assertNull(manager.getCurrentConversation());
	}

	@Test
	public void testNestedConversation() {
		Conversation conversation = manager.beginConversation(false, JoinMode.ROOT);
		assertNotNull(conversation);
		assertFalse(conversation.isTemporary());
		assertSame(conversation, manager.getCurrentConversation());
		assertFalse(conversation.isNested());
		assertFalse(((MutableConversation) conversation).isParent());

		ConversationalBean bean = (ConversationalBean) context.getBean("testBean");
		assertNotNull(bean);

		Conversation nestedConversation = manager.beginConversation(false, JoinMode.NESTED);
		assertNotNull(nestedConversation);
		assertSame(nestedConversation, manager.getCurrentConversation());
		assertNotSame(conversation, nestedConversation);
		assertTrue(nestedConversation.isNested());
		assertTrue(((MutableConversation) conversation).isParent());

		assertSame(bean, context.getBean("testBean"));

		nestedConversation.end(ConversationEndingType.SUCCESS);
		assertSame(conversation, manager.getCurrentConversation());

		conversation.end(ConversationEndingType.SUCCESS);
		assertTrue(conversation.isEnded());
		assertTrue(nestedConversation.isEnded());
		assertNull(resolver.getCurrentConversationId());
		assertNull(manager.getCurrentConversation());
	}

	@Test
	public void testNestedConversationEndingFailure() {
		Conversation conversation = manager.beginConversation(false, JoinMode.ROOT);
		assertNotNull(conversation);
		assertFalse(conversation.isTemporary());
		assertSame(conversation, manager.getCurrentConversation());
		assertFalse(conversation.isNested());
		assertFalse(((MutableConversation) conversation).isParent());

		Conversation nestedConversation = manager.beginConversation(false, JoinMode.NESTED);
		assertNotNull(nestedConversation);
		assertFalse(nestedConversation.isSwitched());
		assertNotSame(conversation, nestedConversation);
		assertTrue(nestedConversation.isNested());
		assertTrue(((MutableConversation) conversation).isParent());

		try {
			conversation.end(ConversationEndingType.SUCCESS);
			fail("There must be an IllegalStateException as trying to end a parent conversation without having its nested conversation ended first");
		} catch (IllegalStateException e) {
			// must happen
		}

		nestedConversation.end(ConversationEndingType.SUCCESS);
		assertSame(conversation, manager.getCurrentConversation());

		conversation.end(ConversationEndingType.SUCCESS);
		assertTrue(conversation.isEnded());
		assertTrue(nestedConversation.isEnded());
		assertNull(resolver.getCurrentConversationId());
		assertNull(manager.getCurrentConversation());
	}

	@Test
	public void testIsolatedConversation() {
		Conversation conversation = manager.beginConversation(false, JoinMode.ROOT);
		assertNotNull(conversation);
		assertFalse(conversation.isTemporary());
		assertSame(conversation, manager.getCurrentConversation());
		assertFalse(conversation.isNested());
		assertFalse(((MutableConversation) conversation).isParent());

		ConversationalBean bean = (ConversationalBean) context.getBean("testBean");
		assertNotNull(bean);

		Conversation nestedConversation = manager.beginConversation(false, JoinMode.ISOLATED);
		assertNotNull(nestedConversation);
		assertFalse(nestedConversation.isSwitched());
		assertSame(nestedConversation, manager.getCurrentConversation());
		assertNotSame(conversation, nestedConversation);
		assertTrue(nestedConversation.isNested());
		assertTrue(((MutableConversation) conversation).isParent());

		assertNotSame(bean, context.getBean("testBean"));

		nestedConversation.end(ConversationEndingType.SUCCESS);
		assertSame(conversation, manager.getCurrentConversation());

		assertSame(bean, context.getBean("testBean"));

		conversation.end(ConversationEndingType.SUCCESS);
		assertTrue(conversation.isEnded());
		assertTrue(nestedConversation.isEnded());
		assertNull(resolver.getCurrentConversationId());
		assertNull(manager.getCurrentConversation());
	}

	@Test
	public void testJoinedConversation() {
		Conversation conversation = manager.beginConversation(false, JoinMode.ROOT);
		assertNotNull(conversation);
		assertFalse(conversation.isTemporary());
		assertFalse(conversation.isSwitched());
		assertSame(conversation, manager.getCurrentConversation());
		assertFalse(conversation.isNested());
		assertFalse(((MutableConversation) conversation).isParent());

		ConversationalBean bean = (ConversationalBean) context.getBean("testBean");
		assertNotNull(bean);

		Conversation joinedConversation = manager.beginConversation(false, JoinMode.JOINED);
		assertNotNull(joinedConversation);
		assertFalse(joinedConversation.isSwitched());
		assertSame(joinedConversation, manager.getCurrentConversation());
		assertSame(conversation, joinedConversation);
		assertFalse(joinedConversation.isNested());
		assertFalse(((MutableConversation) joinedConversation).isParent());

		assertSame(bean, context.getBean("testBean"));

		joinedConversation.end(ConversationEndingType.SUCCESS);
		assertSame(conversation, manager.getCurrentConversation());

		assertSame(bean, context.getBean("testBean"));

		conversation.end(ConversationEndingType.SUCCESS);
		assertTrue(conversation.isEnded());
		assertTrue(joinedConversation.isEnded());
		assertNull(resolver.getCurrentConversationId());
		assertNull(manager.getCurrentConversation());
	}

	@Test
	public void testSwitchedConversation() {
		Conversation conversation = manager.beginConversation(false, JoinMode.SWITCHED);
		assertNotNull(conversation);
		assertFalse(conversation.isTemporary());
		assertTrue(conversation.isSwitched());
		assertSame(conversation, manager.getCurrentConversation());
		assertFalse(conversation.isNested());
		assertFalse(((MutableConversation) conversation).isParent());

		ConversationalBean bean = (ConversationalBean) context.getBean("testBean");
		assertNotNull(bean);

		Conversation switchedConversation = manager.beginConversation(false, JoinMode.SWITCHED);
		assertNotNull(switchedConversation);
		assertTrue(conversation.isSwitched());
		assertSame(switchedConversation, manager.getCurrentConversation());
		assertNotSame(conversation, switchedConversation);
		assertFalse(switchedConversation.isNested());
		assertFalse(((MutableConversation) switchedConversation).isParent());

		ConversationalBean bean2 = (ConversationalBean) context.getBean("testBean");
		assertNotSame(bean, bean2);

		manager.switchConversation(conversation.getId());
		assertSame(conversation, manager.getCurrentConversation());
		assertSame(bean, context.getBean("testBean"));

		manager.switchConversation(switchedConversation.getId());
		assertSame(bean2, context.getBean("testBean"));

		switchedConversation.end(ConversationEndingType.SUCCESS);
		conversation.end(ConversationEndingType.SUCCESS);
		assertTrue(conversation.isEnded());
		assertTrue(switchedConversation.isEnded());
		assertNull(resolver.getCurrentConversationId());
		assertNull(manager.getCurrentConversation());
	}

	@Test
	public void testSwitchedConversationEnding() {
		Conversation conversation = manager.beginConversation(false, JoinMode.SWITCHED);
		assertNotNull(conversation);
		assertSame(conversation, manager.getCurrentConversation());

		Conversation switchedConversation = manager.beginConversation(false, JoinMode.SWITCHED);
		assertNotNull(switchedConversation);
		assertSame(switchedConversation, manager.getCurrentConversation());
		assertNotSame(conversation, switchedConversation);

		manager.switchConversation(conversation.getId());
		manager.switchConversation(switchedConversation.getId());

		switchedConversation.end(ConversationEndingType.SUCCESS);
		assertNull(manager.getCurrentConversation());
		conversation.end(ConversationEndingType.SUCCESS);
		assertNull(manager.getCurrentConversation());

		assertTrue(conversation.isEnded());
		assertTrue(switchedConversation.isEnded());
		assertNull(resolver.getCurrentConversationId());
		assertNull(manager.getCurrentConversation());
	}

	@Test
	public void testImplicitConversationEnding() {
		Conversation conversation = manager.beginConversation(false, JoinMode.ROOT);
		assertNotNull(conversation);
		assertSame(conversation, manager.getCurrentConversation());

		Conversation switchedConversation = manager.beginConversation(false, JoinMode.SWITCHED);
		assertNotNull(switchedConversation);
		assertSame(switchedConversation, manager.getCurrentConversation());
		assertNotSame(conversation, switchedConversation);
		assertTrue(conversation.isEnded());

		switchedConversation.end(ConversationEndingType.SUCCESS);
		assertNull(manager.getCurrentConversation());

		assertTrue(conversation.isEnded());
		assertTrue(switchedConversation.isEnded());
		assertNull(resolver.getCurrentConversationId());
		assertNull(manager.getCurrentConversation());
	}

	@Test
	public void testConversationAnnotation() {
		ConversationalServiceBean serviceBean = context.getBean(ConversationalServiceBean.class);
		assertNotNull(serviceBean);

		assertNull(serviceBean.getStartingConversation());
		assertNull(serviceBean.getEndingConversation());
		assertNull(serviceBean.getConversationalConversation());

		serviceBean.startConversation();
		Conversation conversation1 = serviceBean.getStartingConversation();
		assertNotNull(conversation1);
		assertSame(conversation1, manager.getCurrentConversation());
		assertNull(serviceBean.getEndingConversation());
		assertNull(serviceBean.getConversationalConversation());

		serviceBean.endConversation();
		Conversation conversation2 = serviceBean.getEndingConversation();
		assertNotNull(conversation2);
		assertNull(manager.getCurrentConversation());
		assertSame(conversation1, conversation2);
		assertNull(serviceBean.getConversationalConversation());

		serviceBean.doInConversation();
		Conversation conversation3 = serviceBean.getConversationalConversation();
		assertNotNull(conversation3);
		assertNotSame(conversation1, conversation3);
		assertNotSame(conversation2, conversation3);

		assertNull(manager.getCurrentConversation());

		serviceBean.clean();
	}

	protected static String getContextLocation() {
		return "org/springframework/conversation/conversationTestContext.xml";
	}

	protected static ConfigurableApplicationContext loadContext(String configLocation) {
		return new GenericXmlApplicationContext(getContextLocation());
	}
}
