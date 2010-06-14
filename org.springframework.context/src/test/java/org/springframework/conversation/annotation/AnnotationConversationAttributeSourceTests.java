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
import java.util.Collections;
import java.util.LinkedHashSet;

import org.junit.Test;

import org.springframework.conversation.interceptor.ConversationAttribute;
import org.springframework.util.ReflectionUtils;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/** @author Agim Emruli */
public class AnnotationConversationAttributeSourceTests {

	@Test
	public void testWithDefaultAnnotationParser() throws NoSuchMethodException {
		AnnotationConversationAttributeSource source = new AnnotationConversationAttributeSource();
		Method method = ReflectionUtils.findMethod(TestConversationalBean.class,"noConversationMethod");
		ConversationAttribute attribute = source.getConversationAttribute(method, TestConversationalBean.class);
		assertNull("No ConversationAttribute found for non-annotated method",attribute);
	}

	@Test
	public void testWithCustomAnnotationParser() throws NoSuchMethodException {
		ConversationAnnotationParser parser = createMock(ConversationAnnotationParser.class);

		Method method = ReflectionUtils.findMethod(TestConversationalBean.class,"conversationalMethod");

		AnnotationConversationAttributeSource source = new AnnotationConversationAttributeSource(parser);
		ConversationAttribute attribute = createMock(ConversationAttribute.class);
		expect(parser.parseConversationAnnotation(method)).andReturn(attribute).once();
		replay(parser, attribute);
		ConversationAttribute result = source.getConversationAttribute(method, TestConversationalBean.class);
		assertEquals("ConversationAttribute returned from ConversationAnnotationParser",attribute, result);
		verify(parser,attribute);
	}

	@Test
	public void testWithMultipleCustomAnnotationParsers() throws Exception {
		ConversationAnnotationParser firstParser = createMock(ConversationAnnotationParser.class);
		ConversationAnnotationParser secondParser = createMock(ConversationAnnotationParser.class);

		LinkedHashSet<ConversationAnnotationParser> parsers = new LinkedHashSet<ConversationAnnotationParser>();
		Collections.addAll(parsers, firstParser, secondParser);

		AnnotationConversationAttributeSource source = new AnnotationConversationAttributeSource(parsers);

		Method method = ReflectionUtils.findMethod(TestConversationalBean.class,"conversationalMethod");

		expect(firstParser.parseConversationAnnotation(method)).andReturn(null).once();

		ConversationAttribute attribute = createMock(ConversationAttribute.class);
		expect(secondParser.parseConversationAnnotation(method)).andReturn(attribute).once();

		replay(firstParser, secondParser, attribute);
		ConversationAttribute result = source.getConversationAttribute(method, TestConversationalBean.class);
		assertEquals("ConversationAttribute from second ConversationAnnotationParser",attribute, result);
		verify(firstParser,secondParser,attribute);
	}

	@Test
	public void testWithConversationalInterfaceMethod() throws Exception {
		ConversationAnnotationParser parser = createMock(ConversationAnnotationParser.class);

		Method interfaceMethod = ReflectionUtils.findMethod(TestConversationalBeanInterface.class,"conversationalInterfaceMethod");
		Method implementationMethod = ReflectionUtils.findMethod(TestConversationalBean.class,"conversationalInterfaceMethod");

		AnnotationConversationAttributeSource source = new AnnotationConversationAttributeSource(parser);
		ConversationAttribute attribute = createMock(ConversationAttribute.class);
		expect(parser.parseConversationAnnotation(implementationMethod)).andReturn(attribute).once();
		replay(parser, attribute);
		ConversationAttribute result = source.getConversationAttribute(interfaceMethod, TestConversationalBean.class);
		assertEquals("ConversationAttribute from implementation method",attribute, result);
		verify(parser,attribute);
	}

	@Test
	public void testWithConversationalAnnotatedInterfaceMethod() throws Exception {
		ConversationAnnotationParser parser = createMock(ConversationAnnotationParser.class);

		Method interfaceMethod = ReflectionUtils.findMethod(TestConversationalBeanInterface.class,"conversationalAnnotatedInterfaceMethod");
		Method implementationMethod = ReflectionUtils.findMethod(TestConversationalBean.class,"conversationalAnnotatedInterfaceMethod");

		AnnotationConversationAttributeSource source = new AnnotationConversationAttributeSource(parser);
		ConversationAttribute attribute = createMock(ConversationAttribute.class);
		expect(parser.parseConversationAnnotation(implementationMethod)).andReturn(null).once();
		expect(parser.parseConversationAnnotation(interfaceMethod)).andReturn(attribute).once();
		replay(parser, attribute);
		ConversationAttribute result = source.getConversationAttribute(interfaceMethod, TestConversationalBean.class);
		assertEquals("ConversationAttribute from (fallback) interface method",attribute, result);
		verify(parser,attribute);
	}

	@Test
	public void testWithConversationalAnnotatedInterfaceMethodAndMultipleParsers() throws Exception {
		ConversationAnnotationParser firstParser = createMock(ConversationAnnotationParser.class);
		ConversationAnnotationParser secondParser = createMock(ConversationAnnotationParser.class);

		LinkedHashSet<ConversationAnnotationParser> parsers = new LinkedHashSet<ConversationAnnotationParser>();
		Collections.addAll(parsers, firstParser, secondParser);

		AnnotationConversationAttributeSource source = new AnnotationConversationAttributeSource(parsers);

		Method interfaceMethod = ReflectionUtils.findMethod(TestConversationalBeanInterface.class,"conversationalAnnotatedInterfaceMethod");
		Method implementationMethod = ReflectionUtils.findMethod(TestConversationalBean.class,"conversationalAnnotatedInterfaceMethod");

		expect(firstParser.parseConversationAnnotation(implementationMethod)).andReturn(null).once();
		expect(firstParser.parseConversationAnnotation(interfaceMethod)).andReturn(null).once();

		ConversationAttribute attribute = createMock(ConversationAttribute.class);
		expect(secondParser.parseConversationAnnotation(implementationMethod)).andReturn(attribute).once();

		replay(firstParser, secondParser, attribute);
		ConversationAttribute result = source.getConversationAttribute(interfaceMethod, TestConversationalBean.class);
		assertEquals("ConversationAttribute from second parser with interface method",attribute, result);
		verify(firstParser,secondParser,attribute);
	}

	@SuppressWarnings({"UnusedDeclaration"})
	private static class TestConversationalBean implements TestConversationalBeanInterface{

		@Conversational
		void conversationalMethod() {
		}

		void noConversationMethod() {
		}


		@Conversational
		public void conversationalInterfaceMethod() {
		}

		public void conversationalAnnotatedInterfaceMethod() {
		}
	}

	@SuppressWarnings({"UnusedDeclaration"})
	private interface TestConversationalBeanInterface {
		void conversationalInterfaceMethod();

		@Conversational
		void conversationalAnnotatedInterfaceMethod();
	}
}
