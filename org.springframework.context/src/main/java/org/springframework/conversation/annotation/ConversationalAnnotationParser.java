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

import java.lang.reflect.AnnotatedElement;

import org.springframework.conversation.interceptor.ConversationAttribute;
import org.springframework.conversation.interceptor.DefaultConversationAttribute;

/**
 * ConversationAnnotationParser for the Conversational annotation
 *
 * @author Agim Emruli
 * @see Conversational
 */
public class ConversationalAnnotationParser implements ConversationAnnotationParser {

	public ConversationAttribute parseConversationAnnotation(AnnotatedElement annotatedElement) {
		Conversational conversational = annotatedElement.getAnnotation(Conversational.class);
		if (conversational != null) {
			return new DefaultConversationAttribute(conversational.joinMode(), conversational.timeout(),
					conversational.endingType());
		}
		return null;
	}
}