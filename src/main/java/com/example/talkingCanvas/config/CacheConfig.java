package com.example.talkingCanvas.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine for in-memory caching.
 * Improves performance by reducing database queries for frequently accessed
 * data.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        List<Cache> caches = new ArrayList<>();
        // Painting list cache - 15 minutes TTL, max 500 entries
        caches.add(buildCache("paintings", 500, 15, TimeUnit.MINUTES));
        // Individual painting details - 10 minutes TTL, max 200 entries
        caches.add(buildCache("painting-details", 200, 10, TimeUnit.MINUTES));
        // Categories cache - 1 hour TTL, max 50 entries (rarely changes)
        caches.add(buildCache("categories", 50, 60, TimeUnit.MINUTES));
        // Admin dashboard stats - 5 minutes TTL, max 10 entries
        caches.add(buildCache("dashboard-stats", 10, 5, TimeUnit.MINUTES));

        cacheManager.setCaches(caches);

        return cacheManager;
    }

    private CaffeineCache buildCache(String name, int maxSize, long duration, TimeUnit unit) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(duration, unit)
                .recordStats()
                .build());
    }
}
