/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.testfixture.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.parsing.AliasDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.DefaultsDefinition;
import org.springframework.beans.factory.parsing.ImportDefinition;
import org.springframework.beans.factory.parsing.ReaderEventListener;

/**
 *
 * 收集事件信息并作出处理
 * 1. 内部使用map,list 收集数据
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
public class CollectingReaderEventListener implements ReaderEventListener {

	private final List<DefaultsDefinition> defaults = new LinkedList<>();

	/**
	 * key: beanName
	 * value:{@link ComponentDefinition}
	 */
	private final Map<String, ComponentDefinition> componentDefinitions = new LinkedHashMap<>(8);

	/**
	 * key: beanName
	 * value: 别名定义列表
	 */
	private final Map<String, List<AliasDefinition>> aliasMap = new LinkedHashMap<>(8);

	/**
	 * import 定义列表
	 */
	private final List<ImportDefinition> imports = new LinkedList<>();


	@Override
	public void defaultsRegistered(DefaultsDefinition defaultsDefinition) {
		this.defaults.add(defaultsDefinition);
	}

	public List<DefaultsDefinition> getDefaults() {
		return Collections.unmodifiableList(this.defaults);
	}

	@Override
	public void componentRegistered(ComponentDefinition componentDefinition) {
		this.componentDefinitions.put(componentDefinition.getName(), componentDefinition);
	}

	public ComponentDefinition getComponentDefinition(String name) {
		return this.componentDefinitions.get(name);
	}

	public ComponentDefinition[] getComponentDefinitions() {
		Collection<ComponentDefinition> collection = this.componentDefinitions.values();
		return collection.toArray(new ComponentDefinition[collection.size()]);
	}

	@Override
	public void aliasRegistered(AliasDefinition aliasDefinition) {
		// 获取已经注册过的beanName对应的别名
		List<AliasDefinition> aliases = this.aliasMap.get(aliasDefinition.getBeanName());
		if (aliases == null) {
			aliases = new ArrayList<>();
			// beanName 和 别名对应关系设置
			this.aliasMap.put(aliasDefinition.getBeanName(), aliases);
		}
		// 别名列表添加
		aliases.add(aliasDefinition);
	}

	public List<AliasDefinition> getAliases(String beanName) {
		List<AliasDefinition> aliases = this.aliasMap.get(beanName);
		return (aliases != null ? Collections.unmodifiableList(aliases) : null);
	}

	@Override
	public void importProcessed(ImportDefinition importDefinition) {
		this.imports.add(importDefinition);
	}

	public List<ImportDefinition> getImports() {
		return Collections.unmodifiableList(this.imports);
	}

}
