package com.hotstar.adtech.blaze.adserver.ingester.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorConfig {

  @Value("${blaze.ad-ingester-service.executor.core-pool-size:4}")
  private int corePoolSize;
  @Value("${blaze.ad-ingester-service.executor.max-pool-size:16}")
  private int maxPoolSize;
  @Value("${blaze.ad-ingester-service.executor.keep-alive-seconds:60}")
  private int keepAliveSeconds;
  @Value("${blaze.ad-ingester-service.executor.queue-capacity:20000}")
  private int queueCapacity;


  @Bean(name = "concurrencyExecutor")
  public Executor concurrencyExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setKeepAliveSeconds(keepAliveSeconds);
    executor.setQueueCapacity(queueCapacity);
    executor.setThreadNamePrefix("async-concurrency-thread-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();

    return executor;
  }

  @Bean(name = "impressionExecutor")
  public Executor impressionExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setKeepAliveSeconds(keepAliveSeconds);
    executor.setQueueCapacity(queueCapacity);
    executor.setThreadNamePrefix("async-impression-thread-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();

    return executor;
  }
}
