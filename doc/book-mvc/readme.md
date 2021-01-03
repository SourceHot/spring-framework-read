# Spring MVC 源码阅读指南

## Spring MVC 中核心对象
- 首先笔者先列出一些 SpringMVC 中的一些核心类(类、接口、注解)各位读者可以根据自己的需求进行阅读. 
- org.springframework.web.servlet.DispatcherServlet 核心servlet
- RequestMapping \ GetMapping \ PostMapping \ PutMapping \ DeleteMapping \ PatchMapping
- Controller \ RestController 
- InternalResourceViewResolver 视图解析器
- RequestBody
- ResponseBody
- PathVariable
- BeanNameUrlHandlerMapping 
- ParameterMethodNameResolver: 匹配方法并调用的核心



## 起手

- 起手从官方文档出发, 
  1. 搭建环境
  2. 阅读文档提供的主要信息

- 在开始分析之前我们需要先创建出一个可以用来 debug 的测试环境. 具体操作可以在官网找到: [link](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#spring-web)



在官方文档中有下面这段话

> Spring MVC, as many other web frameworks, is designed around the front controller pattern where a central `Servlet`, the `DispatcherServlet`, provides a shared algorithm for request processing, while actual work is performed by configurable delegate components. This model is flexible and supports diverse workflows.
>
> The `DispatcherServlet`, as any `Servlet`, needs to be declared and mapped according to the Servlet specification by using Java configuration or in `web.xml`. In turn, the `DispatcherServlet` uses Spring configuration to discover the delegate components it needs for request mapping, view resolution, exception handling, [and more](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-servlet-special-bean-types).
>
> The following example of the Java configuration registers and initializes the `DispatcherServlet`, which is auto-detected by the Servlet container (see [Servlet Config](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-container-config)):



大致意思是说 Spring MVC 提供了一个 servlet 的类( `DispatcherServlet` ) 用来处理请求, 响应. 





在文档中还有一个关键的容器图片👇

![mvc上下文层次结构](image/mvc-context-hierarchy.png)

- 在这个容器中我们可以认识到 SpringMVC 的大体结构是什么样子的. 



在 SpringMVC 的文档中还有一个表格列出了一些特殊的 Bean



| Bean type                                                    | Explanation                                                  |
| :----------------------------------------------------------- | :----------------------------------------------------------- |
| `HandlerMapping`                                             | Map a request to a handler along with a list of [interceptors](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-handlermapping-interceptor) for pre- and post-processing. The mapping is based on some criteria, the details of which vary by `HandlerMapping` implementation.The two main `HandlerMapping` implementations are `RequestMappingHandlerMapping` (which supports `@RequestMapping` annotated methods) and `SimpleUrlHandlerMapping` (which maintains explicit registrations of URI path patterns to handlers). |
| `HandlerAdapter`                                             | Help the `DispatcherServlet` to invoke a handler mapped to a request, regardless of how the handler is actually invoked. For example, invoking an annotated controller requires resolving annotations. The main purpose of a `HandlerAdapter` is to shield the `DispatcherServlet` from such details. |
| [`HandlerExceptionResolver`](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-exceptionhandlers) | Strategy to resolve exceptions, possibly mapping them to handlers, to HTML error views, or other targets. See [Exceptions](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-exceptionhandlers). |
| [`ViewResolver`](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-viewresolver) | Resolve logical `String`-based view names returned from a handler to an actual `View` with which to render to the response. See [View Resolution](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-viewresolver) and [View Technologies](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-view). |
| [`LocaleResolver`](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-localeresolver), [LocaleContextResolver](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-timezone) | Resolve the `Locale` a client is using and possibly their time zone, in order to be able to offer internationalized views. See [Locale](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-localeresolver). |
| [`ThemeResolver`](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-themeresolver) | Resolve themes your web application can use — for example, to offer personalized layouts. See [Themes](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-themeresolver). |
| [`MultipartResolver`](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-multipart) | Abstraction for parsing a multi-part request (for example, browser form file upload) with the help of some multipart parsing library. See [Multipart Resolver](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-multipart). |
| [`FlashMapManager`](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-flash-attributes) | Store and retrieve the “input” and the “output” `FlashMap` that can be used to pass attributes from one request to another, usually across a redirect. See [Flash Attributes](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-flash-attributes). |





- 笔者后续所进行的各类分析基本上是围绕上述内容进行展开. 有关文档性质的内容请各位读者自行阅读. 笔者不在这里展开导读。









## 分析

- 下面开始正式分析. 分析的方式如下

  首先根据官方文档我们配置了 `web.xml` 这个文件. 这就是第一个分析点**`web.xml` 这个文件中的配置起到了什么作用**

  其次我们在 SpringMVC 中创建了基本工程( 一个简单的 Controller ) **在访问时 SpringMVC 是如何处理请求的.** 

  - 概要： **启动流程、执行流程**







- 首先我们来看 **web.xml** 的内容



```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
       version="4.0">


   <servlet>
      <servlet-name>sp</servlet-name>
      <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
      <init-param>
         <param-name>contextConfigLocation</param-name>
         <param-value>classpath:spring-mvc.xml</param-value>
      </init-param>
   </servlet>
   <servlet-mapping>
      <servlet-name>sp</servlet-name>
      <url-pattern>/</url-pattern>
   </servlet-mapping>
</web-app>
```



- 在文档中告诉我们这里配置的 `DispatcherServlet` 是一个分发请求到具体的 Controller 上的. 他对于启动阶段没有一个直接的帮助. 仅仅作为配置信息存在于此, 到处理请求的时候才会有一个真正的应用. 

  

  

  

**那么启动到底做了什么呢？** 

在 `web.xml`中我们引用了一个外部文件： `spring-mvc.xml` 文件. 这个文件是 Spring xml 配置文件. 在里面包含了 Spring 的启动流程. (在这里就成为 SpringMVC 的启动流程了, 这个说法欠妥, 本质还是 Spring 的启动流程) . 下面把 `spring-mvc.xml` 文件贴出来





```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:context="http://www.springframework.org/schema/context"
      xmlns:mvc="http://www.springframework.org/schema/mvc"
      xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context https://www.springframework.org/schema/context/spring-context.xsd http://www.springframework.org/schema/mvc https://www.springframework.org/schema/mvc/spring-mvc.xsd">



   <!-- 配置扫描的Controller -->
   <context:component-scan base-package="com.source.hot" />
   <!-- 静态资源访问，例如图片、js文件、css文件等 -->
   <mvc:default-servlet-handler />
   <!-- 开启注解 -->
   <mvc:annotation-driven />
   <!-- jspViewResolver jsp视图解析器 -->
   <bean id="jspViewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
      <property name="viewClass" value="org.springframework.web.servlet.view.JstlView"/>
      <property name="prefix" value="/page/"/><!-- 后台默认返回视图寻找视图文件的路径 -->
      <property name="suffix" value=".jsp"/><!-- 后台返回视图默认添加的后缀 -->
   </bean>
</beans>
```





在这个 xml 文件中我们看到了在 SpringIoC 以外的一些标签, 这些标签可是说属于 SpringMVC 的特有标签 `<mvc: */>` 关于这部分标签的处理涉及到的核心接口是 **`NamespaceHandler`**

在这里我们需要阅读的源码类应该是 **`MvcNamespaceHandler`** 实现类



<details>
    <summary>MvcNamespaceHandler 详细代码</summary>





```java
public class MvcNamespaceHandler extends NamespaceHandlerSupport {

   @Override
   public void init() {
      registerBeanDefinitionParser("annotation-driven", new AnnotationDrivenBeanDefinitionParser());
      registerBeanDefinitionParser("default-servlet-handler", new DefaultServletHandlerBeanDefinitionParser());
      registerBeanDefinitionParser("interceptors", new InterceptorsBeanDefinitionParser());
      registerBeanDefinitionParser("resources", new ResourcesBeanDefinitionParser());
      registerBeanDefinitionParser("view-controller", new ViewControllerBeanDefinitionParser());
      registerBeanDefinitionParser("redirect-view-controller", new ViewControllerBeanDefinitionParser());
      registerBeanDefinitionParser("status-controller", new ViewControllerBeanDefinitionParser());
      registerBeanDefinitionParser("view-resolvers", new ViewResolversBeanDefinitionParser());
      registerBeanDefinitionParser("tiles-configurer", new TilesConfigurerBeanDefinitionParser());
      registerBeanDefinitionParser("freemarker-configurer", new FreeMarkerConfigurerBeanDefinitionParser());
      registerBeanDefinitionParser("groovy-configurer", new GroovyMarkupConfigurerBeanDefinitionParser());
      registerBeanDefinitionParser("script-template-configurer", new ScriptTemplateConfigurerBeanDefinitionParser());
      registerBeanDefinitionParser("cors", new CorsBeanDefinitionParser());
   }

}
```



</details>



通过这段代码各位读者可以进行阅读各个 `<mvc: * />` 的解析逻辑(从 xml 转换成 JAVA 对象的过程) 。





在了解完成容器的初始化流程后我们就可以来关注请求的处理了，即 `DispatcherServlet` 的分析核心方法是 `doDispatch` . 