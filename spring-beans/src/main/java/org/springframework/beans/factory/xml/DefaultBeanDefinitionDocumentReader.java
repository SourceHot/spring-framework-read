/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.beans.factory.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.ImportDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Default implementation of the {@link BeanDefinitionDocumentReader} interface that
 * reads bean definitions according to the "spring-beans" DTD and XSD format
 * (Spring's default XML bean definition format).
 *
 * <p>The structure, elements, and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). {@code <beans>} does not need to be the root
 * element of the XML document: this class will parse all bean definition elements
 * in the XML file, regardless of the actual root element.
 * <p>
 * <p>
 * <p>
 * spring xml的一些定义文字
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 18.12.2003
 */
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

    public static final String BEAN_ELEMENT = BeanDefinitionParserDelegate.BEAN_ELEMENT;

    public static final String NESTED_BEANS_ELEMENT = "beans";

    public static final String ALIAS_ELEMENT = "alias";

    public static final String NAME_ATTRIBUTE = "name";

    public static final String ALIAS_ATTRIBUTE = "alias";

    public static final String IMPORT_ELEMENT = "import";

    public static final String RESOURCE_ATTRIBUTE = "resource";

    public static final String PROFILE_ATTRIBUTE = "profile";


    protected final Log logger = LogFactory.getLog(getClass());

    @Nullable
    private XmlReaderContext readerContext;

    @Nullable
    private BeanDefinitionParserDelegate delegate;


    /**
     * This implementation parses bean definitions according to the "spring-beans" XSD
     * (or DTD, historically).
     * <p>Opens a DOM Document; then initializes the default settings
     * specified at the {@code <beans/>} level; then parses the contained bean definitions.
     */
    @Override
    public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
        this.readerContext = readerContext;
        // 真正的注册bean定义的方法
        doRegisterBeanDefinitions(doc.getDocumentElement());
    }

    /**
     * Return the descriptor for the XML resource that this parser works on.
     */
    protected final XmlReaderContext getReaderContext() {
        Assert.state(this.readerContext != null, "No XmlReaderContext available");
        return this.readerContext;
    }

    /**
     * Invoke the {@link org.springframework.beans.factory.parsing.SourceExtractor}
     * to pull the source metadata from the supplied {@link Element}.
     */
    @Nullable
    protected Object extractSource(Element ele) {
        return getReaderContext().extractSource(ele);
    }


    /**
     * Register each bean definition within the given root {@code <beans/>} element.
     * 根据给定的节点解析bean的定义
     */
    @SuppressWarnings("deprecation")  // for Environment.acceptsProfiles(String...)
    protected void doRegisterBeanDefinitions(Element root) {
        // Any nested <beans> elements will cause recursion in this method. In
        // order to propagate and preserve <beans> default-* attributes correctly,
        // keep track of the current (parent) delegate, which may be null. Create
        // the new (child) delegate with a reference to the parent for fallback purposes,
        // then ultimately reset this.delegate back to its original (parent) reference.
        // this behavior emulates a stack of delegates without actually necessitating one.
        BeanDefinitionParserDelegate parent = this.delegate;
        this.delegate = createDelegate(getReaderContext(), root, parent);

        if (this.delegate.isDefaultNamespace(root)) {
            // 解析是否有 profile 标签
            String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
            if (StringUtils.hasText(profileSpec)) {
                String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
                        profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
                // We cannot use Profiles.of(...) since profile expressions are not supported
                // in XML config. See SPR-12458 for details.
                if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Skipped XML bean definition file due to specified profiles [" + profileSpec +
                                "] not matching: " + getReaderContext().getResource());
                    }
                    return;
                }
            }
        }

        // 啥也没干
        preProcessXml(root);
        // 解析bean的定义
        parseBeanDefinitions(root, this.delegate);
        // 啥也没干
        postProcessXml(root);

        this.delegate = parent;
    }

    /**
     * @param readerContext  当前上下文
     * @param root           xml节点
     * @param parentDelegate
     * @return
     */
    protected BeanDefinitionParserDelegate createDelegate(
            XmlReaderContext readerContext, Element root, @Nullable BeanDefinitionParserDelegate parentDelegate) {

        BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);
        delegate.initDefaults(root, parentDelegate);
        return delegate;
    }

    /**
     * Parse the elements at the root level in the document:
     * "import", "alias", "bean".
     * 解析标签,import\alias\bean
     *
     * @param root the DOM root element of the document
     */
    protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
        if (delegate.isDefaultNamespace(root)) {
            NodeList nl = root.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node instanceof Element) {
                    Element ele = (Element) node;
                    if (delegate.isDefaultNamespace(ele)) {
                        // 不同标签的解析
                        parseDefaultElement(ele, delegate);
                    } else {
                        // 非spring 默认标签解析
                        delegate.parseCustomElement(ele);
                    }
                }
            }
        } else {
            delegate.parseCustomElement(root);
        }
    }

    /**
     * 解析不同类型的标签,解析标签如下
     * {@code <import>},{@code <alias>},{@code <bean>},{@code <beans>}
     *
     * @param ele
     * @param delegate
     */
    private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
        if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
            //  import 标签解析
            importBeanDefinitionResource(ele);
        } else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
            // alias 标签解析
            processAliasRegistration(ele);
        } else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
            // bean 标签解析
            processBeanDefinition(ele, delegate);
        } else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
            // beans 标签解析
            // recurse
            doRegisterBeanDefinitions(ele);
        }
    }

    /**
     * 解析{@code <import>} 标签
     * Parse an "import" element and load the bean definitions
     * from the given resource into the bean factory.
     */
    protected void importBeanDefinitionResource(Element ele) {
        // 获取 resource 属性
        String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
        // 是否有 resource 属性
        if (!StringUtils.hasText(location)) {
            getReaderContext().error("Resource location must not be empty", ele);
            return;
        }

        // Resolve system properties: e.g. "${user.dir}"
        // 获取系统环境,解析路径
        /**
         * 1. getReaderContext() 获取{@link XmlReaderContext}
         * 2. getEnvironment() 获取环境
         * 3. resolveRequiredPlaceholders(location) 解析占位符${...}
         */
        location = getReaderContext().getEnvironment().resolveRequiredPlaceholders(location);

        // 资源存放集合
        Set<Resource> actualResources = new LinkedHashSet<>(4);

        // Discover whether the location is an absolute or relative URI
        // 相对路径 绝对路径的判断
        boolean absoluteLocation = false;
        try {
            // 判断是相对路径还是绝对路径
            absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
        } catch (URISyntaxException ex) {
            // cannot convert to an URI, considering the location relative
            // unless it is the well-known Spring prefix "classpath*:"
        }

        // Absolute or relative?
        if (absoluteLocation) {
            try {
                // 获取import中的数量
                int importCount = getReaderContext().getReader().loadBeanDefinitions(location, actualResources);
                if (logger.isTraceEnabled()) {
                    logger.trace("Imported " + importCount + " bean definitions from URL location [" + location + "]");
                }
            } catch (BeanDefinitionStoreException ex) {
                getReaderContext().error(
                        "Failed to import bean definitions from URL location [" + location + "]", ele, ex);
            }
        } else {
            // No URL -> considering resource location as relative to the current file.
            try {
                int importCount;
                // 本地地址
                Resource relativeResource = getReaderContext().getResource().createRelative(location);
                if (relativeResource.exists()) {
                    // 此处调用方式和加载一个xml文件相同
                    importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);

                    actualResources.add(relativeResource);
                } else {
                    String baseLocation = getReaderContext().getResource().getURL().toString();
                    importCount = getReaderContext().getReader().loadBeanDefinitions(
                            StringUtils.applyRelativePath(baseLocation, location), actualResources);
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Imported " + importCount + " bean definitions from relative location [" + location + "]");
                }
            } catch (IOException ex) {
                getReaderContext().error("Failed to resolve current resource location", ele, ex);
            } catch (BeanDefinitionStoreException ex) {
                getReaderContext().error(
                        "Failed to import bean definitions from relative location [" + location + "]", ele, ex);
            }
        }
        // 转换数组
        Resource[] actResArray = actualResources.toArray(new Resource[0]);
        /***
         * fireImportProcessed()触发import事件,
         * 并且通过{@link org.springframework.beans.factory.parsing.ReaderEventListener#importProcessed(ImportDefinition)} 宣布处理结果
         */
        getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
    }

    /**
     * 解析 {@code <alias>}
     * Process the given alias element, registering the alias with the registry.
     */
    protected void processAliasRegistration(Element ele) {
        // 获取 name 属性
        String name = ele.getAttribute(NAME_ATTRIBUTE);
        // 获取  alias 属性
        String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
        boolean valid = true;
        // 判断是否有 name 属性
        if (!StringUtils.hasText(name)) {
            getReaderContext().error("Name must not be empty", ele);
            valid = false;
        }
        // 判断是否有 alias 属性
        if (!StringUtils.hasText(alias)) {
            getReaderContext().error("Alias must not be empty", ele);
            valid = false;
        }
        if (valid) {
            try {
                /**
                 * 1. getReaderContext(): 获取 {@link XmlReaderContext}
                 * 2. getRegistry(): 获取{@link BeanDefinitionRegistry} 获取注册器
                 * 3. registerAlias(): 别名注册 {@link org.springframework.core.SimpleAliasRegistry#registerAlias(String, String)}
                 */
                getReaderContext().getRegistry().registerAlias(name, alias);
            } catch (Exception ex) {
                getReaderContext().error("Failed to register alias '" + alias +
                        "' for bean with name '" + name + "'", ele, ex);
            }
            getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
        }
    }

    /**
     * Process the given bean element, parsing the bean definition
     * and registering it with the registry.
     * <p>
     * 加载信息并且注册
     */
    protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
        // 元素解析
        BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
        if (bdHolder != null) {
            bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
            try {
                // Register the final decorated instance.
                //注册bean定义
                // getReaderContext() 在读文件的时候就加载了
                BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
            } catch (BeanDefinitionStoreException ex) {
                getReaderContext().error("Failed to register bean definition with name '" +
                        bdHolder.getBeanName() + "'", ele, ex);
            }
            // Send registration event.
            getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
        }
    }


    /**
     * Allow the XML to be extensible by processing any custom element types first,
     * before we start to process the bean definitions. This method is a natural
     * extension point for any other custom pre-processing of the XML.
     * <p>The default implementation is empty. Subclasses can override this method to
     * convert custom elements into standard Spring bean definitions, for example.
     * Implementors have access to the parser's bean definition reader and the
     * underlying XML resource, through the corresponding accessors.
     *
     * @see #getReaderContext()
     */
    protected void preProcessXml(Element root) {
    }

    /**
     * Allow the XML to be extensible by processing any custom element types last,
     * after we finished processing the bean definitions. This method is a natural
     * extension point for any other custom post-processing of the XML.
     * <p>The default implementation is empty. Subclasses can override this method to
     * convert custom elements into standard Spring bean definitions, for example.
     * Implementors have access to the parser's bean definition reader and the
     * underlying XML resource, through the corresponding accessors.
     *
     * @see #getReaderContext()
     */
    protected void postProcessXml(Element root) {
    }

}
