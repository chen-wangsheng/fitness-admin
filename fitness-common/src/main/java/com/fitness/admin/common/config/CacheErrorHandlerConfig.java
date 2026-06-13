package com.fitness.admin.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存异常处理:Redis 不可用时降级为直接读/写 DB,避免 500。
 *
 * <p>Spring Cache 的 {@code CacheInterceptor} 会捕获缓存读写异常并调用
 * {@link CacheErrorHandler}。默认实现 {@code SimpleCacheErrorHandler} 会重新抛出,
 * 导致接口 500。本配置把异常吞掉并降级。
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheErrorHandlerConfig implements CachingConfigurer {

    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler();
    }

    static class LoggingCacheErrorHandler implements CacheErrorHandler {

        @Override
        public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
            log.warn("Redis 缓存读取失败,降级到 DB。cache={}, key={}, err={}",
                    cache.getName(), key, e.getMessage());
        }

        @Override
        public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
            log.warn("Redis 缓存写入失败,跳过缓存。cache={}, key={}, err={}",
                    cache.getName(), key, e.getMessage());
        }

        @Override
        public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
            log.warn("Redis 缓存删除失败,忽略。cache={}, key={}, err={}",
                    cache.getName(), key, e.getMessage());
        }

        @Override
        public void handleCacheClearError(RuntimeException e, Cache cache) {
            log.warn("Redis 缓存清空失败,忽略。cache={}, err={}",
                    cache.getName(), e.getMessage());
        }
    }
}
