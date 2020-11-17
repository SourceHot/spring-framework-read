# Spring HierarchicalBeanFactory
- 类全路径: `org.springframework.beans.factory.HierarchicalBeanFactory`
- `HierarchicalBeanFactory` 提供了两个方法
    1. `getParentBeanFactory`: 获取父 bean factory
    2. `containsLocalBean`: 判断是否存在本地 bean
    
    
    
```java
public interface HierarchicalBeanFactory extends BeanFactory {

	/**
	 * Return the parent bean factory, or {@code null} if there is none.
	 * 获取父 bean factory
	 */
	@Nullable
	BeanFactory getParentBeanFactory();

	/**
	 * Return whether the local bean factory contains a bean of the given name,
	 * ignoring beans defined in ancestor contexts.
	 * <p>This is an alternative to {@code containsBean}, ignoring a bean
	 * of the given name from an ancestor bean factory.
	 * 当前bean工厂是否包含 beanName
	 * @param name the name of the bean to query
	 * @return whether a bean with the given name is defined in the local factory
	 * @see BeanFactory#containsBean
	 */
	boolean containsLocalBean(String name);

}

```