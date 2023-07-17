package com.hotstar.adtech.blaze.ingester.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class AppConfig implements SchedulingConfigurer {

  @Override
  public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    taskRegistrar.setScheduler(taskScheduler());
  }

  @Bean(destroyMethod = "shutdown")
  public Executor taskScheduler() {
    return Executors.newScheduledThreadPool(4);
  }

}
