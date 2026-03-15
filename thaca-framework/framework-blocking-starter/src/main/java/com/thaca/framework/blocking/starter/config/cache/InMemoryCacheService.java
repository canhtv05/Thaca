package com.thaca.framework.blocking.starter.config.cache;

import com.thaca.framework.core.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class InMemoryCacheService<K, V> {

    private final Map<K, V> cache = new ConcurrentHashMap<>();

    public void put(K key, V value) {
        try {
            if (CommonUtils.isEmpty(key, value)) return;
            cache.put(key, value);
        } catch (Exception e) {
             log.error("[InMemoryCacheService]:: exception: ", e);
        }
    }

    public Optional<V> get(K key) {
        try {
            if (CommonUtils.isEmpty(key)) return Optional.empty();
            return Optional.ofNullable(cache.get(key));
        } catch (Exception e) {
             log.error("[InMemoryCacheService]:: exception: ", e);
            return Optional.empty();
        }
    }

    public void evict(K key) {
        try {
            if (Objects.isNull(key)) return;
            cache.remove(key);
        } catch (Exception e) {
             log.error("[InMemoryCacheService]:: exception: ", e);
        }
    }

    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    public void remove(K key) {
        try {
            if (Objects.isNull(key)) return;
            cache.remove(key);
        } catch (Exception e) {
             log.error("[InMemoryCacheService]:: exception: ", e);
        }
    }

    public void clear() {
        try {
            cache.clear();
        } catch (Exception e) {
             log.error("[InMemoryCacheService]:: exception: ", e);
        }
    }

    public int size() {
        try {
            return cache.size();
        } catch (Exception e) {
             log.error("[InMemoryCacheService]:: exception: ", e);
            return 0;
        }
    }
}
