# Spring BeanDefinitionReader
- 类全路径: `org.springframework.beans.factory.support.BeanDefinitionReader`



- `BeanDefinitionReader` 作为接口本文仅介绍其提供的方法定义 . 详细请查看下面文档

```java
public interface BeanDefinitionReader {

	/**
	 * 获取 bean 定义注册器
	 */
	BeanDefinitionRegistry getRegistry();

	/**
	 * 获取资源加载器
	 */
	@Nullable
	ResourceLoader getResourceLoader();

	/**
	 *
	 * 获取类加载器
	 */
	@Nullable
	ClassLoader getBeanClassLoader();

	/**
	 *
	 * 获取 bean 名称乘乘其
	 */
	BeanNameGenerator getBeanNameGenerator();


	/**
	 *
	 * 获取资源中定义的的bean数量
	 */
	int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException;

	/**
	 *
	 * 获取资源中定义的的bean数量
	 */
	int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException;

	/**
	 *
	 * 获取资源中定义的的bean数量
	 */
	int loadBeanDefinitions(String location) throws BeanDefinitionStoreException;

	/**
	 * 获取资源中定义的的bean数量
	 */
	int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException;

}
```