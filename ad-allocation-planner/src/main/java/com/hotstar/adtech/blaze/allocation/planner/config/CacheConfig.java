package com.hotstar.adtech.blaze.allocation.planner.config;

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
  public static final String SHALE_DATA = "shaleData";
  public static final String HWM_DATA = "hwmData";
  public static final String CACHE_MANAGER = "cacheManager";

  @Bean
  @Override
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager =
      new CaffeineCacheManager(SHALE_DATA, HWM_DATA);
    cacheManager.setCaffeine(Caffeine.newBuilder()
      .initialCapacity(2)
      .maximumSize(4)
      .recordStats()
      .expireAfterWrite(Duration.ofMinutes(4)));
    return cacheManager;
  }
}
