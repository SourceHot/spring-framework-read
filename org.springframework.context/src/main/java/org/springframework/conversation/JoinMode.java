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
 * The mode used when creating a new conversation declaring on how to use an
 * already existing conversation. The default join mode is {@link #NEW} whereas
 * a new conversation is created and an already existing one is ended.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public enum JoinMode {
	/**
	 * Creates a new conversation and ends an already exiting one by default.
	 * This is the default join mode and is used, if none has been specified
	 * explicitly. The new mode is somewhat the same as the {@link #ROOT} mode,
	 * but an eventually existing current conversation is ended first instead of
	 * raising an exception.
	 */
	NEW,

	/**
	 * The joined mode declares to join an already existing conversation. The
	 * state of the current conversation is shared, so the new conversation is
	 * using the already existing conversation.
	 */
	JOINED,

	/**
	 * The nested mode declares to not join an already existing conversation but
	 * rather add a new conversation as a nested conversation to the current
	 * one. Every newly created attribute having conversation scope is added to
	 * this nested conversation rather than its parent and hence is out of
	 * scope, if the nested conversation ends.
	 */
	NESTED,

	/**
	 * The isolation mode is a special nesting mode isolating the nested
	 * conversation from its parent and hence without inheritance of the
	 * parent's state. Therefore, an isolated conversation has its own state and
	 * as it is ended, is switched to its parent without the states of the
	 * parent and isolated conversation being shared or inherited at any time.
	 * This mode is usually used for a short, isolated conversation /
	 * transaction to be done while an ongoing conversation is in progress and
	 * will still be used after the isolated conversation has been ended.
	 */
	ISOLATED,

	/**
	 * The root mode declares that the new conversation must be a root one,
	 * meaning, an already existing one is not joined nor nested nor it is
	 * allowed. Use this join mode to make sure the conversation management is
	 * properly used (e.g. in asynchronous threads usually where there is a root
	 * conversation which is ended after the job running asynchronously was
	 * finished).
	 */
	ROOT,

	/**
	 * This mode is the same as {@link #ROOT} or {@link #NEW}, but it will not
	 * terminate the current one but rather switch it in order to still be
	 * available but the newly created one will become the current conversation.
	 * Using this mode assumes the manual ending and removing of the switched
	 * conversation as it is not terminated automatically, unless there is a
	 * timeout.
	 */
	SWITCHED;

	/**
	 * @return the default join mode to be used for creating new, temporary
	 * conversations ({@link JoinMode#NEW})
	 */
	public static JoinMode getDefaultJoinMode() {
		return NEW;
	}

	/**
	 * @return <code>true</code>, if this join mode allows nesting,
	 * <code>false</code> otherwise
	 */
	public boolean mustBeNested() {
		return (this == NESTED || this == ISOLATED);
	}

	/**
	 * @return <code>true</code>, if this join mode declares the conversation be
	 * isolated from its parent
	 */
	public boolean mustBeIsolated() {
		return (this == ISOLATED);
	}

	/**
	 * @return <code>true</code>, if this join mode does not allow joining or
	 * nesting
	 */
	public boolean mustBeRoot() {
		return (this == ROOT);
	}

	/**
	 * @return <code>true</code>, if an already existing conversation must be
	 * ended before creating a new one
	 */
	public boolean mustEndCurrent() {
		return (this == NEW);
	}

	/**
	 * @return <code>true</code>, if the this join mode allows joining an
	 * already existing conversation
	 */
	public boolean mustBeJoined() {
		return (this == JOINED);
	}

	/**
	 * @return <code>true</code>, if this join mode needs switching the current
	 * one, if any
	 */
	public boolean mustBeSwitched() {
		return (this == SWITCHED);
	}
}
