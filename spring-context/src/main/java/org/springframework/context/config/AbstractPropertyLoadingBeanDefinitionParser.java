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

package org.springframework.context.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Abstract parser for &lt;context:property-.../&gt; elements.
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @author Dave Syer
 * @since 2.5.2
 */
abstract class AbstractPropertyLoadingBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }

    /**
     * 解析并且设置基本属性
     *
     * @param element       the XML element being parsed
     * @param parserContext the object encapsulating the current state of the parsing process
     * @param builder       used to define the {@code BeanDefinition}
     */
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        // 获取 location 属性
        String location = element.getAttribute("location");
        if (StringUtils.hasLength(location)) {
            // 路径解析
            location = parserContext.getReaderContext().getEnvironment().resolvePlaceholders(location);
            // 分割
            String[] locations = StringUtils.commaDelimitedListToStringArray(location);
            // 设置路径
            builder.addPropertyValue("locations", locations);
        }

        // 获取 properties-ref 属性
        String propertiesRef = element.getAttribute("properties-ref");
        if (StringUtils.hasLength(propertiesRef)) {
            // 添加 properties
            builder.addPropertyReference("properties", propertiesRef);
        }
        // 获取 file-encoding 文件编码
        String fileEncoding = element.getAttribute("file-encoding");
        if (StringUtils.hasLength(fileEncoding)) {
            // 设置文件编码
            builder.addPropertyValue("fileEncoding", fileEncoding);
        }
        // 获取 order 属性
        String order = element.getAttribute("order");
        if (StringUtils.hasLength(order)) {
            // 添加 order 值
            builder.addPropertyValue("order", Integer.valueOf(order));
        }

        builder.addPropertyValue("ignoreResourceNotFound",
                Boolean.valueOf(element.getAttribute("ignore-resource-not-found")));

        builder.addPropertyValue("localOverride",
                Boolean.valueOf(element.getAttribute("local-override")));

        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
    }

}
