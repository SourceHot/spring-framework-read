package org.sourcehot.spring.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CacheContext<T> {
    private Map<Integer, T> cacheMap = new ConcurrentHashMap<>();

    public T get(Integer key) {
        return cacheMap.get(key);
    }

    public void addOrUpdate(Integer key, T value) {
        cacheMap.put(key, value);
    }

    public void removeKey(Integer key) {
        if (cacheMap.containsKey(key)) {
            cacheMap.remove(key);
        }
    }
    public void clean(){
        cacheMap.clear();
    }
}
