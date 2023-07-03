package com.hotstar.adtech.blaze.exchanger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.hotstar.adtech.blaze"})
public class DataExchangerApplication {
  public static void main(String[] args) {
    SpringApplication.run(DataExchangerApplication.class, args);
  }
}
