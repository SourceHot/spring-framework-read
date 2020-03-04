# Spring-MVC-ViewResolverRegistry
- Author: [HuiFer](https://github.com/huifer)
- 源码阅读仓库: [SourceHot-spring](https://github.com/SourceHot/spring-framework-read)
- 源码路径: `org.springframework.web.servlet.config.annotation.ViewResolverRegistry`
- `ViewResolverRegistry`是`org.springframework.web.servlet.config.annotation.WebMvcConfigurer`的一个配置`org.springframework.web.servlet.config.annotation.WebMvcConfigurer.configureViewResolvers`
```java
public interface WebMvcConfigurer {

    default void configureViewResolvers(ViewResolverRegistry registry) {
    }
}
```

## 方法解析
### enableContentNegotiation
- 初始化视图解析器,设置基本信息
```java
    public void enableContentNegotiation(View... defaultViews) {
        // 初始化视图解析器
        initContentNegotiatingViewResolver(defaultViews);
    }

    /**
     *
     * @param defaultViews
     * @return 内容协调器,可以对不同类型的数据进行包装,做不同的返回值,如context-type=json，或者context-type=xml
     */
    private ContentNegotiatingViewResolver initContentNegotiatingViewResolver(View[] defaultViews) {
        // ContentNegotiatingResolver in the registry: elevate its precedence!
        // 设置order
        this.order = (this.order != null ? this.order : Ordered.HIGHEST_PRECEDENCE);


        if (this.contentNegotiatingResolver != null) {
            if (!ObjectUtils.isEmpty(defaultViews) &&
                    !CollectionUtils.isEmpty(this.contentNegotiatingResolver.getDefaultViews())) {
                // 视图设置
                List<View> views = new ArrayList<>(this.contentNegotiatingResolver.getDefaultViews());
                views.addAll(Arrays.asList(defaultViews));
                this.contentNegotiatingResolver.setDefaultViews(views);
            }
        }
        else {
            this.contentNegotiatingResolver = new ContentNegotiatingViewResolver();
            this.contentNegotiatingResolver.setDefaultViews(Arrays.asList(defaultViews));
            this.contentNegotiatingResolver.setViewResolvers(this.viewResolvers);
            if (this.contentNegotiationManager != null) {
                this.contentNegotiatingResolver.setContentNegotiationManager(this.contentNegotiationManager);
            }
        }
        return this.contentNegotiatingResolver;
    }

```


### jsp
- 设置jsp相关信息,前缀和后缀以及url视图解析器
```java

    public UrlBasedViewResolverRegistration jsp() {
        return jsp("/WEB-INF/", ".jsp");
    }


    public UrlBasedViewResolverRegistration jsp(String prefix, String suffix) {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        // 设置前缀
        resolver.setPrefix(prefix);
        // 设置后缀
        resolver.setSuffix(suffix);
        this.viewResolvers.add(resolver);
        return new UrlBasedViewResolverRegistration(resolver);
    }

```