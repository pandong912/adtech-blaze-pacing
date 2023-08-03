package com.hotstar.adtech.blaze.exchanger.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {
  public static final String COHORT_CONCURRENCY = "cohortConcurrency";
  public static final String COHORT_CONCURRENCY_MANAGER = "cacheManager";
  public static final String STREAM_CONCURRENCY = "streamConcurrency";
  public static final String STREAM_CONCURRENCY_MANAGER = "cacheManager";
  public static final String SINGLE_PLATFORM_STREAM_CONCURRENCY = "singlePlatformStreamConcurrency";
  public static final String SINGLE_PLATFORM_STREAM_CONCURRENCY_MANAGER = "cacheManager";
  public static final String STREAM_DEFINITION = "streamDefinition";
  public static final String STREAM_DEFINITION_V2 = "streamDefinitionV2";
  public static final String SEASON_ID_BY_CONTENT = "seasonIdByContent";
  public static final String LANGUAGE = "language";
  public static final String PLATFORM = "platform";
  public static final String REACH_AD_SET = "reachAdSet";
  public static final String DATABASE_CACHE_MANAGER = "databaseCacheManager";
  public static final String AD_MODEL_CACHE_MANAGER = "adModelCacheManager";

  @Bean
  @Override
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager =
      new CaffeineCacheManager(COHORT_CONCURRENCY, STREAM_CONCURRENCY, SINGLE_PLATFORM_STREAM_CONCURRENCY);
    cacheManager.setCaffeine(Caffeine.newBuilder()
      .initialCapacity(16)
      .maximumSize(128)
      .recordStats()
      .expireAfterWrite(Duration.ofSeconds(5)));
    return cacheManager;
  }

  @Bean
  public CacheManager impressionCacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("impression");
    cacheManager.setCaffeine(Caffeine.newBuilder()
      .initialCapacity(16)
      .maximumSize(128)
      .recordStats()
      .expireAfterWrite(Duration.ofSeconds(5)));
    return cacheManager;
  }

  @Bean
  public CacheManager databaseCacheManager() {
    CaffeineCacheManager cacheManager =
      new CaffeineCacheManager(STREAM_DEFINITION, STREAM_DEFINITION_V2, SEASON_ID_BY_CONTENT, LANGUAGE, PLATFORM,
        REACH_AD_SET);
    cacheManager.setCaffeine(Caffeine.newBuilder()
      .initialCapacity(16)
      .maximumSize(128)
      .recordStats()
      .expireAfterWrite(Duration.ofMinutes(5)));
    return cacheManager;
  }

  @Bean
  public CacheManager adModelCacheManager() {
    CaffeineCacheManager cacheManager =
      new CaffeineCacheManager(REACH_AD_SET);
    cacheManager.setCaffeine(Caffeine.newBuilder()
      .initialCapacity(16)
      .maximumSize(128)
      .recordStats()
      .expireAfterWrite(Duration.ofSeconds(10)));
    return cacheManager;
  }

}
