package com.hotstar.adtech.blaze.allocation.diagnosis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.hotstar.adtech.blaze"})
@EnableScheduling
@EnableFeignClients(basePackages = {"com.hotstar.adtech.blaze.exchanger"})
public class DiagnosisWriterApplication {
  public static void main(String[] args) {
    SpringApplication.run(DiagnosisWriterApplication.class, args);
  }
}
