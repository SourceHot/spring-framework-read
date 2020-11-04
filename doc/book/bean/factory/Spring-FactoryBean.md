# Spring FactoryBean 



- 类全路径: `org.springframework.beans.factory.FactoryBean`

```java
public interface FactoryBean<T> {

   /**
    * object 类型
    */
   String OBJECT_TYPE_ATTRIBUTE = "factoryBeanObjectType";


   /**
    *获取对象
    */
   @Nullable
   T getObject() throws Exception;

   /**
    * 是否单例
    */
   default boolean isSingleton() {
      return true;
   }

}
```



- 这里我们找几个类来了解一下



## GsonFactoryBean



```java
public class GsonFactoryBean implements FactoryBean<Gson>, InitializingBean {
    
    // 省略其他代码
    
    /**
	 * Return the created Gson instance.
	 */
	@Override
	@Nullable
	public Gson getObject() {
		return this.gson;
	}

	@Override
	public Class<?> getObjectType() {
		return Gson.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
```





## Jackson2ObjectMapperFactoryBean

```java
public class Jackson2ObjectMapperFactoryBean implements FactoryBean<ObjectMapper>, BeanClassLoaderAware,
      ApplicationContextAware, InitializingBean {
          
          // 省略其他
          	@Override
            @Nullable
            public ObjectMapper getObject() {
                return this.objectMapper;
            }

            @Override
            public Class<?> getObjectType() {
                return (this.objectMapper != null ? this.objectMapper.getClass() : null);
            }

            @Override
            public boolean isSingleton() {
                return true;
            }
          
      }
```