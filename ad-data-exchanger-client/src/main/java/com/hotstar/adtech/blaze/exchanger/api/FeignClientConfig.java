package com.hotstar.adtech.blaze.exchanger.api;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class FeignClientConfig {

  @Value("${blaze.data-exchanger.retryer.period:100}")
  private long period;
  @Value("${blaze.data-exchanger.retryer.max-period:1000}")
  private long maxPeriod;
  @Value("${blaze.data-exchanger.retryer.max-attempts:3}")
  private int maxAttempts;

  @Bean
  public Retryer retryer() {
    return new Retryer.Default(period, maxPeriod, maxAttempts);
  }

  @Bean
  public ErrorDecoder errorDecoder() {
    return new DefaultErrorDecoder();
  }

}
