# Spring Bean 



## 作用域
### singleton
- 在 Spring 容器中有且仅有一个 Bean 实例. `singleton`是默认值
### prototype
- 在 Spring 容器中存在不止一个 Bean 实例, 每次获取 Bean 实例都会创建一个新的对象. 

### request
- 每次 http 请求都会创建一个新的 Bean 对象, 
- 仅对 webApplicationContext 有效

### Session
- 同一个 http Session 共同享用同一个 Bean 对象
- 仅对 webApplicationContext 有效

### globalSession
- 每个全局的 http Session，使用 Session 定义的 Bean 都将产生一个新实例
- 仅对 webApplicationContext 有效

### 实例




## 生命周期
1. 实例化
1. Bean 属性注入
1. 设置 bean name , org.springframework.beans.factory.BeanNameAware.setBeanName 
1. 设置 bean factory , org.springframework.beans.factory.BeanFactoryAware.setBeanFactory
1. 设置上下文 org.springframework.context.ApplicationContextAware.setApplicationContext
1. 前置准备 org.springframework.beans.factory.config.BeanPostProcessor.postProcessBeforeInitialization
1. 属性设置 org.springframework.beans.factory.InitializingBean.afterPropertiesSet
1. init method 
1. 后置方法 org.springframework.beans.factory.config.BeanPostProcessor.postProcessAfterInitialization
1. bean 销毁 org.springframework.beans.factory.DisposableBean

