# Spring ListableBeanFactory
- 类全路径: `org.springframework.beans.factory.ListableBeanFactory`

- `ListableBeanFactory` 继承自 `BeanFactory`  , 增强了 `BeanFactory` 在原有基础上补充下面这些方法,

## 方法列表


```java
public interface ListableBeanFactory extends BeanFactory {

	/**
	 *
	 * 是否存在bean定义
	 */
	boolean containsBeanDefinition(String beanName);

	/**
	 * 获取bean定义的数量
	 */
	int getBeanDefinitionCount();

	/**
	 * 获取bean定义的名字列表
	 */
	String[] getBeanDefinitionNames();

	/**
	 * 获取类型对应的bean名称
	 */
	String[] getBeanNamesForType(ResolvableType type);

	/**
	 * 获取类型对应的bean名称
	 */
	String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit);

	/**
	 * 获取类型对应的bean名称
	 */
	String[] getBeanNamesForType(@Nullable Class<?> type);

	/**
	 * 根据类获取 beanName
	 */
	String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);

	/**
	 * 根据类型获取bean
	 */
	<T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException;

	/**
	 * 获取类型对应的bean名称
	 */
	<T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException;

	/**
	 * 根据注解返回bean名称
	 */
	String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType);

	/**
	 * 获取带有注解的bean
	 */
	Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;

	/**
	 * 通过 bean 名称 + 注解类型, 获取注解. 即通过bean获取注解
	 */
	@Nullable
	<A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException;

}
```