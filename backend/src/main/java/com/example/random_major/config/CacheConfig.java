package com.example.random_major.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * CacheConfig
 * ===========
 * Configures a Caffeine in-memory cache for LIME explanations (limeCache).
 * Cache settings are driven by application.properties.
 *
 * Cache key = SHA-256 of (text + numFeatures) — set in LimeService.
 * Result: same text with same depth is never re-computed within TTL.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${lime.cache.max-size:500}")
    private int maxSize;

    @Value("${lime.cache.ttl-minutes:60}")
    private int ttlMinutes;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("limeCache");
        manager.setCaffeine(
            Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .recordStats()  // enables /actuator/metrics/cache.* endpoints
        );
        return manager;
    }
}
