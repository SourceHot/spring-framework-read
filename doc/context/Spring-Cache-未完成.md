# Spring Cache 
- Author: [HuiFer](https://github.com/huifer)
- 源码阅读仓库: [SourceHot-spring](https://github.com/SourceHot/spring-framework-read)

## 分析
```java
public interface Cache {

    /**
     * 获取名称
     */
    String getName();

    /**
     * Return the underlying native cache provider.
     */
    Object getNativeCache();

    /**
     * 获取值
     */
    @Nullable
    ValueWrapper get(Object key);

    /**
     * 根据key 和类型获取值
     */
    @Nullable
    <T> T get(Object key, @Nullable Class<T> type);

    /**
     * 获取数据
     */
    @Nullable
    <T> T get(Object key, Callable<T> valueLoader);

    /**
     * 添加数据
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     */
    void put(Object key, @Nullable Object value);

    @Nullable
    ValueWrapper putIfAbsent(Object key, @Nullable Object value);

    /**
     * 删除一个key对应的缓存
     */
    void evict(Object key);

    /**
     * 清空缓存
     */
    void clear();


    /**
     * 数据包装接口
     */
    @FunctionalInterface
    interface ValueWrapper {

        /**
         * 获取缓存的数据值
         */
        @Nullable
        Object get();
    }

    @SuppressWarnings("serial")
    class ValueRetrievalException extends RuntimeException {

        @Nullable
        private final Object key;

        public ValueRetrievalException(@Nullable Object key, Callable<?> loader, Throwable ex) {
            super(String.format("Value for key '%s' could not be loaded using '%s'", key, loader), ex);
            this.key = key;
        }

        @Nullable
        public Object getKey() {
            return this.key;
        }
    }

}
```