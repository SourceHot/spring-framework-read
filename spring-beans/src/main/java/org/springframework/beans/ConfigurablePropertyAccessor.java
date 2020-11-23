/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.beans;

import org.springframework.core.convert.ConversionService;
import org.springframework.lang.Nullable;

/**
 * Interface that encapsulates configuration methods for a PropertyAccessor.
 * Also extends the PropertyEditorRegistry interface, which defines methods
 * for PropertyEditor management.
 *
 * <p>Serves as base interface for {@link BeanWrapper}.
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 2.0
 * @see BeanWrapper
 */
public interface ConfigurablePropertyAccessor extends PropertyAccessor, PropertyEditorRegistry, TypeConverter {

	/**
	 * Return the associated ConversionService, if any.
	 * 获取类型转换服务
	 */
	@Nullable
	ConversionService getConversionService();

	/**
	 * Specify a Spring 3.0 ConversionService to use for converting
	 * property values, as an alternative to JavaBeans PropertyEditors.
	 * 设置类型转换服务
	 */
	void setConversionService(@Nullable ConversionService conversionService);

	/**
	 * Return whether to extract the old property value when applying a
	 * property editor to a new value for a property.
	 * 是否需要修改老的对象数据
	 */
	boolean isExtractOldValueForEditor();

	/**
	 * Set whether to extract the old property value when applying a
	 * property editor to a new value for a property.
	 *
	 * 是否需要修改老的对象数据
	 */
	void setExtractOldValueForEditor(boolean extractOldValueForEditor);

	/**
	 * Return whether "auto-growing" of nested paths has been activated.
	 * 嵌套注入的时候是否为null的情况下是否需要创建对象
	 */
	boolean isAutoGrowNestedPaths();

	/**
	 * Set whether this instance should attempt to "auto-grow" a
	 * nested path that contains a {@code null} value.
	 * <p>If {@code true}, a {@code null} path location will be populated
	 * with a default object value and traversed instead of resulting in a
	 * {@link NullValueInNestedPathException}.
	 * <p>Default is {@code false} on a plain PropertyAccessor instance.
	 *
	 * 嵌套注入的时候是否为null的情况下是否需要创建对象
	 */
	void setAutoGrowNestedPaths(boolean autoGrowNestedPaths);

}
