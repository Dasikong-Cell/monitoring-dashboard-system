package com.monitor.dashboard.local;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class CacheManager {

    private static class Cached<T> {
        final T value;
        final long expireAt;

        Cached(T value, long ttlMs) {
            this.value = value;
            this.expireAt = System.currentTimeMillis() + ttlMs;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireAt;
        }
    }

    private final Map<String, Cached<?>> cache = new ConcurrentHashMap<>();

    public static final long TTL_FAST = 2_000L;
    public static final long TTL_MEDIUM = 3_000L;
    public static final long TTL_SLOW = 5_000L;

    public <T> T getOrCompute(String key, long ttlMs, Supplier<T> fn) {
        Cached<?> c = cache.get(key);
        if (c != null && !c.isExpired()) {
            RuntimeMetricsCollector.INSTANCE.recordHit();
            @SuppressWarnings("unchecked")
            T casted = (T) c.value;
            return casted;
        }
        RuntimeMetricsCollector.INSTANCE.recordMiss();
        long t0 = System.nanoTime();
        T v = fn.get();
        long cost = System.nanoTime() - t0;
        RuntimeMetricsCollector.INSTANCE.recordQuery(cost);
        cache.put(key, new Cached<>(v, ttlMs));
        return v;
    }

    public void invalidate(String key) {
        cache.remove(key);
    }

    public int size() {
        return cache.size();
    }
}
