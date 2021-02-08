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

package org.springframework.context.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;

/**
 * Internal class used to evaluate {@link Conditional} annotations.
 *
 * @author Phillip Webb
 * @author Juergen Hoeller
 * @since 4.0
 */
class ConditionEvaluator {

    private final ConditionContextImpl context;


    /**
     * Create a new {@link ConditionEvaluator} instance.
     */
    public ConditionEvaluator(@Nullable BeanDefinitionRegistry registry,
            @Nullable Environment environment, @Nullable ResourceLoader resourceLoader) {

        this.context = new ConditionContextImpl(registry, environment, resourceLoader);
    }


    /**
     * Determine if an item should be skipped based on {@code @Conditional} annotations.
     * The {@link ConfigurationPhase} will be deduced from the type of item (i.e. a
     * {@code @Configuration} class will be {@link ConfigurationPhase#PARSE_CONFIGURATION})
     * @param metadata the meta data
     * @return if the item should be skipped
     *
     * 是否需要跳过当前类的初始化
     */
    public boolean shouldSkip(AnnotatedTypeMetadata metadata) {
        return shouldSkip(metadata, null);
    }

	/**
	 * Determine if an item should be skipped based on {@code @Conditional} annotations.
	 * @param metadata the meta data
	 * @param phase the phase of the call
	 * @return if the item should be skipped
	 * 是否需要跳过当前类的初始化
	 */
	public boolean shouldSkip(@Nullable AnnotatedTypeMetadata metadata, @Nullable ConfigurationPhase phase) {
		// 注解元数据不存在或者注解元数据中不包含Conditional注解
		if (metadata == null || !metadata.isAnnotated(Conditional.class.getName())) {
			return false;
		}

		// 配置解析阶段处理
		if (phase == null) {
			// 注解元数据据类型是 AnnotationMetadata
			// 注解元数据中存在 Bean Component ComponentScan Import ImportResource
			if (metadata instanceof AnnotationMetadata &&
					ConfigurationClassUtils.isConfigurationCandidate((AnnotationMetadata) metadata)) {
				// 配置解析阶段
				return shouldSkip(metadata, ConfigurationPhase.PARSE_CONFIGURATION);
			}
			// 注册 Bean 阶段
			return shouldSkip(metadata, ConfigurationPhase.REGISTER_BEAN);
		}

		// 需要处理的 Condition , 数据从注解 Conditional 中来
		List<Condition> conditions = new ArrayList<>();
		// 获取注解 Conditional 的属性值
		for (String[] conditionClasses : getConditionClasses(metadata)) {
			for (String conditionClass : conditionClasses) {
				// 将注解中的数据转换成 Condition 接口
				// 从 class 转换成实例
				Condition condition = getCondition(conditionClass, this.context.getClassLoader());
				// 插入注解列表
				conditions.add(condition);
			}
		}

		// 对 Condition 进行排序
		AnnotationAwareOrderComparator.sort(conditions);

		// 执行 Condition 得到验证结果
		for (Condition condition : conditions) {
			ConfigurationPhase requiredPhase = null;
			// 如果类型是 ConfigurationCondition
			if (condition instanceof ConfigurationCondition) {
				requiredPhase = ((ConfigurationCondition) condition).getConfigurationPhase();
			}

			// matches 进行验证
			if ((requiredPhase == null || requiredPhase == phase) && !condition.matches(this.context, metadata)) {
				return true;
			}
		}

		return false;
	}

    @SuppressWarnings("unchecked")
    private List<String[]> getConditionClasses(AnnotatedTypeMetadata metadata) {
        MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(Conditional.class.getName(), true);
        Object values = (attributes != null ? attributes.get("value") : null);
        return (List<String[]>) (values != null ? values : Collections.emptyList());
    }

    private Condition getCondition(String conditionClassName, @Nullable ClassLoader classloader) {
        Class<?> conditionClass = ClassUtils.resolveClassName(conditionClassName, classloader);
        return (Condition) BeanUtils.instantiateClass(conditionClass);
    }


    /**
     * Implementation of a {@link ConditionContext}.
     */
    private static class ConditionContextImpl implements ConditionContext {

        @Nullable
        private final BeanDefinitionRegistry registry;

        @Nullable
        private final ConfigurableListableBeanFactory beanFactory;

        private final Environment environment;

        private final ResourceLoader resourceLoader;

        @Nullable
        private final ClassLoader classLoader;

        public ConditionContextImpl(@Nullable BeanDefinitionRegistry registry,
                @Nullable Environment environment, @Nullable ResourceLoader resourceLoader) {

            this.registry = registry;
            this.beanFactory = deduceBeanFactory(registry);
            this.environment = (environment != null ? environment : deduceEnvironment(registry));
            this.resourceLoader = (resourceLoader != null ? resourceLoader : deduceResourceLoader(registry));
            this.classLoader = deduceClassLoader(resourceLoader, this.beanFactory);
        }

        @Nullable
        private ConfigurableListableBeanFactory deduceBeanFactory(@Nullable BeanDefinitionRegistry source) {
            if (source instanceof ConfigurableListableBeanFactory) {
                return (ConfigurableListableBeanFactory) source;
            }
            if (source instanceof ConfigurableApplicationContext) {
                return (((ConfigurableApplicationContext) source).getBeanFactory());
            }
            return null;
        }

        private Environment deduceEnvironment(@Nullable BeanDefinitionRegistry source) {
            if (source instanceof EnvironmentCapable) {
                return ((EnvironmentCapable) source).getEnvironment();
            }
            return new StandardEnvironment();
        }

        private ResourceLoader deduceResourceLoader(@Nullable BeanDefinitionRegistry source) {
            if (source instanceof ResourceLoader) {
                return (ResourceLoader) source;
            }
            return new DefaultResourceLoader();
        }

        @Nullable
        private ClassLoader deduceClassLoader(@Nullable ResourceLoader resourceLoader,
                @Nullable ConfigurableListableBeanFactory beanFactory) {

            if (resourceLoader != null) {
                ClassLoader classLoader = resourceLoader.getClassLoader();
                if (classLoader != null) {
                    return classLoader;
                }
            }
            if (beanFactory != null) {
                return beanFactory.getBeanClassLoader();
            }
            return ClassUtils.getDefaultClassLoader();
        }

        @Override
        public BeanDefinitionRegistry getRegistry() {
            Assert.state(this.registry != null, "No BeanDefinitionRegistry available");
            return this.registry;
        }

        @Override
        @Nullable
        public ConfigurableListableBeanFactory getBeanFactory() {
            return this.beanFactory;
        }

        @Override
        public Environment getEnvironment() {
            return this.environment;
        }

        @Override
        public ResourceLoader getResourceLoader() {
            return this.resourceLoader;
        }

        @Override
        @Nullable
        public ClassLoader getClassLoader() {
            return this.classLoader;
        }
    }

}
