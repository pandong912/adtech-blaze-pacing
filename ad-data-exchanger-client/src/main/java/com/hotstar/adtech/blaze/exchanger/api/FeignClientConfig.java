package com.hotstar.adtech.blaze.exchanger.api;

import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

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

  private static final HttpMessageConverters jacksonConverters = new HttpMessageConverters(
    new MappingJackson2HttpMessageConverter(SerializeUtils.objectMapper()));

  @Bean
  public Decoder feignDecoder() {
    ObjectFactory<HttpMessageConverters> objectFactory = () -> jacksonConverters;
    return new ResponseEntityDecoder(new SpringDecoder(objectFactory));
  }

  @Bean
  public Encoder feignEncoder() {
    ObjectFactory<HttpMessageConverters> objectFactory = () -> jacksonConverters;
    return new SpringEncoder(objectFactory);
  }

}
