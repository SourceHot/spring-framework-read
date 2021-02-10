/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.context.annotation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.Location;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase;
import org.springframework.context.annotation.DeferredImportSelector.Group;
import org.springframework.core.NestedIOException;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Parses a {@link Configuration} class definition, populating a collection of
 * {@link ConfigurationClass} objects (parsing a single Configuration class may result in
 * any number of ConfigurationClass objects because one Configuration class may import
 * another using the {@link Import} annotation).
 *
 * <p>This class helps separate the concern of parsing the structure of a Configuration
 * class from the concern of registering BeanDefinition objects based on the content of
 * that model (with the exception of {@code @ComponentScan} annotations which need to be
 * registered immediately).
 *
 * <p>This ASM-based implementation avoids reflection and eager class loading in order to
 * interoperate effectively with lazy class loading in a Spring ApplicationContext.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Sam Brannen
 * @author Stephane Nicoll
 * @since 3.0
 * @see ConfigurationClassBeanDefinitionReader
 */
class ConfigurationClassParser {

	private static final PropertySourceFactory DEFAULT_PROPERTY_SOURCE_FACTORY = new DefaultPropertySourceFactory();

	private static final Comparator<DeferredImportSelectorHolder> DEFERRED_IMPORT_COMPARATOR =
			(o1, o2) -> AnnotationAwareOrderComparator.INSTANCE.compare(o1.getImportSelector(), o2.getImportSelector());


	private final Log logger = LogFactory.getLog(getClass());

	private final MetadataReaderFactory metadataReaderFactory;

	private final ProblemReporter problemReporter;

	private final Environment environment;

	private final ResourceLoader resourceLoader;

	private final BeanDefinitionRegistry registry;

	private final ComponentScanAnnotationParser componentScanParser;

	private final ConditionEvaluator conditionEvaluator;

	private final Map<ConfigurationClass, ConfigurationClass> configurationClasses = new LinkedHashMap<>();

	/**
	 * key: 父类名称
	 * value：配置类
	 */
	private final Map<String, ConfigurationClass> knownSuperclasses = new HashMap<>();

	/**
	 * 配置名称列表
	 */
	private final List<String> propertySourceNames = new ArrayList<>();

	private final ImportStack importStack = new ImportStack();

	private final DeferredImportSelectorHandler deferredImportSelectorHandler = new DeferredImportSelectorHandler();

	private final SourceClass objectSourceClass = new SourceClass(Object.class);


	/**
	 * Create a new {@link ConfigurationClassParser} instance that will be used
	 * to populate the set of configuration classes.
	 */
	public ConfigurationClassParser(MetadataReaderFactory metadataReaderFactory,
			ProblemReporter problemReporter, Environment environment, ResourceLoader resourceLoader,
			BeanNameGenerator componentScanBeanNameGenerator, BeanDefinitionRegistry registry) {

		this.metadataReaderFactory = metadataReaderFactory;
		this.problemReporter = problemReporter;
		this.environment = environment;
		this.resourceLoader = resourceLoader;
		this.registry = registry;
		this.componentScanParser = new ComponentScanAnnotationParser(
				environment, resourceLoader, componentScanBeanNameGenerator, registry);
		this.conditionEvaluator = new ConditionEvaluator(registry, environment, resourceLoader);
	}


	public void parse(Set<BeanDefinitionHolder> configCandidates) {
		for (BeanDefinitionHolder holder : configCandidates) {
			BeanDefinition bd = holder.getBeanDefinition();
			try {
				if (bd instanceof AnnotatedBeanDefinition) {
					parse(((AnnotatedBeanDefinition) bd).getMetadata(), holder.getBeanName());
				}
				else if (bd instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) bd).hasBeanClass()) {
					parse(((AbstractBeanDefinition) bd).getBeanClass(), holder.getBeanName());
				}
				else {
					parse(bd.getBeanClassName(), holder.getBeanName());
				}
			}
			catch (BeanDefinitionStoreException ex) {
				throw ex;
			}
			catch (Throwable ex) {
				throw new BeanDefinitionStoreException(
						"Failed to parse configuration class [" + bd.getBeanClassName() + "]", ex);
			}
		}

		this.deferredImportSelectorHandler.process();
	}

	protected final void parse(@Nullable String className, String beanName) throws IOException {
		Assert.notNull(className, "No bean class name for configuration class bean definition");
		MetadataReader reader = this.metadataReaderFactory.getMetadataReader(className);
		processConfigurationClass(new ConfigurationClass(reader, beanName));
	}

	protected final void parse(Class<?> clazz, String beanName) throws IOException {
		processConfigurationClass(new ConfigurationClass(clazz, beanName));
	}

	protected final void parse(AnnotationMetadata metadata, String beanName) throws IOException {
		processConfigurationClass(new ConfigurationClass(metadata, beanName));
	}

	/**
	 * Validate each {@link ConfigurationClass} object.
	 * @see ConfigurationClass#validate
	 */
	public void validate() {
		for (ConfigurationClass configClass : this.configurationClasses.keySet()) {
			configClass.validate(this.problemReporter);
		}
	}

	public Set<ConfigurationClass> getConfigurationClasses() {
		return this.configurationClasses.keySet();
	}


	protected void processConfigurationClass(ConfigurationClass configClass) throws IOException {
		// 条件注解解析，阶段为配置解析阶段
		if (this.conditionEvaluator.shouldSkip(configClass.getMetadata(), ConfigurationPhase.PARSE_CONFIGURATION)) {
			return;
		}

		// 尝试从配置类缓存中获取配置类
		ConfigurationClass existingClass = this.configurationClasses.get(configClass);
		// 缓存中的配置类存在
		if (existingClass != null) {
			// 判断 配置类是否是导入的
			if (configClass.isImported()) {
				// 判断缓存中的配置类是否是导入的
				if (existingClass.isImported()) {
					// 合并配置信息
					existingClass.mergeImportedBy(configClass);
				}
				// Otherwise ignore new imported config class; existing non-imported class overrides it.
				return;
			}
			else {
				// Explicit bean definition found, probably replacing an import.
				// Let's remove the old one and go with the new one.
				// 从配置类缓存中移除当前配置类
				this.configurationClasses.remove(configClass);
				// 从已知的配置类中移除当前的配置类
				this.knownSuperclasses.values().removeIf(configClass::equals);
			}
		}

		// Recursively process the configuration class and its superclass hierarchy.
		// 将配置类转换成 SourceClass
		SourceClass sourceClass = asSourceClass(configClass);
		do {
			// 解析配置类
			sourceClass = doProcessConfigurationClass(configClass, sourceClass);
		}
		while (sourceClass != null);

		// 放入配置类缓存
		this.configurationClasses.put(configClass, configClass);
	}

	/**
	 * Apply processing and build a complete {@link ConfigurationClass} by reading the
	 * annotations, members and methods from the source class. This method can be called
	 * multiple times as relevant sources are discovered.
	 * @param configClass the configuration class being build
	 * @param sourceClass a source class
	 * @return the superclass, or {@code null} if none found or previously processed
	 */
	@Nullable
	protected final SourceClass doProcessConfigurationClass(ConfigurationClass configClass, SourceClass sourceClass)
			throws IOException {

		// 配置类是否存在 Component 注解
		if (configClass.getMetadata().isAnnotated(Component.class.getName())) {
			// Recursively process any member (nested) classes first
			processMemberClasses(configClass, sourceClass);
		}

		// 处理 PropertySource 和 PropertySource 注解
		// Process any @PropertySource annotations
		for (AnnotationAttributes propertySource : AnnotationConfigUtils.attributesForRepeatable(
				sourceClass.getMetadata(), PropertySources.class,
				org.springframework.context.annotation.PropertySource.class)) {
			if (this.environment instanceof ConfigurableEnvironment) {
				processPropertySource(propertySource);
			}
			else {
				logger.info("Ignoring @PropertySource annotation on [" + sourceClass.getMetadata().getClassName() +
						"]. Reason: Environment must implement ConfigurableEnvironment");
			}
		}

		// 处理 ComponentScans ComponentScan 注解
		// Process any @ComponentScan annotations
		Set<AnnotationAttributes> componentScans = AnnotationConfigUtils.attributesForRepeatable(
				sourceClass.getMetadata(), ComponentScans.class, ComponentScan.class);
		// 如果 ComponentScan 数据解析结果不为空 并且通过条件解析，条件解析的阶段是注册阶段
		if (!componentScans.isEmpty() &&
				!this.conditionEvaluator.shouldSkip(sourceClass.getMetadata(), ConfigurationPhase.REGISTER_BEAN)) {
			// 处理单个 componentScan 数据
			for (AnnotationAttributes componentScan : componentScans) {
				// The config class is annotated with @ComponentScan -> perform the scan immediately
				Set<BeanDefinitionHolder> scannedBeanDefinitions =
						this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName());
				// Check the set of scanned definitions for any further config classes and parse recursively if needed
				for (BeanDefinitionHolder holder : scannedBeanDefinitions) {
					BeanDefinition bdCand = holder.getBeanDefinition().getOriginatingBeanDefinition();
					if (bdCand == null) {
						bdCand = holder.getBeanDefinition();
					}
					if (ConfigurationClassUtils.checkConfigurationClassCandidate(bdCand, this.metadataReaderFactory)) {
						parse(bdCand.getBeanClassName(), holder.getBeanName());
					}
				}
			}
		}

		// 处理 Import 注解
		// Process any @Import annotations
		processImports(configClass, sourceClass, getImports(sourceClass), true);

		// 处理 ImportResource 注解
		// Process any @ImportResource annotations
		AnnotationAttributes importResource =
				AnnotationConfigUtils.attributesFor(sourceClass.getMetadata(), ImportResource.class);
		if (importResource != null) {
			String[] resources = importResource.getStringArray("locations");
			Class<? extends BeanDefinitionReader> readerClass = importResource.getClass("reader");
			for (String resource : resources) {
				String resolvedResource = this.environment.resolveRequiredPlaceholders(resource);
				configClass.addImportedResource(resolvedResource, readerClass);
			}
		}

		// 处理 Bean 注解
		// Process individual @Bean methods
		Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(sourceClass);
		for (MethodMetadata methodMetadata : beanMethods) {
			configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
		}

		// 处理 Bean Method
		// Process default methods on interfaces
		processInterfaces(configClass, sourceClass);

		// 父类配置处理
		// Process superclass, if any
		if (sourceClass.getMetadata().hasSuperClass()) {
			String superclass = sourceClass.getMetadata().getSuperClassName();
			if (superclass != null && !superclass.startsWith("java") &&
					!this.knownSuperclasses.containsKey(superclass)) {
				this.knownSuperclasses.put(superclass, configClass);
				// Superclass found, return its annotation metadata and recurse
				return sourceClass.getSuperClass();
			}
		}

		// No superclass -> processing is complete
		return null;
	}

	/**
	 * Register member (nested) classes that happen to be configuration classes themselves.
	 */
	private void processMemberClasses(ConfigurationClass configClass, SourceClass sourceClass) throws IOException {
		// 找到当前配置类中存在的成员类
		Collection<SourceClass> memberClasses = sourceClass.getMemberClasses();
		// 成员类列表不为空
		if (!memberClasses.isEmpty()) {
			List<SourceClass> candidates = new ArrayList<>(memberClasses.size());
			for (SourceClass memberClass : memberClasses) {
				// 成员类是否符合配置类候选标准 Component ComponentScan Import ImportResource 注解是否存在
				// 成员类是否和配置类同名
				if (ConfigurationClassUtils.isConfigurationCandidate(memberClass.getMetadata()) &&
						!memberClass.getMetadata().getClassName().equals(configClass.getMetadata().getClassName())) {

					candidates.add(memberClass);
				}
			}
			// 排序候选类
			OrderComparator.sort(candidates);
			for (SourceClass candidate : candidates) {
				// 判断 importStack 中是否存在当前配置类
				if (this.importStack.contains(configClass)) {
					this.problemReporter.error(new CircularImportProblem(configClass, this.importStack));
				}
				else {
					this.importStack.push(configClass);
					try {
						// 解析成员类
						processConfigurationClass(candidate.asConfigClass(configClass));
					}
					finally {
						this.importStack.pop();
					}
				}
			}
		}
	}

	/**
	 * Register default methods on interfaces implemented by the configuration class.
	 */
	private void processInterfaces(ConfigurationClass configClass, SourceClass sourceClass) throws IOException {
		// 获取接口
		for (SourceClass ifc : sourceClass.getInterfaces()) {
			// 接口上获取方法元数据
			Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(ifc);
			for (MethodMetadata methodMetadata : beanMethods) {
				if (!methodMetadata.isAbstract()) {
					// A default method or other concrete method on a Java 8+ interface...
					configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
				}
			}
			processInterfaces(configClass, ifc);
		}
	}

	/**
	 * Retrieve the metadata for all <code>@Bean</code> methods.
	 */
	private Set<MethodMetadata> retrieveBeanMethodMetadata(SourceClass sourceClass) {
		// 提取元数据
		AnnotationMetadata original = sourceClass.getMetadata();
		// 提取带有 Bean 标签的数据
		Set<MethodMetadata> beanMethods = original.getAnnotatedMethods(Bean.class.getName());

		if (beanMethods.size() > 1 && original instanceof StandardAnnotationMetadata) {
			// Try reading the class file via ASM for deterministic declaration order...
			// Unfortunately, the JVM's standard reflection returns methods in arbitrary
			// order, even between different runs of the same application on the same JVM.
			try {
				// 类的注解元数据
				AnnotationMetadata asm =
						this.metadataReaderFactory.getMetadataReader(original.getClassName()).getAnnotationMetadata();
				// 提取带有 Bean 注解的方法元数据
				Set<MethodMetadata> asmMethods = asm.getAnnotatedMethods(Bean.class.getName());
				if (asmMethods.size() >= beanMethods.size()) {

					// 选中的方法元数据
					Set<MethodMetadata> selectedMethods = new LinkedHashSet<>(asmMethods.size());
					// 循环两个方法元数据进行 methodName 比较如果相同会被加入到选中集合中
					for (MethodMetadata asmMethod : asmMethods) {
						for (MethodMetadata beanMethod : beanMethods) {
							if (beanMethod.getMethodName().equals(asmMethod.getMethodName())) {
								selectedMethods.add(beanMethod);
								break;
							}
						}
					}
					if (selectedMethods.size() == beanMethods.size()) {
						// All reflection-detected methods found in ASM method set -> proceed
						beanMethods = selectedMethods;
					}
				}
			}
			catch (IOException ex) {
				logger.debug("Failed to read class file via ASM for determining @Bean method order", ex);
				// No worries, let's continue with the reflection metadata we started with...
			}
		}
		return beanMethods;
	}


	/**
	 * Process the given <code>@PropertySource</code> annotation metadata.
	 * @param propertySource metadata for the <code>@PropertySource</code> annotation found
	 * @throws IOException if loading a property source failed
	 */
	private void processPropertySource(AnnotationAttributes propertySource) throws IOException {
		// 提取注解属性中的 name 属性
		String name = propertySource.getString("name");
		if (!StringUtils.hasLength(name)) {
			name = null;
		}
		// 提取注解属性中的 encoding 属性
		String encoding = propertySource.getString("encoding");
		if (!StringUtils.hasLength(encoding)) {
			encoding = null;
		}
		// 提取注解属性中的 value 属性
		String[] locations = propertySource.getStringArray("value");
		Assert.isTrue(locations.length > 0, "At least one @PropertySource(value) location is required");

		// 提取注解属性中的 ignoreResourceNotFound 属性
		boolean ignoreResourceNotFound = propertySource.getBoolean("ignoreResourceNotFound");

		// 提取注解属性中的 factory 属性
		Class<? extends PropertySourceFactory> factoryClass = propertySource.getClass("factory");
		PropertySourceFactory factory = (factoryClass == PropertySourceFactory.class ?
				DEFAULT_PROPERTY_SOURCE_FACTORY : BeanUtils.instantiateClass(factoryClass));

		// 循环处理每个配置文件
		for (String location : locations) {
			try {
				// 解析配置文件地址
				String resolvedLocation = this.environment.resolveRequiredPlaceholders(location);
				// 资源加载其加载资源
				Resource resource = this.resourceLoader.getResource(resolvedLocation);
				// 加入配置
				addPropertySource(factory.createPropertySource(name, new EncodedResource(resource, encoding)));
			}
			catch (IllegalArgumentException | FileNotFoundException | UnknownHostException ex) {
				// Placeholders not resolvable or resource not found when trying to open it
				if (ignoreResourceNotFound) {
					if (logger.isInfoEnabled()) {
						logger.info("Properties location [" + location + "] not resolvable: " + ex.getMessage());
					}
				}
				else {
					throw ex;
				}
			}
		}
	}

	private void addPropertySource(PropertySource<?> propertySource) {
		// 获取配置名称
		String name = propertySource.getName();
		// 提取环境对象这种的 MutablePropertySources 对象
		MutablePropertySources propertySources = ((ConfigurableEnvironment) this.environment).getPropertySources();

		// 当前处理的配置名称是否在 propertySourceNames 存在
		if (this.propertySourceNames.contains(name)) {
			// We've already added a version, we need to extend it
			// 从 propertySources 中获取当前配置名称对应的 PropertySource
			PropertySource<?> existing = propertySources.get(name);
			if (existing != null) {
				PropertySource<?> newSource = (propertySource instanceof ResourcePropertySource ?
						((ResourcePropertySource) propertySource).withResourceName() : propertySource);
				if (existing instanceof CompositePropertySource) {
					((CompositePropertySource) existing).addFirstPropertySource(newSource);
				}
				else {
					if (existing instanceof ResourcePropertySource) {
						existing = ((ResourcePropertySource) existing).withResourceName();
					}
					CompositePropertySource composite = new CompositePropertySource(name);
					composite.addPropertySource(newSource);
					composite.addPropertySource(existing);
					propertySources.replace(name, composite);
				}
				return;
			}
		}

		// 如果 propertySourceNames 为空
		if (this.propertySourceNames.isEmpty()) {
			propertySources.addLast(propertySource);
		}
		else {
			String firstProcessed = this.propertySourceNames.get(this.propertySourceNames.size() - 1);
			propertySources.addBefore(firstProcessed, propertySource);
		}
		this.propertySourceNames.add(name);
	}


	/**
	 * Returns {@code @Import} class, considering all meta-annotations.
	 */
	private Set<SourceClass> getImports(SourceClass sourceClass) throws IOException {
		Set<SourceClass> imports = new LinkedHashSet<>();
		Set<SourceClass> visited = new LinkedHashSet<>();
		collectImports(sourceClass, imports, visited);
		return imports;
	}

	/**
	 * Recursively collect all declared {@code @Import} values. Unlike most
	 * meta-annotations it is valid to have several {@code @Import}s declared with
	 * different values; the usual process of returning values from the first
	 * meta-annotation on a class is not sufficient.
	 * <p>For example, it is common for a {@code @Configuration} class to declare direct
	 * {@code @Import}s in addition to meta-imports originating from an {@code @Enable}
	 * annotation.
	 * @param sourceClass the class to search
	 * @param imports the imports collected so far
	 * @param visited used to track visited classes to prevent infinite recursion
	 * @throws IOException if there is any problem reading metadata from the named class
	 */
	private void collectImports(SourceClass sourceClass, Set<SourceClass> imports, Set<SourceClass> visited)
			throws IOException {

		if (visited.add(sourceClass)) {
			for (SourceClass annotation : sourceClass.getAnnotations()) {
				String annName = annotation.getMetadata().getClassName();
				if (!annName.equals(Import.class.getName())) {
					collectImports(annotation, imports, visited);
				}
			}
			imports.addAll(sourceClass.getAnnotationAttributes(Import.class.getName(), "value"));
		}
	}

	private void processImports(ConfigurationClass configClass, SourceClass currentSourceClass,
			Collection<SourceClass> importCandidates, boolean checkForCircularImports) {

		// 判断是否存在需要处理的 ImportSelector 集合, 如果不需要则不处理直接返回
		if (importCandidates.isEmpty()) {
			return;
		}

		// 判断是否需要进行import循环检查
		// 判断但钱配置类是否在 importStack 中
		if (checkForCircularImports && isChainedImportOnStack(configClass)) {
			this.problemReporter.error(new CircularImportProblem(configClass, this.importStack));
		}
		else {
			// 向 importStack 中加入当前正在处理的配置类
			this.importStack.push(configClass);
			try {
				// 循环处理参数传递过来的 importSelector
				for (SourceClass candidate : importCandidates) {

					// 判断
					if (candidate.isAssignable(ImportSelector.class)) {
						// Candidate class is an ImportSelector -> delegate to it to determine imports
						Class<?> candidateClass = candidate.loadClass();
						// 将 class 转换成对象
						ImportSelector selector = ParserStrategyUtils.instantiateClass(candidateClass, ImportSelector.class,
								this.environment, this.resourceLoader, this.registry);
						// 如果类型是 DeferredImportSelector
						if (selector instanceof DeferredImportSelector) {
							this.deferredImportSelectorHandler.handle(configClass, (DeferredImportSelector) selector);
						}
						else {
							// 获取需要导入的类
							String[] importClassNames = selector.selectImports(currentSourceClass.getMetadata());
							// 将需要导入的类从字符串转换成 SourceClass
							Collection<SourceClass> importSourceClasses = asSourceClasses(importClassNames);
							// 递归处理需要导入的类
							processImports(configClass, currentSourceClass, importSourceClasses, false);
						}
					}

					// 处理类型是 ImportBeanDefinitionRegistrar 的情况
					else if (candidate.isAssignable(ImportBeanDefinitionRegistrar.class)) {
						// Candidate class is an ImportBeanDefinitionRegistrar ->
						// delegate to it to register additional bean definitions
						Class<?> candidateClass = candidate.loadClass();
						ImportBeanDefinitionRegistrar registrar =
								ParserStrategyUtils.instantiateClass(candidateClass, ImportBeanDefinitionRegistrar.class,
										this.environment, this.resourceLoader, this.registry);
						configClass.addImportBeanDefinitionRegistrar(registrar, currentSourceClass.getMetadata());
					}
					// 其他情况
					else {
						// Candidate class not an ImportSelector or ImportBeanDefinitionRegistrar ->
						// process it as an @Configuration class
						this.importStack.registerImport(
								currentSourceClass.getMetadata(), candidate.getMetadata().getClassName());
						processConfigurationClass(candidate.asConfigClass(configClass));
					}
				}
			}
			catch (BeanDefinitionStoreException ex) {
				throw ex;
			}
			catch (Throwable ex) {
				throw new BeanDefinitionStoreException(
						"Failed to process import candidates for configuration class [" +
						configClass.getMetadata().getClassName() + "]", ex);
			}
			finally {
				// 从 importStack 删除配置类
				this.importStack.pop();
			}
		}
	}

	private boolean isChainedImportOnStack(ConfigurationClass configClass) {
		if (this.importStack.contains(configClass)) {
			String configClassName = configClass.getMetadata().getClassName();
			AnnotationMetadata importingClass = this.importStack.getImportingClassFor(configClassName);
			while (importingClass != null) {
				if (configClassName.equals(importingClass.getClassName())) {
					return true;
				}
				importingClass = this.importStack.getImportingClassFor(importingClass.getClassName());
			}
		}
		return false;
	}

	ImportRegistry getImportRegistry() {
		return this.importStack;
	}


	/**
	 * Factory method to obtain a {@link SourceClass} from a {@link ConfigurationClass}.
	 */
	private SourceClass asSourceClass(ConfigurationClass configurationClass) throws IOException {
		AnnotationMetadata metadata = configurationClass.getMetadata();
		if (metadata instanceof StandardAnnotationMetadata) {
			return asSourceClass(((StandardAnnotationMetadata) metadata).getIntrospectedClass());
		}
		return asSourceClass(metadata.getClassName());
	}

	/**
	 * Factory method to obtain a {@link SourceClass} from a {@link Class}.
	 */
	SourceClass asSourceClass(@Nullable Class<?> classType) throws IOException {
		if (classType == null || classType.getName().startsWith("java.lang.annotation.")) {
			return this.objectSourceClass;
		}
		try {
			// Sanity test that we can reflectively read annotations,
			// including Class attributes; if not -> fall back to ASM
			for (Annotation ann : classType.getDeclaredAnnotations()) {
				AnnotationUtils.validateAnnotation(ann);
			}
			return new SourceClass(classType);
		}
		catch (Throwable ex) {
			// Enforce ASM via class name resolution
			return asSourceClass(classType.getName());
		}
	}

	/**
	 * Factory method to obtain {@link SourceClass SourceClasss} from class names.
	 */
	private Collection<SourceClass> asSourceClasses(String... classNames) throws IOException {
		List<SourceClass> annotatedClasses = new ArrayList<>(classNames.length);
		for (String className : classNames) {
			annotatedClasses.add(asSourceClass(className));
		}
		return annotatedClasses;
	}

	/**
	 * Factory method to obtain a {@link SourceClass} from a class name.
	 */
	SourceClass asSourceClass(@Nullable String className) throws IOException {
		if (className == null || className.startsWith("java.lang.annotation.")) {
			return this.objectSourceClass;
		}
		if (className.startsWith("java")) {
			// Never use ASM for core java types
			try {
				return new SourceClass(ClassUtils.forName(className,
						this.resourceLoader.getClassLoader()));
			}
			catch (ClassNotFoundException ex) {
				throw new NestedIOException(
						"Failed to load class [" + className + "]", ex);
			}
		}
		return new SourceClass(
				this.metadataReaderFactory.getMetadataReader(className));
	}


	@SuppressWarnings("serial")
	private static class ImportStack extends ArrayDeque<ConfigurationClass> implements ImportRegistry {

		private final MultiValueMap<String, AnnotationMetadata> imports = new LinkedMultiValueMap<>();

		public void registerImport(AnnotationMetadata importingClass, String importedClass) {
			this.imports.add(importedClass, importingClass);
		}

		@Override
		@Nullable
		public AnnotationMetadata getImportingClassFor(String importedClass) {
			return CollectionUtils.lastElement(this.imports.get(importedClass));
		}

		@Override
		public void removeImportingClass(String importingClass) {
			for (List<AnnotationMetadata> list : this.imports.values()) {
				for (Iterator<AnnotationMetadata> iterator = list.iterator(); iterator.hasNext();) {
					if (iterator.next().getClassName().equals(importingClass)) {
						iterator.remove();
						break;
					}
				}
			}
		}

		/**
		 * Given a stack containing (in order)
		 * <ul>
		 * <li>com.acme.Foo</li>
		 * <li>com.acme.Bar</li>
		 * <li>com.acme.Baz</li>
		 * </ul>
		 * return "[Foo->Bar->Baz]".
		 */
		@Override
		public String toString() {
			StringJoiner joiner = new StringJoiner("->", "[", "]");
			for (ConfigurationClass configurationClass : this) {
				joiner.add(configurationClass.getSimpleName());
			}
			return joiner.toString();
		}
	}


	private class DeferredImportSelectorHandler {

		@Nullable
		private List<DeferredImportSelectorHolder> deferredImportSelectors = new ArrayList<>();

		/**
		 * Handle the specified {@link DeferredImportSelector}. If deferred import
		 * selectors are being collected, this registers this instance to the list. If
		 * they are being processed, the {@link DeferredImportSelector} is also processed
		 * immediately according to its {@link DeferredImportSelector.Group}.
		 * @param configClass the source configuration class
		 * @param importSelector the selector to handle
		 */
		public void handle(ConfigurationClass configClass, DeferredImportSelector importSelector) {
			DeferredImportSelectorHolder holder = new DeferredImportSelectorHolder(
					configClass, importSelector);
			if (this.deferredImportSelectors == null) {
				DeferredImportSelectorGroupingHandler handler = new DeferredImportSelectorGroupingHandler();
				handler.register(holder);
				handler.processGroupImports();
			}
			else {
				this.deferredImportSelectors.add(holder);
			}
		}

		public void process() {
			List<DeferredImportSelectorHolder> deferredImports = this.deferredImportSelectors;
			this.deferredImportSelectors = null;
			try {
				if (deferredImports != null) {
					DeferredImportSelectorGroupingHandler handler = new DeferredImportSelectorGroupingHandler();
					deferredImports.sort(DEFERRED_IMPORT_COMPARATOR);
					deferredImports.forEach(handler::register);
					handler.processGroupImports();
				}
			}
			finally {
				this.deferredImportSelectors = new ArrayList<>();
			}
		}

	}


	private class DeferredImportSelectorGroupingHandler {

		private final Map<Object, DeferredImportSelectorGrouping> groupings = new LinkedHashMap<>();

		private final Map<AnnotationMetadata, ConfigurationClass> configurationClasses = new HashMap<>();

		public void register(DeferredImportSelectorHolder deferredImport) {
			Class<? extends Group> group = deferredImport.getImportSelector()
					.getImportGroup();
			DeferredImportSelectorGrouping grouping = this.groupings.computeIfAbsent(
					(group != null ? group : deferredImport),
					key -> new DeferredImportSelectorGrouping(createGroup(group)));
			grouping.add(deferredImport);
			this.configurationClasses.put(deferredImport.getConfigurationClass().getMetadata(),
					deferredImport.getConfigurationClass());
		}

		public void processGroupImports() {
			for (DeferredImportSelectorGrouping grouping : this.groupings.values()) {
				grouping.getImports().forEach(entry -> {
					ConfigurationClass configurationClass = this.configurationClasses.get(
							entry.getMetadata());
					try {
						processImports(configurationClass, asSourceClass(configurationClass),
								asSourceClasses(entry.getImportClassName()), false);
					}
					catch (BeanDefinitionStoreException ex) {
						throw ex;
					}
					catch (Throwable ex) {
						throw new BeanDefinitionStoreException(
								"Failed to process import candidates for configuration class [" +
										configurationClass.getMetadata().getClassName() + "]", ex);
					}
				});
			}
		}

		private Group createGroup(@Nullable Class<? extends Group> type) {
			Class<? extends Group> effectiveType = (type != null ? type
					: DefaultDeferredImportSelectorGroup.class);
			Group group = ParserStrategyUtils.instantiateClass(effectiveType, Group.class,
					ConfigurationClassParser.this.environment,
					ConfigurationClassParser.this.resourceLoader,
					ConfigurationClassParser.this.registry);
			return group;
		}

	}


	private static class DeferredImportSelectorHolder {

		private final ConfigurationClass configurationClass;

		private final DeferredImportSelector importSelector;

		public DeferredImportSelectorHolder(ConfigurationClass configClass, DeferredImportSelector selector) {
			this.configurationClass = configClass;
			this.importSelector = selector;
		}

		public ConfigurationClass getConfigurationClass() {
			return this.configurationClass;
		}

		public DeferredImportSelector getImportSelector() {
			return this.importSelector;
		}
	}


	private static class DeferredImportSelectorGrouping {

		private final DeferredImportSelector.Group group;

		private final List<DeferredImportSelectorHolder> deferredImports = new ArrayList<>();

		DeferredImportSelectorGrouping(Group group) {
			this.group = group;
		}

		public void add(DeferredImportSelectorHolder deferredImport) {
			this.deferredImports.add(deferredImport);
		}

		/**
		 * Return the imports defined by the group.
		 * @return each import with its associated configuration class
		 */
		public Iterable<Group.Entry> getImports() {
			for (DeferredImportSelectorHolder deferredImport : this.deferredImports) {
				this.group.process(deferredImport.getConfigurationClass().getMetadata(),
						deferredImport.getImportSelector());
			}
			return this.group.selectImports();
		}
	}


	private static class DefaultDeferredImportSelectorGroup implements Group {

		private final List<Entry> imports = new ArrayList<>();

		@Override
		public void process(AnnotationMetadata metadata, DeferredImportSelector selector) {
			for (String importClassName : selector.selectImports(metadata)) {
				this.imports.add(new Entry(metadata, importClassName));
			}
		}

		@Override
		public Iterable<Entry> selectImports() {
			return this.imports;
		}
	}


	/**
	 * Simple wrapper that allows annotated source classes to be dealt with
	 * in a uniform manner, regardless of how they are loaded.
	 */
	private class SourceClass implements Ordered {

		private final Object source;  // Class or MetadataReader

		private final AnnotationMetadata metadata;

		public SourceClass(Object source) {
			this.source = source;
			if (source instanceof Class) {
				this.metadata = AnnotationMetadata.introspect((Class<?>) source);
			}
			else {
				this.metadata = ((MetadataReader) source).getAnnotationMetadata();
			}
		}

		public final AnnotationMetadata getMetadata() {
			return this.metadata;
		}

		@Override
		public int getOrder() {
			Integer order = ConfigurationClassUtils.getOrder(this.metadata);
			return (order != null ? order : Ordered.LOWEST_PRECEDENCE);
		}

		public Class<?> loadClass() throws ClassNotFoundException {
			if (this.source instanceof Class) {
				return (Class<?>) this.source;
			}
			String className = ((MetadataReader) this.source).getClassMetadata().getClassName();
			return ClassUtils.forName(className, resourceLoader.getClassLoader());
		}

		public boolean isAssignable(Class<?> clazz) throws IOException {
			if (this.source instanceof Class) {
				return clazz.isAssignableFrom((Class<?>) this.source);
			}
			return new AssignableTypeFilter(clazz).match((MetadataReader) this.source, metadataReaderFactory);
		}

		public ConfigurationClass asConfigClass(ConfigurationClass importedBy) {
			if (this.source instanceof Class) {
				return new ConfigurationClass((Class<?>) this.source, importedBy);
			}
			return new ConfigurationClass((MetadataReader) this.source, importedBy);
		}

		public Collection<SourceClass> getMemberClasses() throws IOException {
			Object sourceToProcess = this.source;
			if (sourceToProcess instanceof Class) {
				Class<?> sourceClass = (Class<?>) sourceToProcess;
				try {
					Class<?>[] declaredClasses = sourceClass.getDeclaredClasses();
					List<SourceClass> members = new ArrayList<>(declaredClasses.length);
					for (Class<?> declaredClass : declaredClasses) {
						members.add(asSourceClass(declaredClass));
					}
					return members;
				}
				catch (NoClassDefFoundError err) {
					// getDeclaredClasses() failed because of non-resolvable dependencies
					// -> fall back to ASM below
					sourceToProcess = metadataReaderFactory.getMetadataReader(sourceClass.getName());
				}
			}

			// ASM-based resolution - safe for non-resolvable classes as well
			MetadataReader sourceReader = (MetadataReader) sourceToProcess;
			String[] memberClassNames = sourceReader.getClassMetadata().getMemberClassNames();
			List<SourceClass> members = new ArrayList<>(memberClassNames.length);
			for (String memberClassName : memberClassNames) {
				try {
					members.add(asSourceClass(memberClassName));
				}
				catch (IOException ex) {
					// Let's skip it if it's not resolvable - we're just looking for candidates
					if (logger.isDebugEnabled()) {
						logger.debug("Failed to resolve member class [" + memberClassName +
								"] - not considering it as a configuration class candidate");
					}
				}
			}
			return members;
		}

		public SourceClass getSuperClass() throws IOException {
			if (this.source instanceof Class) {
				return asSourceClass(((Class<?>) this.source).getSuperclass());
			}
			return asSourceClass(((MetadataReader) this.source).getClassMetadata().getSuperClassName());
		}

		public Set<SourceClass> getInterfaces() throws IOException {
			Set<SourceClass> result = new LinkedHashSet<>();
			if (this.source instanceof Class) {
				Class<?> sourceClass = (Class<?>) this.source;
				for (Class<?> ifcClass : sourceClass.getInterfaces()) {
					result.add(asSourceClass(ifcClass));
				}
			}
			else {
				for (String className : this.metadata.getInterfaceNames()) {
					result.add(asSourceClass(className));
				}
			}
			return result;
		}

		public Set<SourceClass> getAnnotations() {
			Set<SourceClass> result = new LinkedHashSet<>();
			if (this.source instanceof Class) {
				Class<?> sourceClass = (Class<?>) this.source;
				for (Annotation ann : sourceClass.getDeclaredAnnotations()) {
					Class<?> annType = ann.annotationType();
					if (!annType.getName().startsWith("java")) {
						try {
							result.add(asSourceClass(annType));
						}
						catch (Throwable ex) {
							// An annotation not present on the classpath is being ignored
							// by the JVM's class loading -> ignore here as well.
						}
					}
				}
			}
			else {
				for (String className : this.metadata.getAnnotationTypes()) {
					if (!className.startsWith("java")) {
						try {
							result.add(getRelated(className));
						}
						catch (Throwable ex) {
							// An annotation not present on the classpath is being ignored
							// by the JVM's class loading -> ignore here as well.
						}
					}
				}
			}
			return result;
		}

		public Collection<SourceClass> getAnnotationAttributes(String annType, String attribute) throws IOException {
			Map<String, Object> annotationAttributes = this.metadata.getAnnotationAttributes(annType, true);
			if (annotationAttributes == null || !annotationAttributes.containsKey(attribute)) {
				return Collections.emptySet();
			}
			String[] classNames = (String[]) annotationAttributes.get(attribute);
			Set<SourceClass> result = new LinkedHashSet<>();
			for (String className : classNames) {
				result.add(getRelated(className));
			}
			return result;
		}

		private SourceClass getRelated(String className) throws IOException {
			if (this.source instanceof Class) {
				try {
					Class<?> clazz = ClassUtils.forName(className, ((Class<?>) this.source).getClassLoader());
					return asSourceClass(clazz);
				}
				catch (ClassNotFoundException ex) {
					// Ignore -> fall back to ASM next, except for core java types.
					if (className.startsWith("java")) {
						throw new NestedIOException("Failed to load class [" + className + "]", ex);
					}
					return new SourceClass(metadataReaderFactory.getMetadataReader(className));
				}
			}
			return asSourceClass(className);
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof SourceClass &&
					this.metadata.getClassName().equals(((SourceClass) other).metadata.getClassName())));
		}

		@Override
		public int hashCode() {
			return this.metadata.getClassName().hashCode();
		}

		@Override
		public String toString() {
			return this.metadata.getClassName();
		}
	}


	/**
	 * {@link Problem} registered upon detection of a circular {@link Import}.
	 */
	private static class CircularImportProblem extends Problem {

		public CircularImportProblem(ConfigurationClass attemptedImport, Deque<ConfigurationClass> importStack) {
			super(String.format("A circular @Import has been detected: " +
					"Illegal attempt by @Configuration class '%s' to import class '%s' as '%s' is " +
					"already present in the current import stack %s", importStack.element().getSimpleName(),
					attemptedImport.getSimpleName(), attemptedImport.getSimpleName(), importStack),
					new Location(importStack.element().getResource(), attemptedImport.getMetadata()));
		}
	}

}
