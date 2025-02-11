package com.hotstar.adtech.blaze.allocation.planner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.hotstar.adtech.blaze"})
@EnableFeignClients(basePackages = {"com.hotstar.adtech.blaze.exchanger"})
@EnableCaching
@EnableScheduling
@EnableAsync
public class AllocationApplication {

  public static void main(String[] args) {
    SpringApplication.run(AllocationApplication.class, args);
  }

}
