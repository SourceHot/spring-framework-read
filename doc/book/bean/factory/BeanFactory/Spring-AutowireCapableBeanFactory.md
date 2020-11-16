# Spring AutowireCapableBeanFactory
- 类全路径: `org.springframework.beans.factory.config.AutowireCapableBeanFactory`
- 在`AutowireCapableBeanFactory`定义了5种注入方式
    1.	不采取自动注入
    2.	根据名称自动注入
    3.	根据 类型自动注入
    4.	通过构造函数进行自动注入
    5. 	类型+构造函数自动注入
    
    
```java
public interface AutowireCapableBeanFactory extends BeanFactory {

    /**
	 * Constant that indicates no externally defined autowiring. Note that
	 * BeanFactoryAware etc and annotation-driven injection will still be applied.
	 *
	 * 不采取自动注入
	 * @see #createBean
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_NO = 0;

	/**
	 * Constant that indicates autowiring bean properties by name
	 * (applying to all bean property setters).
	 *
	 * 根据名称自动注入
	 * @see #createBean
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_BY_NAME = 1;

	/**
	 * Constant that indicates autowiring bean properties by type
	 * (applying to all bean property setters).
	 *
	 * 根据 类型自动注入
	 * @see #createBean
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_BY_TYPE = 2;

	/**
	 * Constant that indicates autowiring the greediest constructor that
	 * can be satisfied (involves resolving the appropriate constructor).
	 * 通过构造函数进行自动注入
	 * @see #createBean
	 * @see #autowire
	 */
	int AUTOWIRE_CONSTRUCTOR = 3;

	/**
	 * Constant that indicates determining an appropriate autowire strategy
	 * through introspection of the bean class.
	 * 类型+构造函数自动注入
	 * @see #createBean
	 * @see #autowire
	 * @deprecated as of Spring 3.0: If you are using mixed autowiring strategies,
	 * prefer annotation-based autowiring for clearer demarcation of autowiring needs.
	 */
	@Deprecated
	int AUTOWIRE_AUTODETECT = 4;

}
```


在提供注入方式的静态变量之外, `AutowireCapableBeanFactory` 也同时定义了下面这些方法

## 方法列表


```java
public interface AutowireCapableBeanFactory extends BeanFactory {

	/**
	 * 创建 bean
	 */
	<T> T createBean(Class<T> beanClass) throws BeansException;

	/**
	 * 自动注入bean
	 */
	void autowireBean(Object existingBean) throws BeansException;

	/**
	 * 配置bean
	 */
	Object configureBean(Object existingBean, String beanName) throws BeansException;


	/**
	 * 创建bean
	 */
	Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

	/**
	 * 自动装配
	 */
	Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

	/**
	 * 自动装配bean属性
	 */
	void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException;

	/**
	 * 应用 bean 属性
	 */
	void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException;

	/**
	 * 实例化 bean
	 */
	Object initializeBean(Object existingBean, String beanName) throws BeansException;

	/**
	 * 在初始化之前使用Bean后处理器
	 */
	Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
			throws BeansException;

	/**
	 * 在初始化之后使用Bean后处理器
	 */
	Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
			throws BeansException;

	/**
	 * 摧毁bean
	 */
	void destroyBean(Object existingBean);


	//-------------------------------------------------------------------------
	// Delegate methods for resolving injection points
	//-------------------------------------------------------------------------

	/**
	 * 解析 bean name
	 */
	<T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException;

	/**
	 * 解析 bean name
	 */
	Object resolveBeanByName(String name, DependencyDescriptor descriptor) throws BeansException;

	/**
	 * 解析依赖
	 */
	@Nullable
	Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName) throws BeansException;

	/**
	 * 解析依赖
	 */
	@Nullable
	Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException;
}
```