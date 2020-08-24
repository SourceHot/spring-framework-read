# Spring Conditional
- Author: [HuiFer](https://github.com/huifer)
- 源码阅读仓库: [SourceHot-spring](https://github.com/SourceHot/spring-framework-read)



## Conditional

```java
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conditional {

    /**
     * 多个匹配器接口
     */
    Class<? extends Condition>[] value();

}
```



## Condition

```
@FunctionalInterface
public interface Condition {

    /**
     * 匹配,如果匹配返回true进行初始化,返回false跳过初始化
     */
    boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata);

}
```



- ConditionContext 上下文
- AnnotatedTypeMetadata 注解信息

### ConditionContext

```
public interface ConditionContext {

   /**
     * bean的定义
    */
   BeanDefinitionRegistry getRegistry();

   /**
     * bean 工厂
    */
   @Nullable
   ConfigurableListableBeanFactory getBeanFactory();

   /**
     * 环境
    */
   Environment getEnvironment();

   /**
     * 资源加载器
    */
   ResourceLoader getResourceLoader();

   /**
     * 类加载器
    */
   @Nullable
   ClassLoader getClassLoader();

}
```

- 唯一实现 : `org.springframework.context.annotation.ConditionEvaluator.ConditionContextImpl`



- 构造方法

```java
public ConditionContextImpl(@Nullable BeanDefinitionRegistry registry,
      @Nullable Environment environment, @Nullable ResourceLoader resourceLoader) {

   this.registry = registry;
   this.beanFactory = deduceBeanFactory(registry);
   this.environment = (environment != null ? environment : deduceEnvironment(registry));
   this.resourceLoader = (resourceLoader != null ? resourceLoader : deduceResourceLoader(registry));
   this.classLoader = deduceClassLoader(resourceLoader, this.beanFactory);
}
```

- 在构造方法中加载了一些变量, 这些变量是根据一定规则转换后得到. 具体规则不展开.







### AnnotatedTypeMetadata 

- 元数据接口