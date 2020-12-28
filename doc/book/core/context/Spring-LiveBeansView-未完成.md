# Spring LiveBeansView
- 类全路径: `org.springframework.context.support.LiveBeansView`





## 方法分析

### registerApplicationContext

- 方法签名: `org.springframework.context.support.LiveBeansView#registerApplicationContext`

- 方法作用: 注册应用上下文



```java
static void registerApplicationContext(ConfigurableApplicationContext applicationContext) {

   // 获取属性 spring.liveBeansView.mbeanDomain
   String mbeanDomain = applicationContext.getEnvironment().getProperty(MBEAN_DOMAIN_PROPERTY_NAME);
   if (mbeanDomain != null) {
      synchronized (applicationContexts) {
         if (applicationContexts.isEmpty()) {
            try {
               MBeanServer server = ManagementFactory.getPlatformMBeanServer();
               // 获取应用名称
               applicationName = applicationContext.getApplicationName();
               // 注册
               server.registerMBean(new LiveBeansView(),
                     new ObjectName(mbeanDomain, MBEAN_APPLICATION_KEY, applicationName));
            }
            catch (Throwable ex) {
               throw new ApplicationContextException("Failed to register LiveBeansView MBean", ex);
            }
         }
         applicationContexts.add(applicationContext);
      }
   }
}
```



### unregisterApplicationContext

- 方法签名: `org.springframework.context.support.LiveBeansView#unregisterApplicationContext`

- 方法作用: 移除应用上下文