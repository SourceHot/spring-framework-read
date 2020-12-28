# Spring AbstractRefreshableWebApplicationContext
- 类全路径: `org.springframework.web.context.support.AbstractRefreshableWebApplicationContext`


## 成员变量


```java
    /**
	 *  Servlet context that this context runs in.
	 *
	 * servlet 上下文
	 * */
	@Nullable
	private ServletContext servletContext;

	/**
	 * Servlet config that this context runs in, if any.
	 *
	 * servlet 配置
	 * */
	@Nullable
	private ServletConfig servletConfig;

	/**
	 * Namespace of this context, or {@code null} if root.
	 *
	 * 命名空间
	 * */
	@Nullable
	private String namespace;

	/**
	 *  the ThemeSource for this ApplicationContext.
	 * 主题来源
	 * */
	@Nullable
	private ThemeSource themeSource;

```