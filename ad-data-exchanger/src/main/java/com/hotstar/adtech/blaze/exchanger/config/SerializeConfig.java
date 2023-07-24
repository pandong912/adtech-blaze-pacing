package com.hotstar.adtech.blaze.exchanger.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotstar.adtech.blaze.exchanger.api.SerializeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerializeConfig {
  private static final ObjectMapper MAPPER = SerializeUtils.objectMapper();

  @Bean
  public ObjectMapper objectMapper() {
    return MAPPER;
  }

}
