# Spring SingletonBeanRegistry

- 类全路径: `org.springframework.beans.factory.config.SingletonBeanRegistry`







## 方法列表



```
public interface SingletonBeanRegistry {

   /**
    * 注册单例bean
    */
   void registerSingleton(String beanName, Object singletonObject);

   /**
    * 获取单例bean
    */
   @Nullable
   Object getSingleton(String beanName);

   /**
    * 是否存在单例对象
    */
   boolean containsSingleton(String beanName);

   /**
    * 获取所有的单例对象beanName
    */
   String[] getSingletonNames();

   /**
    * 单例bean的数量
    */
   int getSingletonCount();


   Object getSingletonMutex();

}
```