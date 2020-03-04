# Spring-MVC-Controller
- Author: [HuiFer](https://github.com/huifer)
- 源码阅读仓库: [SourceHot-spring](https://github.com/SourceHot/spring-framework-read)
- 源码路径: `org.springframework.web.servlet.mvc.Controller`
## Controller
```java
@FunctionalInterface
public interface Controller {

    /**
     * Process the request and return a ModelAndView object which the DispatcherServlet
     * will render. A {@code null} return value is not an error: it indicates that
     * this object completed request processing itself and that there is therefore no
     * ModelAndView to render.
     *
     * 处理用户请求
     * @param request  current HTTP request 请求
     * @param response current HTTP response 返回
     * @return a ModelAndView to render, or {@code null} if handled directly 模型和视图
     * @throws Exception in case of errors
     */
    @Nullable
    ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;

}

```
- 只有一个方法,处理请求,实现类`org.springframework.web.servlet.mvc.AbstractController`

类图

![image-20200207154717972](assets/image-20200207154717972.png)

![image-20200207154725604](assets/image-20200207154725604.png)



## AbstractController

- 基本处理方式如下

```java
    /**
     * 处理请求
     * @param request  current HTTP request 请求
     * @param response current HTTP response 返回
     * @return
     * @throws Exception
     */
    @Override
    @Nullable
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        // 请求方法判断是否匹配,options
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            response.setHeader("Allow", getAllowHeader());
            return null;
        }

        // Delegate to WebContentGenerator for checking and preparing.
        // 校验请求
        checkRequest(request);
        // 设置response
        prepareResponse(response);

        // Execute handleRequestInternal in synchronized block if required.
        if (this.synchronizeOnSession) {
            // 获取session
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object mutex = WebUtils.getSessionMutex(session);
                synchronized (mutex) {
                    return handleRequestInternal(request, response);
                }
            }
        }

        return handleRequestInternal(request, response);
    }

```



## ServletForwardingController

- servlet处理方式如下



```java
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {


        // 获取servlet 上下文
        ServletContext servletContext = getServletContext();
        Assert.state(servletContext != null, "No ServletContext");
        // 获取servlet
        RequestDispatcher rd = servletContext.getNamedDispatcher(this.servletName);
        if (rd == null) {
            throw new ServletException("No servlet with name '" + this.servletName + "' defined in web.xml");
        }

        // If already included, include again, else forward.
        // 是否为include请求
        if (useInclude(request, response)) {
            rd.include(request, response);
            if (logger.isTraceEnabled()) {
                logger.trace("Included servlet [" + this.servletName +
                        "] in ServletForwardingController '" + this.beanName + "'");
            }
        }
        else {
            // 请求转发
            rd.forward(request, response);
            if (logger.isTraceEnabled()) {
                logger.trace("Forwarded to servlet [" + this.servletName +
                        "] in ServletForwardingController '" + this.beanName + "'");
            }
        }

        return null;
    }

```



- 处理交给javax.servlet处理