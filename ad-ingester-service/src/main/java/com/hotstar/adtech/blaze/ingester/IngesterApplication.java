package com.hotstar.adtech.blaze.ingester;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.hotstar.adtech.blaze", "com.hotstar.platform.pulse"})
@EnableFeignClients(basePackages = {"com.hotstar.platform.pulse", "com.hotstar.adtech.blaze.exchanger"})
@RequiredArgsConstructor
@EnableScheduling
@EnableAsync
public class IngesterApplication {

  public static void main(String[] args) {
    SpringApplication.run(IngesterApplication.class, args);
  }

}
